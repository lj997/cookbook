package com.cookbook.controller;

import com.cookbook.payload.ApiResponse;
import com.cookbook.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请选择要上传的文件"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("只支持上传图片文件"));
        }

        try {
            String fileUrl = fileService.storeFile(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(ApiResponse.success("文件上传成功", result));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("文件上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) {
        
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("请选择要上传的文件"));
        }

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                failCount++;
                continue;
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                failCount++;
                continue;
            }

            try {
                String fileUrl = fileService.storeFile(file);
                result.put(file.getOriginalFilename(), fileUrl);
                successCount++;
            } catch (IOException e) {
                failCount++;
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);

        if (successCount == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("所有文件上传失败", result));
        }

        return ResponseEntity.ok(ApiResponse.success("成功上传 " + successCount + " 个文件", result));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            fileService.deleteFile(fileUrl);
            return ResponseEntity.ok(ApiResponse.success("文件删除成功", null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("文件删除失败: " + e.getMessage()));
        }
    }
}
