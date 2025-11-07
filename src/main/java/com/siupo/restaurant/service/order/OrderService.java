package com.siupo.restaurant.service.order;

import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.OrderResponse;
import com.siupo.restaurant.model.User;

public interface OrderService {

	OrderResponse createOrder(CreateOrderRequest request, User user);

	OrderResponse getOrderById(Long id, User user);

}
