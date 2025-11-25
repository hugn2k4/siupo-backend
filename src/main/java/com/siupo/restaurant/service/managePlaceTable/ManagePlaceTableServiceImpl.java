package com.siupo.restaurant.service.managePlaceTable;

import com.siupo.restaurant.dto.response.*;
import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.exception.ResourceNotFoundException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.PlaceTableForCustomerRepository;
import com.siupo.restaurant.repository.PlaceTableForGuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagePlaceTableServiceImpl implements ManagePlaceTableService {

    private final PlaceTableForCustomerRepository customerRepository;
    private final PlaceTableForGuestRepository guestRepository;

    // ============= CUSTOMER BOOKINGS =============

    @Override
    public List<PlaceTableForCustomerResponse> getAllCustomerBookings() {
        return customerRepository.findAll().stream()
                .map(this::mapCustomerToResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // Sắp xếp mới nhất trước
                .collect(Collectors.toList());
    }

    @Override
    public PlaceTableForCustomerResponse getCustomerBookingById(Long id) {
        PlaceTableForCustomer booking = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));
        return mapCustomerToResponse(booking);
    }

    @Override
    public List<PlaceTableForCustomerResponse> getCustomerBookingsByStatus(String status) {
        EPlaceTableStatus tableStatus = EPlaceTableStatus.valueOf(status.toUpperCase());
        return customerRepository.findByStatus(tableStatus).stream()
                .map(this::mapCustomerToResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlaceTableForCustomerResponse> getCustomerBookingsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        return customerRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapCustomerToResponse)
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlaceTableForCustomerResponse confirmCustomerBooking(Long id, String note) {
        return updateCustomerStatus(id, EPlaceTableStatus.CONFIRMED, note);
    }

    @Override
    @Transactional
    public PlaceTableForCustomerResponse denyCustomerBooking(Long id, String note) {
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
        }
        return updateCustomerStatus(id, EPlaceTableStatus.DENIED, note);
    }

    @Override
    @Transactional
    public PlaceTableForCustomerResponse completeCustomerBooking(Long id, String note) {
        return updateCustomerStatus(id, EPlaceTableStatus.COMPLETED, note);
    }

    @Override
    @Transactional
    public PlaceTableForCustomerResponse updateCustomerBookingStatus(Long id, String status, String note) {
        try {
            EPlaceTableStatus newStatus = EPlaceTableStatus.valueOf(status.toUpperCase());

            // Validate note for DENIED status
            if (newStatus == EPlaceTableStatus.DENIED && (note == null || note.trim().isEmpty())) {
                throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
            }

            return updateCustomerStatus(id, newStatus, note);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    public Long countCustomerBookingsByStatus(String status) {
        EPlaceTableStatus tableStatus = EPlaceTableStatus.valueOf(status.toUpperCase());
        return customerRepository.countByStatus(tableStatus);
    }

    @Override
    public void resendCustomerConfirmation(Long id) {
        PlaceTableForCustomer booking = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));

        // TODO: Implement notification service
        log.info("Resend confirmation for customer booking ID: {} - User: {}", id, booking.getUser().getId());
    }

    // ============= GUEST BOOKINGS =============

    @Override
    public List<PlaceTableForGuestResponse> getAllGuestBookings() {
        return guestRepository.findAll().stream()
                .map(this::mapGuestToResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public PlaceTableForGuestResponse getGuestBookingById(Long id) {
        PlaceTableForGuest booking = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu đặt bàn với ID: " + id));
        return mapGuestToResponse(booking);
    }

    @Override
    public List<PlaceTableForGuestResponse> getGuestBookingsByStatus(String status) {
        EPlaceTableStatus tableStatus = EPlaceTableStatus.valueOf(status.toUpperCase());
        return guestRepository.findByStatus(tableStatus).stream()
                .map(this::mapGuestToResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlaceTableForGuestResponse> getGuestBookingsByPhone(String phoneNumber) {
        return guestRepository.findByPhoneNumber(phoneNumber).stream()
                .map(this::mapGuestToResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlaceTableForGuestResponse> getGuestBookingsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        return guestRepository.findByStartedAtBetween(startDate, endDate).stream()
                .map(this::mapGuestToResponse)
                .sorted((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PlaceTableForGuestResponse confirmGuestBooking(Long id, String note) {
        return updateGuestStatus(id, EPlaceTableStatus.CONFIRMED, note);
    }

    @Override
    @Transactional
    public PlaceTableForGuestResponse denyGuestBooking(Long id, String note) {
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
        }
        return updateGuestStatus(id, EPlaceTableStatus.DENIED, note);
    }

    @Override
    @Transactional
    public PlaceTableForGuestResponse completeGuestBooking(Long id, String note) {
        return updateGuestStatus(id, EPlaceTableStatus.COMPLETED, note);
    }

    @Override
    @Transactional
    public PlaceTableForGuestResponse updateGuestBookingStatus(Long id, String status, String note) {
        try {
            EPlaceTableStatus newStatus = EPlaceTableStatus.valueOf(status.toUpperCase());

            // Validate note for DENIED status
            if (newStatus == EPlaceTableStatus.DENIED && (note == null || note.trim().isEmpty())) {
                throw new IllegalArgumentException("Vui lòng nhập lý do từ chối");
            }

            return updateGuestStatus(id, newStatus, note);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
    }

    // ============= STATISTICS =============

    @Override
    public Map<String, Object> getBookingStatistics() {
        Long customerPending = customerRepository.countByStatus(EPlaceTableStatus.PENDING);
        Long customerConfirmed = customerRepository.countByStatus(EPlaceTableStatus.CONFIRMED);
        Long customerDenied = customerRepository.countByStatus(EPlaceTableStatus.DENIED);
        Long customerCompleted = customerRepository.countByStatus(EPlaceTableStatus.COMPLETED);

        List<PlaceTableForGuest> allGuests = guestRepository.findAll();
        Long guestPending = allGuests.stream().filter(g -> g.getStatus() == EPlaceTableStatus.PENDING).count();
        Long guestConfirmed = allGuests.stream().filter(g -> g.getStatus() == EPlaceTableStatus.CONFIRMED).count();
        Long guestDenied = allGuests.stream().filter(g -> g.getStatus() == EPlaceTableStatus.DENIED).count();
        Long guestCompleted = allGuests.stream().filter(g -> g.getStatus() == EPlaceTableStatus.COMPLETED).count();

        return Map.of(
                "customer", Map.of(
                        "pending", customerPending,
                        "confirmed", customerConfirmed,
                        "denied", customerDenied,
                        "completed", customerCompleted,
                        "total", customerPending + customerConfirmed + customerDenied + customerCompleted
                ),
                "guest", Map.of(
                        "pending", guestPending,
                        "confirmed", guestConfirmed,
                        "denied", guestDenied,
                        "completed", guestCompleted,
                        "total", guestPending + guestConfirmed + guestDenied + guestCompleted
                ),
                "total", Map.of(
                        "pending", customerPending + guestPending,
                        "confirmed", customerConfirmed + guestConfirmed,
                        "denied", customerDenied + guestDenied,
                        "completed", customerCompleted + guestCompleted,
                        "all", customerPending + customerConfirmed + customerDenied + customerCompleted +
                                guestPending + guestConfirmed + guestDenied + guestCompleted
                )
        );
    }

    @Override
    public Map<String, Object> getTodayBookings() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<PlaceTableForCustomerResponse> customerBookings =
                customerRepository.findByDateRange(startOfDay, endOfDay).stream()
                        .map(this::mapCustomerToResponse)
                        .sorted((a, b) -> a.getStartedAt().compareTo(b.getStartedAt()))
                        .collect(Collectors.toList());

        List<PlaceTableForGuestResponse> guestBookings =
                guestRepository.findByStartedAtBetween(startOfDay, endOfDay).stream()
                        .map(this::mapGuestToResponse)
                        .sorted((a, b) -> a.getStartedAt().compareTo(b.getStartedAt()))
                        .collect(Collectors.toList());

        return Map.of(
                "date", startOfDay.toLocalDate(),
                "customer", customerBookings,
                "guest", guestBookings,
                "totalCustomer", customerBookings.size(),
                "totalGuest", guestBookings.size(),
                "total", customerBookings.size() + guestBookings.size()
        );
    }

    // ============= PRIVATE HELPER METHODS =============

    private PlaceTableForCustomerResponse updateCustomerStatus(Long id, EPlaceTableStatus status, String note) {
        PlaceTableForCustomer booking = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt bàn với ID: " + id));

        booking.setStatus(status);

        if (note != null && !note.trim().isEmpty()) {
            String currentNote = booking.getNote() != null ? booking.getNote() : "";
            String timestamp = LocalDateTime.now().toString();
            booking.setNote(currentNote + (currentNote.isEmpty() ? "" : "\n") +
                    "[Admin - " + timestamp + "] " + note);
        }

        PlaceTableForCustomer updated = customerRepository.save(booking);
        log.info("Updated customer booking {} to status: {}", id, status);

        // TODO: Send notification to customer
        return mapCustomerToResponse(updated);
    }

    private PlaceTableForGuestResponse updateGuestStatus(Long id, EPlaceTableStatus status, String note) {
        PlaceTableForGuest booking = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu đặt bàn với ID: " + id));

        booking.setStatus(status);

        if (note != null && !note.trim().isEmpty()) {
            String currentNote = booking.getNote() != null ? booking.getNote() : "";
            String timestamp = LocalDateTime.now().toString();
            booking.setNote(currentNote + (currentNote.isEmpty() ? "" : "\n") +
                    "[Admin - " + timestamp + "] " + note);
        }

        PlaceTableForGuest updated = guestRepository.save(booking);
        log.info("Updated guest booking {} to status: {}", id, status);

        // TODO: Send notification to guest (SMS/Email)
        return mapGuestToResponse(updated);
    }

    private PlaceTableForCustomerResponse mapCustomerToResponse(PlaceTableForCustomer booking) {

        return PlaceTableForCustomerResponse.builder()
                .id(booking.getId())
                .member(booking.getMember())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .phoneNumber(booking.getPhoneNumber())
                .startedAt(booking.getStartedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .note(booking.getNote())
                .user(mapToUserSimpleResponse(booking.getUser()))
                .items(booking.getItems() != null ?
                        booking.getItems().stream()
                                .map(this::mapToOrderItemResponse)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .payment(booking.getPayment() != null ? mapToPaymentResponse(booking.getPayment()) : null)
                .hasPreOrder(booking.getItems() != null && !booking.getItems().isEmpty())
                .build();
    }

    private PlaceTableForGuestResponse mapGuestToResponse(PlaceTableForGuest booking) {
        return PlaceTableForGuestResponse.builder()
                .id(booking.getId())
                .fullname(booking.getFullname())
                .phoneNumber(booking.getPhoneNumber())
                .email(booking.getEmail())
                .memberInt(booking.getMemberInt())
                .status(booking.getStatus())
                .startedAt(booking.getStartedAt())
                .note(booking.getNote())
                .createdAt(booking.getCreatedAt())
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
            imageUrl = productImages.get(0).getUrl();
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