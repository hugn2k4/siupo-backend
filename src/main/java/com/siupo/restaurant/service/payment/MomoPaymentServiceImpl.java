package com.siupo.restaurant.service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siupo.restaurant.config.MomoConfig;
import com.siupo.restaurant.dto.request.MomoIpnRequest;
import com.siupo.restaurant.dto.request.MomoPaymentRequest;
import com.siupo.restaurant.dto.response.MomoPaymentResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.MomoPayment;
import com.siupo.restaurant.model.Order;
import com.siupo.restaurant.repository.OrderRepository;
import com.siupo.restaurant.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentServiceImpl implements MomoPaymentService {

    private final MomoConfig momoConfig;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public MomoPaymentResponse createPayment(Order order) {
        try {
            log.info("Creating MoMo payment for Order #{}", order.getId());
            log.debug("MoMo Config - Endpoint: {}, PartnerCode: {}", momoConfig.getEndpoint(), momoConfig.getPartnerCode());
            
            // Sinh requestId và orderId unique
            String requestId = UUID.randomUUID().toString();
            // Thêm timestamp để tránh trùng orderId khi test nhiều lần
            String orderId = "ORDER_" + order.getId() + "_" + System.currentTimeMillis();

            double exchangeRate = 24000; // Tỷ giá USD to VND
            long amountInVND = Math.round(order.getTotalPrice() * exchangeRate);
            // Tính amount (VND) - đảm bảo >= 1000 VND
            if (amountInVND < 1000) {
                log.warn("Order #{} amount {} VND is less than minimum 1000 VND. Using minimum amount.", order.getId(), amountInVND);
                amountInVND = 1000; // Set minimum amount cho test
            }
            if (amountInVND > 50000000) {
                throw  new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//                throw new BadRequestException("Số tiền vượt quá giới hạn 50,000,000 VND");
            }
            
            String amount = String.valueOf(amountInVND);
            String orderInfo = "Thanh toán đơn hàng #" + order.getId();
            String extraData = "";
            
            log.info("Order #{} - Total price: {} VND", order.getId(), amountInVND);
            String requestType = "captureWallet";
            String lang = "vi";

            // Tạo raw signature
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + momoConfig.getIpnUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + momoConfig.getRedirectUrl() +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            log.debug("MoMo Raw Signature: {}", rawSignature);

            // Ký HMAC SHA256
            String signature = generateSignature(rawSignature);
            log.debug("MoMo Signature: {}", signature);

            // Tạo request body
            MomoPaymentRequest momoRequest = MomoPaymentRequest.builder()
                    .partnerCode(momoConfig.getPartnerCode())
                    .accessKey(momoConfig.getAccessKey())
                    .requestId(requestId)
                    .amount(amount)
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(momoConfig.getRedirectUrl())
                    .ipnUrl(momoConfig.getIpnUrl())
                    .requestType(requestType)
                    .extraData(extraData)
                    .lang(lang)
                    .signature(signature)
                    .build();

            String requestBody = objectMapper.writeValueAsString(momoRequest);
            log.debug("MoMo Request Body: {}", requestBody);

            // Gửi request đến MoMo
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(momoConfig.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("MoMo Response: {}", response.body());

            // Parse response
            MomoPaymentResponse momoResponse = objectMapper.readValue(response.body(), MomoPaymentResponse.class);

            if (momoResponse.getResultCode() != 0) {
                log.error("MoMo payment creation failed: {} - {}", momoResponse.getResultCode(), momoResponse.getMessage());
                throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//                throw new BadRequestException("Tạo thanh toán MoMo thất bại: " + momoResponse.getMessage());
            }

            // Lưu thông tin requestId vào payment
            if (order.getPayment() instanceof MomoPayment momoPayment) {
                momoPayment.setRequestId(Long.parseLong(requestId.replaceAll("[^0-9]", "").substring(0, 10)));
                paymentRepository.save(momoPayment);
            }

            log.info("MoMo payment created successfully for Order #{}", order.getId());
            return momoResponse;

        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Lỗi khi tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean processIpnCallback(MomoIpnRequest ipnRequest) {
        try {
            log.info("Processing MoMo IPN for orderId: {}", ipnRequest.getOrderId());

            // Xác thực signature
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + ipnRequest.getAmount() +
                    "&extraData=" + ipnRequest.getExtraData() +
                    "&message=" + ipnRequest.getMessage() +
                    "&orderId=" + ipnRequest.getOrderId() +
                    "&orderInfo=" + ipnRequest.getOrderInfo() +
                    "&orderType=" + ipnRequest.getOrderType() +
                    "&partnerCode=" + ipnRequest.getPartnerCode() +
                    "&payType=" + ipnRequest.getPayType() +
                    "&requestId=" + ipnRequest.getRequestId() +
                    "&responseTime=" + ipnRequest.getResponseTime() +
                    "&resultCode=" + ipnRequest.getResultCode() +
                    "&transId=" + ipnRequest.getTransId();

            if (!verifySignature(rawSignature, ipnRequest.getSignature())) {
                log.error("Invalid signature from MoMo IPN");
                return false;
            }

            // Tìm order
            String orderIdStr = ipnRequest.getOrderId().replace("ORDER_", "");
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                    .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng"));

            // Kiểm tra resultCode
            if (ipnRequest.getResultCode() == 0) {
                // Thanh toán thành công
                if (order.getPayment() instanceof MomoPayment momoPayment) {
                    momoPayment.setStatus(EPaymentStatus.PAID);
                    momoPayment.setPaymentDate(LocalDateTime.now());
                    momoPayment.setTransactionId(ipnRequest.getTransId());
                    momoPayment.setResultCode(ipnRequest.getResultCode());
                    momoPayment.setPaymentMessage(ipnRequest.getMessage());
                    momoPayment.setPaymentInfo(ipnRequest.getOrderInfo());
                    paymentRepository.save(momoPayment);
                }

                order.setStatus(EOrderStatus.CONFIRMED);
                orderRepository.save(order);

                log.info("MoMo payment successful for Order #{}", orderId);
                return true;
            } else {
                // Thanh toán thất bại
                if (order.getPayment() instanceof MomoPayment momoPayment) {
                    momoPayment.setStatus(EPaymentStatus.FAIL);
                    momoPayment.setResultCode(ipnRequest.getResultCode());
                    momoPayment.setPaymentMessage(ipnRequest.getMessage());
                    paymentRepository.save(momoPayment);
                }

                order.setStatus(EOrderStatus.CANCELED);
                orderRepository.save(order);

                log.warn("MoMo payment failed for Order #{}: {}", orderId, ipnRequest.getMessage());
                return false;
            }

        } catch (Exception e) {
            log.error("Error processing MoMo IPN", e);
            return false;
        }
    }

    @Override
    public String generateSignature(String rawData) {
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, momoConfig.getSecretKey())
                .hmacHex(rawData);
    }

    @Override
    public boolean verifySignature(String rawData, String signature) {
        String expectedSignature = generateSignature(rawData);
        return expectedSignature.equalsIgnoreCase(signature);
    }
}
