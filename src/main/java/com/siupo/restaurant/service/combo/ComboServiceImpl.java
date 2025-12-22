package com.siupo.restaurant.service.combo;

import com.siupo.restaurant.dto.request.ComboItemRequest;
import com.siupo.restaurant.dto.request.CreateComboRequest;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.ResourceNotFoundException;
import com.siupo.restaurant.mapper.ComboMapper;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.ComboItemRepository;
import com.siupo.restaurant.repository.ComboRepository;
import com.siupo.restaurant.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {
    private final ComboRepository comboRepository;
    private final ComboItemRepository comboItemRepository;
    private final ProductRepository productRepository;
    private final ComboMapper comboMapper;

    @Override
    @Transactional
    public ComboResponse createCombo(CreateComboRequest request) {
        Combo comboInit = comboMapper.toEntity(request);
        Combo comboToUse = comboRepository.save(comboInit);
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ComboImage> images = request.getImageUrls().stream()
                    .map(url -> ComboImage.builder()
                            .url(url)
                            .combo(comboToUse)
                            .build())
                    .collect(Collectors.toList());
            comboToUse.setImages(images);
        }
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<Long> productIds = request.getItems().stream()
                    .map(ComboItemRequest::getProductId)
                    .collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(productIds);
            if (products.size() != productIds.stream().distinct().count()) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            final Combo finalComboRef = comboToUse;
            List<ComboItem> comboItems = request.getItems().stream()
                    .map(itemRequest -> {
                        Product product = productMap.get(itemRequest.getProductId());
                        return ComboItem.builder()
                                .combo(finalComboRef)
                                .product(product)
                                .quantity(itemRequest.getQuantity())
                                .displayOrder(itemRequest.getDisplayOrder() != null ? itemRequest.getDisplayOrder() : 0)
                                .build();
                    })
                    .collect(Collectors.toList());
            comboToUse.setItems(comboItems);
        }
        Combo savedCombo = comboRepository.save(comboToUse);
        return comboMapper.toResponse(savedCombo);
    }

    @Override
    @Transactional(readOnly = true)
    public ComboResponse getComboById(Long id) {
        Combo combo = comboRepository.findByIdWithItems(id);
        if (combo == null) {
            throw new ResourceNotFoundException(ErrorCode.COMBO_NOT_FOUND);
        }
        return comboMapper.toResponse(combo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboResponse> getAllCombos() {
        List<Combo> combos = comboRepository.findAll();
        return combos.stream()
                .filter(combo -> combo.getStatus() != EProductStatus.DELETED)
                .map(comboMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboResponse> getAvailableCombos() {
        List<Combo> combos = comboRepository.findByStatus(EProductStatus.AVAILABLE);
        return combos.stream()
                .map(comboMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComboResponse updateCombo(Long id, CreateComboRequest request) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMBO_NOT_FOUND));
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setBasePrice(request.getBasePrice());
        if (request.getImageUrls() != null) {
            combo.getImages().clear();
            // Thêm ảnh mới
            if (!request.getImageUrls().isEmpty()) {
                List<ComboImage> newImages = request.getImageUrls().stream()
                        .map(url -> ComboImage.builder()
                                .url(url)
                                .combo(combo)
                                .build())
                        .collect(Collectors.toList());
                combo.getImages().addAll(newImages);
            }
        }
        // 4. Xử lý Items
        if (request.getItems() != null) {
            // 4.1 Lấy danh sách ID
            List<Long> productIds = request.getItems().stream()
                    .map(ComboItemRequest::getProductId)
                    .collect(Collectors.toList());
            // 4.2 Query 1 lần lấy hết Product
            List<Product> products = productRepository.findAllById(productIds);
            // Validate: Nếu thiếu product nào đó
            if (products.size() != productIds.stream().distinct().count()) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));
            // 4.3 Xóa items cũ
            combo.getItems().clear();
            // 4.4 Thêm items mới
            List<ComboItem> newItems = request.getItems().stream()
                    .map(req -> ComboItem.builder()
                            .combo(combo)
                            .product(productMap.get(req.getProductId()))
                            .quantity(req.getQuantity())
                            .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0)
                            .build())
                    .collect(Collectors.toList());
            combo.getItems().addAll(newItems);
        }
        // 5. Save Combo
        return comboMapper.toResponse(comboRepository.save(combo));
    }

    @Override
    @Transactional
    public void deleteCombo(Long id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMBO_NOT_FOUND));
        combo.setStatus(EProductStatus.DELETED);
        comboRepository.save(combo);
    }

    @Override
    @Transactional
    public ComboResponse toggleComboStatus(Long id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMBO_NOT_FOUND));
        if (combo.getStatus() == EProductStatus.DELETED) {
            throw new BadRequestException(ErrorCode.CANNOT_UPDATE_DELETED_COMBO);
        }
        combo.setStatus(combo.getStatus() == EProductStatus.AVAILABLE
                ? EProductStatus.UNAVAILABLE
                : EProductStatus.AVAILABLE);
        return comboMapper.toResponse(comboRepository.save(combo));
    }
}
