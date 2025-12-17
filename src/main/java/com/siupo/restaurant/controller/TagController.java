package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.TagRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.TagResponse;
import com.siupo.restaurant.model.ProductTag;
import com.siupo.restaurant.service.tag.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        ApiResponse<List<TagResponse>> response = ApiResponse.<List<TagResponse>>builder()
                .success(true)
                .code("200")
                .message("Tags retrieved successfully")
                .data(tags)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(@PathVariable Long id) {
        TagResponse tag = tagService.getTagById(id);
        ApiResponse<TagResponse> response = ApiResponse.<TagResponse>builder()
                .success(true)
                .code("200")
                .message("Tag retrieved successfully")
                .data(tag)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@RequestBody TagRequest request) {
        TagResponse tag = tagService.createTag(request);
        ApiResponse<TagResponse> response = ApiResponse.<TagResponse>builder()
                .success(true)
                .code("201")
                .message("Tag created successfully")
                .data(tag)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(@PathVariable Long id, @RequestBody TagRequest request) {
        TagResponse tag = tagService.updateTag(id, request);
        ApiResponse<TagResponse> response = ApiResponse.<TagResponse>builder()
                .success(true)
                .code("200")
                .message("Tag updated successfully")
                .data(tag)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Tag deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
