package com.siupo.restaurant.controller;

import com.siupo.restaurant.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/single")
    public ResponseEntity<String> uploadSingle(@RequestParam("file") MultipartFile file) throws IOException {
        String url = cloudinaryService.uploadImage(file);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<String>> uploadMultiple(@RequestParam("files") List<MultipartFile> files) throws IOException {
        List<String> urls = cloudinaryService.uploadImages(files);
        return ResponseEntity.ok(urls);
    }
}
