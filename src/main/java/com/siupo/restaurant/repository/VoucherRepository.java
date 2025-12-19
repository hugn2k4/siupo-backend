package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.EVoucherStatus;
import com.siupo.restaurant.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v WHERE v.status = :status " +
           "AND v.isPublic = true " +
           "AND v.startDate <= :now " +
           "AND v.endDate >= :now " +
           "AND (v.usageLimit = 0 OR v.usedCount < v.usageLimit)")
    List<Voucher> findAvailableVouchers(@Param("status") EVoucherStatus status, 
                                         @Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE v.endDate < :now AND v.status != :expiredStatus")
    List<Voucher> findExpiredVouchers(@Param("now") LocalDateTime now, 
                                       @Param("expiredStatus") EVoucherStatus expiredStatus);
}
