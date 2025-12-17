package com.siupo.restaurant.service.orderAtTable;

import com.siupo.restaurant.dto.request.OrderAtTableRequest;
import com.siupo.restaurant.dto.response.OrderAtTableResponse;
import com.siupo.restaurant.dto.response.OrderItemResponse;
import com.siupo.restaurant.dto.response.ProductSimpleResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.OrderAtTableRepository;
import com.siupo.restaurant.repository.OrderItemRepository;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public OrderAtTableResponse createOrder(OrderAtTableRequest request) {
        log.info("Creating order at table for table ID: {}", request.getTableId());

        // Kiểm tra bàn có tồn tại không
        TableEntity table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn"));

        // Lấy danh sách product IDs từ request
        List<Long> productIds = request.getItems().stream()
                .map(OrderAtTableRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        // Kiểm tra tất cả sản phẩm có tồn tại không
        List<Product> products = productRepository.findByIdIn(productIds);

        if (products.size() != productIds.size()) {
            throw new NotFoundException(ErrorCode.LOI_CHUA_DAT);
//            throw new NotFoundException("Một số sản phẩm không tồn tại");
        }

        // Tạo map để dễ dàng tra cứu product
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // Kiểm tra sản phẩm còn hàng không
        for (OrderAtTableRequest.OrderItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product.getStatus() != EProductStatus.AVAILABLE) {
                throw new NotFoundException(ErrorCode.LOI_CHUA_DAT);
//                throw new OutOfStockException(
//                        String.format("Món '%s' đã hết hàng. Vui lòng chọn món khác", product.getName())
//                );
            }
        }


        // Tạo đơn hàng
        OrderAtTable orderAtTable = OrderAtTable.builder()
                .table(table)
                .items(new ArrayList<>())
                .build();

        OrderAtTable savedOrder = orderAtTableRepository.save(orderAtTable);

        // Tạo các order items
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderAtTableRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMap.get(itemRequest.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .orderAtTable(savedOrder)
                    .reviewed(false)
                    .build();

            orderItems.add(orderItem);
            totalAmount += product.getPrice() * itemRequest.getQuantity();
        }

        orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        return buildOrderResponse(savedOrder, totalAmount);
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
                .createdAt(order.getCreatedAt())
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
}