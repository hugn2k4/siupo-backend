package com.siupo.restaurant.service.managePlaceTable;

import com.siupo.restaurant.dto.response.PlaceTableForCustomerResponse;
import com.siupo.restaurant.dto.response.PlaceTableForGuestResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ManagePlaceTableService {

    // ============= CUSTOMER BOOKINGS =============

    /**
     * Lấy tất cả đơn đặt bàn của customer
     */
    List<PlaceTableForCustomerResponse> getAllCustomerBookings();

    /**
     * Lấy chi tiết đơn đặt bàn customer theo ID
     */
    PlaceTableForCustomerResponse getCustomerBookingById(Long id);

    /**
     * Lấy đơn đặt bàn customer theo trạng thái
     */
    List<PlaceTableForCustomerResponse> getCustomerBookingsByStatus(String status);

    /**
     * Lấy đơn đặt bàn customer theo khoảng thời gian
     */
    List<PlaceTableForCustomerResponse> getCustomerBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Xác nhận đơn đặt bàn customer
     */
    PlaceTableForCustomerResponse confirmCustomerBooking(Long id, String note);

    /**
     * Từ chối đơn đặt bàn customer
     */
    PlaceTableForCustomerResponse denyCustomerBooking(Long id, String note);

    /**
     * Hoàn thành đơn đặt bàn customer
     */
    PlaceTableForCustomerResponse completeCustomerBooking(Long id, String note);

    /**
     * Cập nhật trạng thái tùy chỉnh cho đơn đặt bàn customer
     */
    PlaceTableForCustomerResponse updateCustomerBookingStatus(Long id, String status, String note);

    /**
     * Đếm số đơn đặt bàn customer theo trạng thái
     */
    Long countCustomerBookingsByStatus(String status);

    /**
     * Gửi lại thông báo xác nhận cho customer
     */
    void resendCustomerConfirmation(Long id);

    // ============= GUEST BOOKINGS =============

    /**
     * Lấy tất cả yêu cầu đặt bàn của guest
     */
    List<PlaceTableForGuestResponse> getAllGuestBookings();

    /**
     * Lấy chi tiết yêu cầu đặt bàn guest theo ID
     */
    PlaceTableForGuestResponse getGuestBookingById(Long id);

    /**
     * Lấy yêu cầu đặt bàn guest theo trạng thái
     */
    List<PlaceTableForGuestResponse> getGuestBookingsByStatus(String status);

    /**
     * Lấy yêu cầu đặt bàn guest theo số điện thoại
     */
    List<PlaceTableForGuestResponse> getGuestBookingsByPhone(String phoneNumber);

    /**
     * Lấy yêu cầu đặt bàn guest theo khoảng thời gian
     */
    List<PlaceTableForGuestResponse> getGuestBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Xác nhận yêu cầu đặt bàn guest
     */
    PlaceTableForGuestResponse confirmGuestBooking(Long id, String note);

    /**
     * Từ chối yêu cầu đặt bàn guest
     */
    PlaceTableForGuestResponse denyGuestBooking(Long id, String note);

    /**
     * Hoàn thành yêu cầu đặt bàn guest
     */
    PlaceTableForGuestResponse completeGuestBooking(Long id, String note);

    /**
     * Cập nhật trạng thái tùy chỉnh cho yêu cầu đặt bàn guest
     */
    PlaceTableForGuestResponse updateGuestBookingStatus(Long id, String status, String note);

    // ============= STATISTICS =============

    /**
     * Lấy thống kê tổng quan về đặt bàn
     */
    Map<String, Object> getBookingStatistics();

    /**
     * Lấy tất cả đặt bàn trong ngày hôm nay
     */
    Map<String, Object> getTodayBookings();
}