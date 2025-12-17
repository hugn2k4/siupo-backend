package com.siupo.restaurant.service.tag;

import com.siupo.restaurant.dto.request.TagRequest;
import com.siupo.restaurant.dto.response.TagResponse;
import com.siupo.restaurant.exception.ResourceNotFoundException;
import com.siupo.restaurant.model.ProductTag;
import com.siupo.restaurant.repository.ProductTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private ProductTagRepository productTagRepository;

    @Override
    public List<TagResponse> getAllTags() {
        return productTagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        return mapToResponse(tag);
    }

    @Override
    public TagResponse createTag(TagRequest request) {
        // Check if tag with same name already exists
        productTagRepository.findByName(request.getName())
                .ifPresent(tag -> {
                    throw new RuntimeException("Tag with name '" + request.getName() + "' already exists");
                });

        ProductTag tag = ProductTag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        ProductTag savedTag = productTagRepository.save(tag);
        return mapToResponse(savedTag);
    }

    @Override
    public TagResponse updateTag(Long id, TagRequest request) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));

        // Check if another tag with the same name exists
        productTagRepository.findByName(request.getName())
                .ifPresent(existingTag -> {
                    if (!existingTag.getId().equals(id)) {
                        throw new RuntimeException("Tag with name '" + request.getName() + "' already exists");
                    }
                });

        tag.setName(request.getName());
        tag.setDescription(request.getDescription());

        ProductTag updatedTag = productTagRepository.save(tag);
        return mapToResponse(updatedTag);
    }

    @Override
    public void deleteTag(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + id));
        productTagRepository.delete(tag);
    }

    private TagResponse mapToResponse(ProductTag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}
