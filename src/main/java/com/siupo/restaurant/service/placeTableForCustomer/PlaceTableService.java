package com.siupo.restaurant.service.placeTableForCustomer;

import com.siupo.restaurant.dto.request.PlaceTableRequest;
import com.siupo.restaurant.dto.response.PlaceTableResponse;
import com.siupo.restaurant.enums.EPlaceTableStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PlaceTableService {

    // Tạo đặt bàn mới (có thể chọn món trước hoặc không)
    PlaceTableResponse createPlaceTable(PlaceTableRequest request);

    // Lấy thông tin chi tiết đặt bàn
    PlaceTableResponse getPlaceTableById(Long id);

    // Lấy đặt bàn theo ID và userId (để khách hàng xem đơn của mình)
    PlaceTableResponse getPlaceTableByIdAndUserId(Long id, Long userId);

    // Lấy tất cả đặt bàn (admin)
    List<PlaceTableResponse> getAllPlaceTables();

    // Lấy danh sách đặt bàn của khách hàng
    List<PlaceTableResponse> getPlaceTablesByUserId(Long userId);

    // Lấy đặt bàn theo trạng thái
    List<PlaceTableResponse> getPlaceTablesByStatus(EPlaceTableStatus status);

    // Cập nhật trạng thái đặt bàn (admin xác nhận/hủy)
    PlaceTableResponse updatePlaceTableStatus(Long id, String status, String note);

    // Thêm món vào đơn đặt bàn (khách hàng có thể thêm món sau khi đặt)
    PlaceTableResponse addItemToPlaceTable(Long placeTableId, Long productId, Long quantity, String note);

    // Xóa món khỏi đơn đặt bàn
    PlaceTableResponse removeItemFromPlaceTable(Long placeTableId, Long orderItemId);

    // Hủy đặt bàn (khách hàng)
    void cancelPlaceTable(Long id);

    // Xóa đặt bàn (admin)
    void deletePlaceTable(Long id);

    // Lấy đặt bàn theo khoảng thời gian
    List<PlaceTableResponse> getPlaceTablesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Đếm số lượng đặt bàn theo trạng thái
    Long countPlaceTablesByStatus(EPlaceTableStatus status);

    // Cập nhật tổng tiền
    PlaceTableResponse updateTotalPrice(Long placeTableId);

    // Gửi thông báo xác nhận đặt bàn
    void sendBookingConfirmation(Long placeTableId);
}