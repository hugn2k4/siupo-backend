package com.siupo.restaurant.service.combo;

import com.siupo.restaurant.dto.request.ComboItemRequest;
import com.siupo.restaurant.dto.request.CreateComboRequest;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.ResourceNotFoundException;
import com.siupo.restaurant.mapper.ComboMapper;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.ComboItemRepository;
import com.siupo.restaurant.repository.ComboRepository;
import com.siupo.restaurant.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
    public Combo createCombo(CreateComboRequest request) {
        // Create Combo entity
        Combo combo = Combo.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(EProductStatus.AVAILABLE)
                .build();
        
        // Save combo first to get ID
        combo = comboRepository.save(combo);
        
        // Create images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ComboImage> images = new ArrayList<>();
            for (String url : request.getImageUrls()) {
                ComboImage image = ComboImage.builder()
                        .url(url)
                        .combo(combo)
                        .build();
                images.add(image);
            }
            combo.setImages(images);
        }
        
        // Create combo items
        List<ComboItem> items = new ArrayList<>();
        for (ComboItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));
            
            ComboItem item = ComboItem.builder()
                    .combo(combo)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .displayOrder(itemRequest.getDisplayOrder() != null ? itemRequest.getDisplayOrder() : 0)
                    .build();
            
            items.add(item);
        }
        
        // Save all items
        items = comboItemRepository.saveAll(items);
        combo.setItems(items);
        combo = comboRepository.save(combo);
        
        return combo;
    }

    @Override
    @Transactional(readOnly = true)
    public Combo getComboById(Long id) {
        Combo combo = comboRepository.findByIdWithItems(id);
        if (combo == null) {
            throw new ResourceNotFoundException("Không tìm thấy combo với ID: " + id);
        }
        return combo;
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
    public Combo updateCombo(Long id, CreateComboRequest request) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy combo với ID: " + id));
        
        // Update basic info
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setBasePrice(request.getBasePrice());
        
        // Delete old items
        if (combo.getItems() != null) {
            comboItemRepository.deleteAll(combo.getItems());
        }
        
        // Delete old images
        if (combo.getImages() != null) {
            combo.getImages().clear();
        }
        
        // Create new images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ComboImage> images = new ArrayList<>();
            for (String url : request.getImageUrls()) {
                ComboImage image = ComboImage.builder()
                        .url(url)
                        .combo(combo)
                        .build();
                images.add(image);
            }
            combo.setImages(images);
        }
        
        // Create new combo items
        List<ComboItem> items = new ArrayList<>();
        for (ComboItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));
            
            ComboItem item = ComboItem.builder()
                    .combo(combo)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .displayOrder(itemRequest.getDisplayOrder() != null ? itemRequest.getDisplayOrder() : 0)
                    .build();
            
            items.add(item);
        }
        
        items = comboItemRepository.saveAll(items);
        combo.setItems(items);
        combo = comboRepository.save(combo);
        
        return combo;
    }

    @Override
    @Transactional
    public void deleteCombo(Long id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy combo với ID: " + id));
        
        combo.setStatus(EProductStatus.DELETED);
        comboRepository.save(combo);
    }

    @Override
    @Transactional
    public Combo toggleComboStatus(Long id) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy combo với ID: " + id));
        
        // Toggle between AVAILABLE and UNAVAILABLE
        if (combo.getStatus() == EProductStatus.AVAILABLE) {
            combo.setStatus(EProductStatus.UNAVAILABLE);
        } else if (combo.getStatus() == EProductStatus.UNAVAILABLE) {
            combo.setStatus(EProductStatus.AVAILABLE);
        }
        
        combo = comboRepository.save(combo);
        return combo;
    }
}
