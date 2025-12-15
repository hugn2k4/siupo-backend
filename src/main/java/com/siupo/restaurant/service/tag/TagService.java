package com.siupo.restaurant.service.tag;

import com.siupo.restaurant.dto.request.TagRequest;
import com.siupo.restaurant.dto.response.TagResponse;

import java.util.List;

public interface TagService {
    List<TagResponse> getAllTags();
    TagResponse getTagById(Long id);
    TagResponse createTag(TagRequest request);
    TagResponse updateTag(Long id, TagRequest request);
    void deleteTag(Long id);
}
