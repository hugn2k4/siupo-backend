package com.siupo.restaurant.service.placeTableForGuest;

import com.siupo.restaurant.dto.request.PlaceTableForGuestRequest;
import com.siupo.restaurant.dto.response.PlaceTableForGuestResponse;

public interface PlaceTableForGuestService {
    PlaceTableForGuestResponse createPlaceTableRequest(PlaceTableForGuestRequest request);
}