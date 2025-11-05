package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.model.PlaceTableForCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceTableRepository extends JpaRepository<PlaceTableForCustomer, Long> {

    // Tìm tất cả đơn đặt bàn của user
    List<PlaceTableForCustomer> findByUserId(Long userId);

    // Tìm đơn đặt bàn theo trạng thái
    List<PlaceTableForCustomer> findByStatus(EPlaceTableStatus status);

    // Tìm đơn đặt bàn theo ID và userId (để khách hàng chỉ xem được đơn của mình)
    Optional<PlaceTableForCustomer> findByIdAndUserId(Long id, Long userId);

    // Tìm đơn đặt bàn của user theo trạng thái
    @Query("SELECT p FROM PlaceTableForCustomer p WHERE p.user.id = :userId AND p.status = :status")
    List<PlaceTableForCustomer> findByUserIdAndStatus(@Param("userId") Long userId,
                                                      @Param("status") EPlaceTableStatus status);

    // Tìm đơn đặt bàn trong khoảng thời gian
    @Query("SELECT p FROM PlaceTableForCustomer p WHERE p.startedAt BETWEEN :startDate AND :endDate")
    List<PlaceTableForCustomer> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // Đếm số đơn đặt bàn theo trạng thái
    @Query("SELECT COUNT(p) FROM PlaceTableForCustomer p WHERE p.status = :status")
    Long countByStatus(@Param("status") EPlaceTableStatus status);

    // Đếm số bàn đã được đặt trong khoảng thời gian (để check bàn trống)
    @Query("SELECT COUNT(p) FROM PlaceTableForCustomer p " +
            "WHERE p.startedAt BETWEEN :startRange AND :endRange " +
            "AND p.status IN :statuses")
    Long countBookedTablesInTimeRange(@Param("startRange") LocalDateTime startRange,
                                      @Param("endRange") LocalDateTime endRange,
                                      @Param("statuses") List<EPlaceTableStatus> statuses);

    // Tìm các đơn đặt bàn sắp tới của user
    @Query("SELECT p FROM PlaceTableForCustomer p " +
            "WHERE p.user.id = :userId " +
            "AND p.startedAt > :now " +
            "AND p.status IN ('PENDING', 'CONFIRMED') " +
            "ORDER BY p.startedAt ASC")
    List<PlaceTableForCustomer> findUpcomingBookingsByUserId(@Param("userId") Long userId,
                                                             @Param("now") LocalDateTime now);

    // Tìm các đơn đặt bàn trong ngày hôm nay
    @Query("SELECT p FROM PlaceTableForCustomer p " +
            "WHERE DATE(p.startedAt) = DATE(:date) " +
            "ORDER BY p.startedAt ASC")
    List<PlaceTableForCustomer> findBookingsByDate(@Param("date") LocalDateTime date);
}