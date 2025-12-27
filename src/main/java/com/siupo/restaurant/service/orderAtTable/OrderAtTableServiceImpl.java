package com.siupo.restaurant.service.orderAtTable;

import com.siupo.restaurant.dto.request.OrderAtTableRequest;
import com.siupo.restaurant.dto.response.OrderAtTableResponse;
import com.siupo.restaurant.dto.response.OrderItemResponse;
import com.siupo.restaurant.dto.response.ProductSimpleResponse;
import com.siupo.restaurant.dto.response.MomoPaymentResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import com.siupo.restaurant.enums.EPaymentStatus;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.*;
import com.siupo.restaurant.service.payment.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAtTableServiceImpl implements OrderAtTableService {

    private final OrderAtTableRepository orderAtTableRepository;
    private final TableRepository tableRepository;
    private final ProductRepository productRepository;
    private final ComboRepository comboRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final MomoPaymentService momoPaymentService;

    @Override
    @Transactional
    public OrderAtTableResponse createOrder(OrderAtTableRequest request) {
        log.info("Creating order at table for table ID: {}", request.getTableId());

        // Kiểm tra bàn có tồn tại không
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));

        // Validate items: phải có ít nhất product hoặc combo
        for (OrderAtTableRequest.OrderItemRequest item : request.getItems()) {
            if (item.getProductId() == null && item.getComboId() == null) {
                throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
            }
            if (item.getProductId() != null && item.getComboId() != null) {
                throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
            }
        }

        // Lấy danh sách product IDs và combo IDs từ request
        List<Long> productIds = request.getItems().stream()
                .filter(item -> item.getProductId() != null)
                .map(OrderAtTableRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        List<Long> comboIds = request.getItems().stream()
                .filter(item -> item.getComboId() != null)
                .map(OrderAtTableRequest.OrderItemRequest::getComboId)
                .collect(Collectors.toList());

        // Kiểm tra products
        Map<Long, Product> productMap = productIds.isEmpty() ? Map.of() : 
                productRepository.findByIdIn(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        if (productMap.size() != productIds.size()) {
            throw new NotFoundException(ErrorCode.LOI_CHUA_DAT);
        }

        // Kiểm tra combos
        Map<Long, Combo> comboMap = comboIds.isEmpty() ? Map.of() :
                comboRepository.findByIdIn(comboIds).stream()
                        .collect(Collectors.toMap(Combo::getId, c -> c));

        if (comboMap.size() != comboIds.size()) {
            throw new NotFoundException(ErrorCode.LOI_CHUA_DAT);
        }

        // Kiểm tra sản phẩm/combo còn hàng không
        for (OrderAtTableRequest.OrderItemRequest item : request.getItems()) {
            if (item.getProductId() != null) {
                Product product = productMap.get(item.getProductId());
                if (product.getStatus() != EProductStatus.AVAILABLE) {
                    throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
                }
            }
        }

        // Tạo đơn hàng
        OrderAtTable orderAtTable = OrderAtTable.builder()
                .table(table)
                .items(new ArrayList<>())
                .status(EOrderStatus.PENDING)
                .build();

        OrderAtTable savedOrder = orderAtTableRepository.save(orderAtTable);

        // Tạo các order items
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderAtTableRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderItem.OrderItemBuilder orderItemBuilder = OrderItem.builder()
                    .quantity(itemRequest.getQuantity())
                    .orderAtTable(savedOrder)
                    .reviewed(false)
                    .note(itemRequest.getNote());

            if (itemRequest.getProductId() != null) {
                Product product = productMap.get(itemRequest.getProductId());
                orderItemBuilder.product(product).price(product.getPrice());
                totalAmount += product.getPrice() * itemRequest.getQuantity();
            } else if (itemRequest.getComboId() != null) {
                Combo combo = comboMap.get(itemRequest.getComboId());
                orderItemBuilder.combo(combo).price(combo.getBasePrice());
                totalAmount += combo.getBasePrice() * itemRequest.getQuantity();
            }

            orderItems.add(orderItemBuilder.build());
        }

        orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);
        savedOrder.setTotalPrice(totalAmount);

        // Xử lý thanh toán nếu có paymentMethod
        String payUrl = null;
        if (request.getPaymentMethod() != null) {
            if (request.getPaymentMethod() == EPaymentMethod.MOMO) {
                // Tạo temporary Order để gọi MomoPaymentService
                // Vì MomoPaymentService.createPayment() nhận Order object
                Order tempOrder = Order.builder()
                        .id(savedOrder.getId())
                        .totalPrice(totalAmount)
                        .build();

                try {
                    MomoPaymentResponse momoResponse = momoPaymentService.createPayment(tempOrder);
                    if (momoResponse != null && momoResponse.getPayUrl() != null) {
                        payUrl = momoResponse.getPayUrl();
                        log.info("MoMo payment URL created: {}", payUrl);

                        // Tạo Payment record và link với OrderAtTable
                        Payment payment = Payment.builder()
                                .paymentMethod(EPaymentMethod.MOMO)
                                .status(EPaymentStatus.PROCESSING)
                                .build();
                        Payment savedPayment = paymentRepository.save(payment);
                        savedOrder.setPayment(savedPayment);
                    }
                } catch (Exception e) {
                    log.error("Failed to create MoMo payment", e);
                    // Continue without payment - order still created
                }
            } else if (request.getPaymentMethod() == EPaymentMethod.COD) {
                // COD - thanh toán sau, không cần tạo payment ngay
                log.info("Order created with COD payment method - pay later");
            }
        }

        orderAtTableRepository.save(savedOrder);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        OrderAtTableResponse response = buildOrderResponse(savedOrder, totalAmount);
        response.setPayUrl(payUrl);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderAtTableResponse getOrderById(Long orderId) {
        log.info("Getting order by ID: {}", orderId);

        OrderAtTable order = orderAtTableRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        double totalAmount = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        return buildOrderResponse(order, totalAmount);
    }

    private OrderAtTableResponse buildOrderResponse(OrderAtTable order, double totalAmount) {
        // Map OrderItem sang OrderItemResponse (sử dụng class đã tách riêng)
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    // Map Product sang ProductSimpleResponse
                    ProductSimpleResponse productResponse = mapToProductSimpleResponse(item.getProduct());

                    // Build OrderItemResponse
                    return OrderItemResponse.builder()
                            .id(item.getId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .note(item.getNote())
                            .reviewed(item.getReviewed())
                            .product(productResponse)
                            .createdAt(item.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderAtTableResponse.builder()
                .orderId(order.getId())
                .tableId(order.getTable().getId())
                .tableName(order.getTable().getTableNumber())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .status(order.getStatus())
                .paymentMethod(order.getPayment() != null ? order.getPayment().getPaymentMethod() : null)
                .isPaid(order.getPayment() != null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    private ProductSimpleResponse mapToProductSimpleResponse(Product product) {
        if (product == null) return null;

        List<ProductImage> productImages = product.getImages();
        String imageUrl = null;
        List<String> imageUrls = new ArrayList<>();

        if (productImages != null && !productImages.isEmpty()) {
            // Lấy URL đầu tiên làm imageUrl chính
            imageUrl = productImages.get(0).getUrl();
            // Convert tất cả ProductImage sang List<String> URLs
            imageUrls = productImages.stream()
                    .map(ProductImage::getUrl)
                    .collect(Collectors.toList());
        }

        return ProductSimpleResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(imageUrl)
                .imageUrls(imageUrls)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderAtTableResponse> getAllOrders(Long tableId, EOrderStatus status, Pageable pageable) {
        log.info("Getting all orders at table - tableId: {}, status: {}", tableId, status);

        Page<OrderAtTable> orders;
        
        if (tableId != null && status != null) {
            orders = orderAtTableRepository.findByTableIdAndStatus(tableId, status, pageable);
        } else if (tableId != null) {
            orders = orderAtTableRepository.findByTableId(tableId, pageable);
        } else if (status != null) {
            orders = orderAtTableRepository.findByStatus(status, pageable);
        } else {
            orders = orderAtTableRepository.findAll(pageable);
        }

        return orders.map(order -> {
            double totalAmount = order.getTotalPrice() != null ? order.getTotalPrice() :
                    order.getItems().stream()
                            .mapToDouble(item -> item.getPrice() * item.getQuantity())
                            .sum();
            return buildOrderResponse(order, totalAmount);
        });
    }

    @Override
    @Transactional
    public OrderAtTableResponse updateOrderStatus(Long orderId, EOrderStatus status) {
        log.info("Updating order status - orderId: {}, newStatus: {}", orderId, status);

        OrderAtTable order = orderAtTableRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));

        order.setStatus(status);
        orderAtTableRepository.save(order);

        double totalAmount = order.getTotalPrice() != null ? order.getTotalPrice() :
                order.getItems().stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();

        return buildOrderResponse(order, totalAmount);
    }

    @Override
    @Transactional
    public OrderAtTableResponse processPayment(Long orderId, EPaymentMethod paymentMethod) {
        log.info("Processing payment for order: {}, method: {}", orderId, paymentMethod);

        OrderAtTable order = orderAtTableRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));

        double totalAmount = order.getTotalPrice() != null ? order.getTotalPrice() :
                order.getItems().stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();

        // Tạo payment
        Payment payment;
        if (paymentMethod == EPaymentMethod.COD) {
            payment = CODPayment.builder()
                    .amount(totalAmount)
                    .status(com.siupo.restaurant.enums.EPaymentStatus.PAID)
                    .paymentMethod(EPaymentMethod.COD)
                    .note("Thanh toán tại bàn")
                    .build();
        } else if (paymentMethod == EPaymentMethod.MOMO) {
            payment = MomoPayment.builder()
                    .amount(totalAmount)
                    .status(com.siupo.restaurant.enums.EPaymentStatus.PAID)
                    .paymentMethod(EPaymentMethod.MOMO)
                    .build();
        } else {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
        }

        order.setPayment(payment);
        order.setStatus(EOrderStatus.COMPLETED);
        orderAtTableRepository.save(order);

        return buildOrderResponse(order, totalAmount);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order: {}", orderId);

        OrderAtTable order = orderAtTableRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));

        orderAtTableRepository.delete(order);
    }
}