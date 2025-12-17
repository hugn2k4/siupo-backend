package com.siupo.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siupo.restaurant.config.MomoConfig;
import com.siupo.restaurant.dto.request.MomoPaymentRequest;
import com.siupo.restaurant.dto.request.PreOrderPaymentRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.MomoPaymentResponse;
import com.siupo.restaurant.dto.response.PreOrderPaymentResponse;
import com.siupo.restaurant.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@RestController
@RequestMapping("/api/pre-order/payment")
@RequiredArgsConstructor
@Slf4j
public class PreOrderPaymentController {

    private final MomoConfig momoConfig;
    private final ObjectMapper objectMapper;

    // Lưu tạm (production dùng Redis)
    private final Map<String, PreOrderPaymentRequest> pendingOrders = new HashMap<>();

    @PostMapping("/momo/create")
    public ResponseEntity<ApiResponse<PreOrderPaymentResponse>> createPreOrderPayment(
            @RequestBody PreOrderPaymentRequest request) {
        try {
            log.info("Creating pre-order MoMo payment - amount: {}", request.getAmount());

            if (request.getAmount() < 1000 || request.getAmount() > 50000000) {
                throw new BadRequestException("Số tiền không hợp lệ (1,000 - 50,000,000 VND)");
            }

            String requestId = UUID.randomUUID().toString();
            String orderId = "PREORDER_" + System.currentTimeMillis();
            String amount = String.valueOf(request.getAmount());
            String orderInfo = request.getDescription() != null ?
                    request.getDescription() : "Đặt bàn - Pre-order";

            // Lưu tạm
            pendingOrders.put(orderId, request);

            // Signature
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" +
                    "&ipnUrl=" + momoConfig.getIpnUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + momoConfig.getRedirectUrl() +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            String signature = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, momoConfig.getSecretKey())
                    .hmacHex(rawSignature);

            MomoPaymentRequest momoRequest = MomoPaymentRequest.builder()
                    .partnerCode(momoConfig.getPartnerCode())
                    .accessKey(momoConfig.getAccessKey())
                    .requestId(requestId)
                    .amount(amount)
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(momoConfig.getRedirectUrl())
                    .ipnUrl(momoConfig.getIpnUrl())
                    .requestType("captureWallet")
                    .extraData("")
                    .lang("vi")
                    .signature(signature)
                    .build();

            String requestBody = objectMapper.writeValueAsString(momoRequest);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(momoConfig.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            MomoPaymentResponse momoResponse = objectMapper.readValue(response.body(), MomoPaymentResponse.class);

            if (momoResponse.getResultCode() != 0) {
                throw new BadRequestException("Tạo thanh toán thất bại: " + momoResponse.getMessage());
            }

            PreOrderPaymentResponse paymentResponse = PreOrderPaymentResponse.builder()
                    .orderId(orderId)
                    .payUrl(momoResponse.getPayUrl())
                    .amount(request.getAmount())
                    .build();

            return ResponseEntity.ok(ApiResponse.<PreOrderPaymentResponse>builder()
                    .code("200")
                    .success(true)
                    .message("Tạo thanh toán thành công")
                    .data(paymentResponse)
                    .build());

        } catch (Exception e) {
            log.error("Error creating pre-order payment", e);
            throw new BadRequestException("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody Map<String, Object> callback) {
        String orderId = (String) callback.get("orderId");
        Integer resultCode = (Integer) callback.get("resultCode");

        PreOrderPaymentRequest orderData = pendingOrders.get(orderId);

        if (resultCode == 0) {
            // TODO: Tạo order thật từ orderData
            log.info("Payment success - Create order for: {}", orderId);
            pendingOrders.remove(orderId);
        } else {
            pendingOrders.remove(orderId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", resultCode == 0 ? "success" : "failed");
        return ResponseEntity.ok(response);
    }
}