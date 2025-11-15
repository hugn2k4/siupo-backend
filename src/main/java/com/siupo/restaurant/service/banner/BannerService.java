package com.siupo.restaurant.service.banner;

import com.siupo.restaurant.dto.request.BannerRequest;
import com.siupo.restaurant.dto.response.BannerResponse;

import java.util.List;

public interface BannerService {
    List<BannerResponse> getAllBanners();
    BannerResponse getBannerById(Long id);
    BannerResponse createBanner(BannerRequest request);
    BannerResponse updateBanner(Long id, BannerRequest request);
    void deleteBanner(Long id);
}