package com.siupo.restaurant.service.placeTableForCustomer;

import com.siupo.restaurant.dto.request.PlaceTableRequest;
import com.siupo.restaurant.dto.request.PreOrderItemRequest;
import com.siupo.restaurant.dto.response.*;
import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.exception.ResourceNotFoundException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.OrderItemRepository;
import com.siupo.restaurant.repository.PlaceTableRepository;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.service.placeTableForCustomer.PlaceTableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceTableServiceImpl implements PlaceTableService {

    private final PlaceTableRepository placeTableRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    // private final NotificationService notificationService; // Uncomment nếu có service gửi notification

    // Số bàn tối đa của nhà hàng (có thể config trong properties)
    private static final int MAX_TABLES = 20;

    @Override
    @Transactional
    public PlaceTableResponse createPlaceTable(PlaceTableRequest request) {
        // Validate thời gian đặt bàn
        if (request.getStartedAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian đặt bàn phải là thời gian tương lai");
        }

        // Tạo đơn đặt bàn với thông tin từ form
        PlaceTableForCustomer placeTable = PlaceTableForCustomer.builder()
                .member(request.getMemberInt())
                .status(EPlaceTableStatus.PENDING) // Trạng thái chờ xác nhận
                .user(null) // Không cần user nếu là guest
                .totalPrice(0.0)
                .startedAt(request.getStartedAt())
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        PlaceTableForCustomer savedPlaceTable = placeTableRepository.save(placeTable);

        // Nếu khách chọn món trước, thêm món vào đơn
        if (request.getPreOrderItems() != null && !request.getPreOrderItems().isEmpty()) {
            for (PreOrderItemRequest preOrderItem : request.getPreOrderItems()) {
                Product product = productRepository.findById(preOrderItem.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy sản phẩm với ID: " + preOrderItem.getProductId()));

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

            // Cập nhật tổng tiền
            updateTotalPrice(savedPlaceTable.getId());
        }

        // Gửi thông báo xác nhận
        try {
            sendBookingConfirmation(savedPlaceTable.getId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo xác nhận đặt bàn", e);
        }

        return mapToResponse(savedPlaceTable);
    }

    @Override
    public PlaceTableResponse getPlaceTableById(Long id) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));
        return mapToResponse(placeTable);
    }

    @Override
    public PlaceTableResponse getPlaceTableByIdAndUserId(Long id, Long userId) {
        PlaceTableForCustomer placeTable = placeTableRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn đặt bàn với ID: " + id + " cho người dùng: " + userId));
        return mapToResponse(placeTable);
    }

    @Override
    public List<PlaceTableResponse> getAllPlaceTables() {
        return placeTableRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlaceTableResponse> getPlaceTablesByUserId(Long userId) {
        return placeTableRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlaceTableResponse> getPlaceTablesByStatus(EPlaceTableStatus status) {
        return placeTableRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlaceTableResponse updatePlaceTableStatus(Long id, String status, String note) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));

        try {
            EPlaceTableStatus newStatus = EPlaceTableStatus.valueOf(status.toUpperCase());
            placeTable.setStatus(newStatus);

            if (note != null && !note.isEmpty()) {
                placeTable.setNote(placeTable.getNote() + "\n[Admin] " + note);
            }

            PlaceTableForCustomer updatedPlaceTable = placeTableRepository.save(placeTable);

            // Gửi thông báo cho khách hàng về thay đổi trạng thái
            try {
                sendStatusUpdateNotification(updatedPlaceTable);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo cập nhật trạng thái", e);
            }

            return mapToResponse(updatedPlaceTable);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    @Transactional
    public PlaceTableResponse addItemToPlaceTable(Long placeTableId, Long productId, Long quantity, String note) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(placeTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + placeTableId));

        // Chỉ cho phép thêm món khi đơn đang ở trạng thái PENDING hoặc CONFIRMED
        if (placeTable.getStatus() != EPlaceTableStatus.PENDING &&
                placeTable.getStatus() != EPlaceTableStatus.CONFIRMED) {
            throw new IllegalArgumentException("Không thể thêm món vào đơn đã hoàn tất hoặc đã hủy");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .price(product.getPrice() * quantity)
                .note(note)
                .reviewed(false)
                .placeTable(placeTable)
                .build();

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        placeTable.getItems().add(savedOrderItem);

        updateTotalPrice(placeTableId);

        return mapToResponse(placeTable);
    }

    @Override
    @Transactional
    public PlaceTableResponse removeItemFromPlaceTable(Long placeTableId, Long orderItemId) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(placeTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + placeTableId));

        // Chỉ cho phép xóa món khi đơn đang ở trạng thái PENDING hoặc CONFIRMED
        if (placeTable.getStatus() != EPlaceTableStatus.PENDING &&
                placeTable.getStatus() != EPlaceTableStatus.CONFIRMED) {
            throw new IllegalArgumentException("Không thể xóa món khỏi đơn đã hoàn tất hoặc đã hủy");
        }

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món với ID: " + orderItemId));

        if (!orderItem.getPlaceTable().getId().equals(placeTableId)) {
            throw new IllegalArgumentException("Món này không thuộc đơn đặt bàn đã chọn");
        }

        placeTable.getItems().remove(orderItem);
        orderItemRepository.delete(orderItem);

        updateTotalPrice(placeTableId);

        return mapToResponse(placeTable);
    }

    @Override
    @Transactional
    public void cancelPlaceTable(Long id) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));

        // Chỉ cho phép hủy khi đơn đang ở trạng thái PENDING hoặc CONFIRMED
        if (placeTable.getStatus() != EPlaceTableStatus.PENDING &&
                placeTable.getStatus() != EPlaceTableStatus.CONFIRMED) {
            throw new IllegalArgumentException("Không thể hủy đơn đã hoàn tất hoặc đã hủy");
        }

        placeTable.setStatus(EPlaceTableStatus.DENIED);
        placeTableRepository.save(placeTable);

        // Gửi thông báo hủy đơn
        try {
            sendCancellationNotification(placeTable);
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo hủy đơn", e);
        }
    }

    @Override
    @Transactional
    public void deletePlaceTable(Long id) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));
        placeTableRepository.delete(placeTable);
    }

    @Override
    public List<PlaceTableResponse> getPlaceTablesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return placeTableRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long countPlaceTablesByStatus(EPlaceTableStatus status) {
        return placeTableRepository.countByStatus(status);
    }

    @Override
    @Transactional
    public PlaceTableResponse updateTotalPrice(Long placeTableId) {
        PlaceTableForCustomer placeTable = placeTableRepository.findById(placeTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + placeTableId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + placeTableId));

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

    private PlaceTableResponse mapToResponse(PlaceTableForCustomer placeTable) {
        return PlaceTableResponse.builder()
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