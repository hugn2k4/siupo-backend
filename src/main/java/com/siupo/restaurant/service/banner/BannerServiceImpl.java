package com.siupo.restaurant.service.banner;

import com.siupo.restaurant.dto.request.BannerRequest;
import com.siupo.restaurant.dto.response.BannerResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.Banner;
import com.siupo.restaurant.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public List<BannerResponse> getAllBanners() {
        return bannerRepository.findAllByOrderByPositionAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BannerResponse getBannerById(Long id) {
        Banner banner = findBannerById(id);
        return mapToResponse(banner);
    }

    @Override
    @Transactional
    public BannerResponse createBanner(BannerRequest request) {
        Banner banner = Banner.builder()
                .name(request.getName())
                .url(request.getUrl())
                .position(request.getPosition())
                .build();

        Banner savedBanner = bannerRepository.save(banner);
        return mapToResponse(savedBanner);
    }

    @Override
    @Transactional
    public BannerResponse updateBanner(Long id, BannerRequest request) {
        Banner banner = findBannerById(id);

        banner.setName(request.getName());
        banner.setUrl(request.getUrl());
        banner.setPosition(request.getPosition());

        Banner updatedBanner = bannerRepository.save(banner);
        return mapToResponse(updatedBanner);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = findBannerById(id);
        bannerRepository.delete(banner);
    }


    private Banner findBannerById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Banner không tồn tại với ID: " + id));
    }

    private BannerResponse mapToResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .name(banner.getName())
                .url(banner.getUrl())
                .position(banner.getPosition())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}