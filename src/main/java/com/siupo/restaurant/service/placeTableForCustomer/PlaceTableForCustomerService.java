package com.siupo.restaurant.service.placeTableForCustomer;

import com.siupo.restaurant.dto.request.PlaceTableForCustomerRequest;
import com.siupo.restaurant.dto.response.PlaceTableForCustomerResponse;

public interface PlaceTableForCustomerService {

    // Tạo đặt bàn mới (có thể chọn món trước hoặc không)
    PlaceTableForCustomerResponse createPlaceTable(PlaceTableForCustomerRequest request);

    PlaceTableForCustomerResponse updateTotalPrice(Long placeTableId);
    // Gửi thông báo xác nhận đặt bàn
    void sendBookingConfirmation(Long placeTableId);
}