package com.siupo.restaurant.service.placetableforguest;

import com.siupo.restaurant.dto.request.PlaceTableForGuestRequest;
import com.siupo.restaurant.dto.response.PlaceTableForGuestResponse;

public interface PlaceTableForGuestService {

    /**
     * Create a new place table request from guest
     * @param request the place table request data
     * @return PlaceTableForGuestResponse with created information
     */
    PlaceTableForGuestResponse createPlaceTableRequest(PlaceTableForGuestRequest request);
}