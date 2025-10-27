package com.siupo.restaurant.service.PlaceTableForGuest;

import com.siupo.restaurant.dto.request.PlaceTableForGuestRequest;
import com.siupo.restaurant.dto.response.PlaceTableForGuestResponse;
import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.InvalidTimeException;
import com.siupo.restaurant.model.PlaceTableForGuest;
import com.siupo.restaurant.repository.PlaceTableForGuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceTableForGuestServiceImpl implements PlaceTableForGuestService {

    private final PlaceTableForGuestRepository placeTableForGuestRepository;

    @Override
    @Transactional
    public PlaceTableForGuestResponse createPlaceTableRequest(PlaceTableForGuestRequest request) {
        log.info("Creating place table request for guest: {} - Phone: {}",
                request.getFullname(), request.getPhoneNumber());

        // Validate time
        validateStartedTime(request.getStartedAt());

        // Validate member count
        validateMemberCount(request.getMemberInt());

        // Create entity
        PlaceTableForGuest placeTable = PlaceTableForGuest.builder()
                .fullname(request.getFullname().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .memberInt(request.getMemberInt())
                .startedAt(request.getStartedAt())
                .note(request.getNote() != null ? request.getNote().trim() : null)
                .status(EPlaceTableStatus.PENDING)
                .build();

        try {
            PlaceTableForGuest savedTable = placeTableForGuestRepository.save(placeTable);
            log.info("Place table request created successfully with ID: {}", savedTable.getId());

            return PlaceTableForGuestResponse.builder()
                    .id(savedTable.getId())
                    .fullname(savedTable.getFullname())
                    .phoneNumber(savedTable.getPhoneNumber())
                    .email(savedTable.getEmail())
                    .memberInt(savedTable.getMemberInt())
                    .status(savedTable.getStatus())
                    .startedAt(savedTable.getStartedAt())
                    .note(savedTable.getNote())
                    .createdAt(savedTable.getCreatedAt())
                    .message("Yêu cầu đặt bàn đã được gửi, quản lý sẽ liên hệ lại để xác nhận")
                    .build();
        } catch (Exception e) {
            log.error("Error saving place table request: {}", e.getMessage(), e);
            throw new BadRequestException("Không thể gửi yêu cầu, vui lòng thử lại sau");
        }
    }

    private void validateStartedTime(LocalDateTime startedAt) {
        LocalDateTime now = LocalDateTime.now();

        if (startedAt.isBefore(now)) {
            throw new InvalidTimeException("Thời gian đặt bàn phải là thời điểm trong tương lai");
        }

        // Validate booking time is within business hours (8:00 - 22:00)
        int hour = startedAt.getHour();
        if (hour < 8 || hour >= 22) {
            throw new InvalidTimeException("Thời gian đặt bàn phải trong khung giờ hoạt động (8:00 - 22:00)");
        }

        // Optional: Validate advance booking (e.g., at least 1 hour in advance)
        LocalDateTime minimumBookingTime = now.plusHours(1);
        if (startedAt.isBefore(minimumBookingTime)) {
            throw new InvalidTimeException("Vui lòng đặt bàn trước ít nhất 1 giờ");
        }
    }

    private void validateMemberCount(Integer memberInt) {
        if (memberInt == null || memberInt <= 0) {
            throw new BadRequestException("Số lượng khách phải lớn hơn 0");
        }

        if (memberInt > 50) {
            throw new BadRequestException("Số lượng khách không được vượt quá 50 người, vui lòng liên hệ trực tiếp");
        }
    }
}