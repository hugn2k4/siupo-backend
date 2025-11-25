package com.siupo.restaurant.service.payment;

import com.siupo.restaurant.dto.request.MomoIpnRequest;
import com.siupo.restaurant.dto.response.MomoPaymentResponse;
import com.siupo.restaurant.model.Order;

public interface MomoPaymentService {
    
    /**
     * Tạo payment URL từ MoMo
     * @param order Đơn hàng cần thanh toán
     * @return Response chứa payUrl từ MoMo
     */
    MomoPaymentResponse createPayment(Order order);
    
    /**
     * Xử lý IPN callback từ MoMo
     * @param ipnRequest Dữ liệu callback từ MoMo
     * @return true nếu xử lý thành công
     */
    boolean processIpnCallback(MomoIpnRequest ipnRequest);
    
    /**
     * Tạo chữ ký HMAC SHA256
     * @param rawData Chuỗi dữ liệu cần ký
     * @return Chữ ký hex
     */
    String generateSignature(String rawData);
    
    /**
     * Xác thực chữ ký từ MoMo
     * @param rawData Chuỗi dữ liệu
     * @param signature Chữ ký cần kiểm tra
     * @return true nếu hợp lệ
     */
    boolean verifySignature(String rawData, String signature);
}
