package com.siupo.restaurant.service.placeTableForCustomer;

import com.siupo.restaurant.dto.request.PlaceTableForCustomerRequest;
import com.siupo.restaurant.dto.request.PreOrderItemRequest;
import com.siupo.restaurant.dto.response.*;
import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.OrderItemRepository;
import com.siupo.restaurant.repository.PlaceTableForCustomerRepository;
import com.siupo.restaurant.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.siupo.restaurant.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceTableForCustomerServiceImpl implements PlaceTableForCustomerService {

    private final PlaceTableForCustomerRepository placeTableRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    // private final NotificationService notificationService; // Uncomment nếu có service gửi notification

    // Số bàn tối đa của nhà hàng (có thể config trong properties)
    private static final int MAX_TABLES = 20;
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            log.info("No authenticated user - Guest booking");
            return null;
        }

        Object principal = auth.getPrincipal();

        // Nếu User implement UserDetails
        if (principal instanceof User) {
            User user = (User) principal;
            log.info("✅ Authenticated user ID: {}", user.getId());
            return user;
        }

        // Fallback: lấy email và query DB
        String email = principal.toString();
        log.info("Principal email: {}", email);

        return userRepository.findByEmail(email)
                .orElse(null);
    }
    @Override
    @Transactional
    public PlaceTableForCustomerResponse createPlaceTable(PlaceTableForCustomerRequest request) {
        if (request.getStartedAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian đặt bàn phải là thời gian tương lai");
        }

        User authenticatedUser = getAuthenticatedUser();

        PlaceTableForCustomer placeTable = PlaceTableForCustomer.builder()
                .member(request.getMemberInt())
                .status(EPlaceTableStatus.PENDING)
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .user(authenticatedUser)
                .totalPrice(0.0)
                .startedAt(request.getStartedAt())
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        PlaceTableForCustomer savedPlaceTable = placeTableRepository.save(placeTable);

        // Nếu có chọn món trước
        if (request.getPreOrderItems() != null && !request.getPreOrderItems().isEmpty()) {
            for (PreOrderItemRequest preOrderItem : request.getPreOrderItems()) {
                Product product = productRepository.findById(preOrderItem.getProductId())
                        .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                        .orElseThrow(() -> new ResourceNotFoundException(
//                                "Không tìm thấy sản phẩm với ID: " + preOrderItem.getProductId()));

                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .quantity(preOrderItem.getQuantity())
                        .price(preOrderItem.getPrice() != null ?
                                preOrderItem.getPrice() * preOrderItem.getQuantity() :
                                product.getPrice() * preOrderItem.getQuantity())
                        .note(preOrderItem.getNote())
                        .reviewed(false)
                        .placeTable(savedPlaceTable)
                        .build();

                OrderItem savedItem = orderItemRepository.save(orderItem);
                savedPlaceTable.getItems().add(savedItem);
            }

            updateTotalPrice(savedPlaceTable.getId());
        }

        // Gửi thông báo
        try {
            sendBookingConfirmation(savedPlaceTable.getId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo xác nhận đặt bàn", e);
        }

        return mapToResponse(savedPlaceTable);
    }

    @Override
    @Transactional
    public PlaceTableForCustomerResponse updateTotalPrice(Long placeTableId) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(placeTableId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + placeTableId));

        Double totalPrice = placeTable.getItems().stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();

        placeTable.setTotalPrice(totalPrice);
        PlaceTableForCustomer updatedPlaceTable = placeTableRepository.save(placeTable);

        return mapToResponse(updatedPlaceTable);
    }

    @Override
    public void sendBookingConfirmation(Long placeTableId) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(placeTableId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + placeTableId));

        // TODO: Implement notification service
        // notificationService.sendBookingConfirmation(placeTable);

        log.info("Gửi thông báo xác nhận đặt bàn cho user: {} - Đơn: {}",
                placeTable.getUser().getId(), placeTableId);
    }

    private void sendStatusUpdateNotification(PlaceTableForCustomer placeTable) {
        // TODO: Implement notification service
        log.info("Gửi thông báo cập nhật trạng thái cho user: {} - Đơn: {} - Trạng thái: {}",
                placeTable.getUser().getId(), placeTable.getId(), placeTable.getStatus());
    }

    private void sendCancellationNotification(PlaceTableForCustomer placeTable) {
        // TODO: Implement notification service
        log.info("Gửi thông báo hủy đơn cho user: {} - Đơn: {}",
                placeTable.getUser().getId(), placeTable.getId());
    }

    private PlaceTableForCustomerResponse mapToResponse(PlaceTableForCustomer placeTable) {
        return PlaceTableForCustomerResponse.builder()
                .id(placeTable.getId())
                .member(placeTable.getMember())
                .status(placeTable.getStatus())
                .totalPrice(placeTable.getTotalPrice())
                .startedAt(placeTable.getStartedAt())
                .createdAt(placeTable.getCreatedAt())
                .updatedAt(placeTable.getUpdatedAt())
                .note(placeTable.getNote())
                .user(mapToUserSimpleResponse(placeTable.getUser()))
                .items(placeTable.getItems() != null ?
                        placeTable.getItems().stream()
                                .map(this::mapToOrderItemResponse)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .payment(placeTable.getPayment() != null ? mapToPaymentResponse(placeTable.getPayment()) : null)
                .hasPreOrder(placeTable.getItems() != null && !placeTable.getItems().isEmpty())
                .build();
    }

    private UserSimpleResponse mapToUserSimpleResponse(User user) {
        if (user == null) return null;
        return UserSimpleResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullname(user.getFullName())
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .note(item.getNote())
                .reviewed(item.getReviewed())
                .product(mapToProductSimpleResponse(item.getProduct()))
                .createdAt(item.getCreatedAt())
                .build();
    }

    private ProductSimpleResponse mapToProductSimpleResponse(Product product) {
        if (product == null) return null;

        List<ProductImage> productImages = product.getImages();
        String imageUrl = null;
        List<String> imageUrls = new ArrayList<>();

        if (productImages != null && !productImages.isEmpty()) {
            // Lấy URL đầu tiên làm imageUrl chính
            imageUrl = productImages.get(0).getUrl();
            // Convert tất cả ProductImage sang List<String> URLs
            imageUrls = productImages.stream()
                    .map(ProductImage::getUrl)
                    .collect(Collectors.toList());
        }

        return ProductSimpleResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrl(imageUrl)
                .imageUrls(imageUrls)
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .method(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .paidAt(payment.getPaymentDate())
                .build();
    }
}