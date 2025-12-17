package com.siupo.restaurant.service.tag;

import com.siupo.restaurant.dto.request.TagRequest;
import com.siupo.restaurant.dto.response.TagResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.ResourceNotFoundException;
import com.siupo.restaurant.mapper.TagMapper;
import com.siupo.restaurant.model.ProductTag;
import com.siupo.restaurant.repository.ProductTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final ProductTagRepository productTagRepository;
    private final TagMapper tagMapper ;

    @Override
    public List<TagResponse> getAllTags() {
        return productTagRepository.findAll().stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.TAG_NOT_FOUND));
        return tagMapper.toResponse(tag);
    }

    @Override
    public TagResponse createTag(TagRequest request) {
        productTagRepository.findByName(request.getName())
                .ifPresent(tag -> {
                    throw new BadRequestException(ErrorCode.TAG_ALREADY_EXISTS);
                });
        ProductTag tag = ProductTag.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        ProductTag savedTag = productTagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Override
    public TagResponse updateTag(Long id, TagRequest request) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.TAG_NOT_FOUND));
        productTagRepository.findByName(request.getName())
                .ifPresent(existingTag -> {
                    if (!existingTag.getId().equals(id)) {
                        throw new BadRequestException(ErrorCode.TAG_ALREADY_EXISTS);
                    }
                });
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());
        ProductTag updatedTag = productTagRepository.save(tag);
        return tagMapper.toResponse(updatedTag);
    }

    @Override
    public void deleteTag(Long id) {
        ProductTag tag = productTagRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.TAG_NOT_FOUND));
        productTagRepository.delete(tag);
    }
}
