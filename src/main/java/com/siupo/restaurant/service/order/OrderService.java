package com.siupo.restaurant.service.order;

import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.CreateOrderResponse;
import com.siupo.restaurant.model.User;

public interface OrderService {

	CreateOrderResponse createOrder(CreateOrderRequest request, User user);

	CreateOrderResponse getOrderById(Long id, User user);

}
