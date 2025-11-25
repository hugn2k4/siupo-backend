package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Address;
import com.siupo.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    // Lấy tất cả địa chỉ của user
    List<Address> findByUser(User user);

    // Đếm số địa chỉ của user
    long countByUserId(Long userId);

    // === TÌM ĐỊA CHỈ THEO TOÀN BỘ NỘI DUNG (natural key) ===
    @Query("""
        SELECT a FROM Address a WHERE 
        a.user = :user 
        AND a.address = :addressLine 
        AND a.ward = :ward 
        AND a.district = :district 
        AND a.province = :province 
        AND a.receiverName = :receiverName 
        AND a.receiverPhone = :receiverPhone
        """)
    Optional<Address> findByUserAndAllFields(
            @Param("user") User user,
            @Param("addressLine") String addressLine,
            @Param("ward") String ward,
            @Param("district") String district,
            @Param("province") String province,
            @Param("receiverName") String receiverName,
            @Param("receiverPhone") String receiverPhone
    );
}
