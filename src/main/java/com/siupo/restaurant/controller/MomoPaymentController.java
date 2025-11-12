package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.MomoIpnRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.service.payment.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/momo")
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentController {

    private final MomoPaymentService momoPaymentService;

    /**
     * Endpoint nhận IPN callback từ MoMo
     * MoMo sẽ gọi endpoint này sau khi user thanh toán
     */
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> handleIpnCallback(@RequestBody MomoIpnRequest ipnRequest) {
        log.info("Received MoMo IPN callback for orderId: {}", ipnRequest.getOrderId());
        log.debug("IPN Request: {}", ipnRequest);

        boolean isSuccess = momoPaymentService.processIpnCallback(ipnRequest);

        Map<String, Object> response = new HashMap<>();
        if (isSuccess) {
            response.put("status", "success");
            response.put("message", "IPN processed successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "IPN processing failed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Endpoint để test IPN callback (dùng khi test local)
     */
    @GetMapping("/ipn/test")
    public ResponseEntity<ApiResponse<String>> testIpn() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code("200")
                        .success(true)
                        .message("MoMo IPN endpoint is working")
                        .data("OK")
                        .build()
        );
    }

    /**
     * Endpoint nhận return callback khi user hoàn tất/hủy thanh toán
     * Frontend sẽ nhận params này và hiển thị kết quả
     */
    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> handleReturnCallback(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String resultCode,
            @RequestParam(required = false) String message) {
        
        log.info("Received MoMo return callback - orderId: {}, resultCode: {}, message: {}", 
                orderId, resultCode, message);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("resultCode", resultCode);
        response.put("message", message);
        response.put("success", "0".equals(resultCode));

        return ResponseEntity.ok(response);
    }
}
