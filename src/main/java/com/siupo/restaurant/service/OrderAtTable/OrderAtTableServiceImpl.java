package com.siupo.restaurant.service.OrderAtTable;

import com.siupo.restaurant.dto.request.OrderAtTableRequest;
import com.siupo.restaurant.dto.response.OrderAtTableResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.NotFoundException;
import com.siupo.restaurant.exception.OutOfStockException;
import com.siupo.restaurant.model.OrderAtTable;
import com.siupo.restaurant.model.OrderItem;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.TableEntity;
import com.siupo.restaurant.repository.OrderAtTableRepository;
import com.siupo.restaurant.repository.OrderItemRepository;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.TableRepository;
import com.siupo.restaurant.service.OrderAtTable.OrderAtTableService;
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
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bàn"));

        // Lấy danh sách product IDs từ request
        List<Long> productIds = request.getItems().stream()
                .map(OrderAtTableRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        // Kiểm tra tất cả sản phẩm có tồn tại không
        List<Product> products = productRepository.findByIdIn(productIds);

        if (products.size() != productIds.size()) {
            throw new NotFoundException("Một số sản phẩm không tồn tại");
        }

        // Tạo map để dễ dàng tra cứu product
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // Kiểm tra sản phẩm còn hàng không
        for (OrderAtTableRequest.OrderItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product.getStatus() != EProductStatus.AVAILABLE) {
                throw new OutOfStockException(
                        String.format("Món '%s' đã hết hàng. Vui lòng chọn món khác", product.getName())
                );
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
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        double totalAmount = order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        return buildOrderResponse(order, totalAmount);
    }

    private OrderAtTableResponse buildOrderResponse(OrderAtTable order, double totalAmount) {
        List<OrderAtTableResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderAtTableResponse.OrderItemResponse.builder()
                        .itemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice() * item.getQuantity())
                        .build())
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
}