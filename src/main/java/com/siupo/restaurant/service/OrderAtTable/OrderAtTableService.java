package com.siupo.restaurant.service.OrderAtTable;

import com.siupo.restaurant.dto.request.OrderAtTableRequest;
import com.siupo.restaurant.dto.response.OrderAtTableResponse;

public interface OrderAtTableService {

    OrderAtTableResponse createOrder(OrderAtTableRequest request);

    OrderAtTableResponse getOrderById(Long orderId);
}