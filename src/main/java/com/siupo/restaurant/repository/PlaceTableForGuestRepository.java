package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.model.PlaceTableForGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlaceTableForGuestRepository extends JpaRepository<PlaceTableForGuest, Long> {

    List<PlaceTableForGuest> findByPhoneNumber(String phoneNumber);

    List<PlaceTableForGuest> findByStatus(EPlaceTableStatus status);

    List<PlaceTableForGuest> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);
}