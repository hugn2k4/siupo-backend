package com.siupo.restaurant.service.orderAtTable;

import com.siupo.restaurant.dto.request.OrderAtTableRequest;
import com.siupo.restaurant.dto.response.OrderAtTableResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderAtTableService {

    OrderAtTableResponse createOrder(OrderAtTableRequest request);

    OrderAtTableResponse getOrderById(Long orderId);

    Page<OrderAtTableResponse> getAllOrders(Long tableId, EOrderStatus status, Pageable pageable);

    OrderAtTableResponse updateOrderStatus(Long orderId, EOrderStatus status);

    OrderAtTableResponse processPayment(Long orderId, EPaymentMethod paymentMethod);

    void deleteOrder(Long orderId);
}