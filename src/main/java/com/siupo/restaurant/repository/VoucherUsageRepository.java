package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.Voucher;
import com.siupo.restaurant.model.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    
    List<VoucherUsage> findByVoucher(Voucher voucher);
    
    List<VoucherUsage> findByUser(User user);
    
    List<VoucherUsage> findByVoucherAndUser(Voucher voucher, User user);
    
    long countByVoucherAndUser(Voucher voucher, User user);
    
    boolean existsByVoucherAndUser(Voucher voucher, User user);
}
