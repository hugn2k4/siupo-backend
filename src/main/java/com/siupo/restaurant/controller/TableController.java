package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.CreateTableRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.TableResponse;
import com.siupo.restaurant.service.table.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Slf4j
public class TableController {

    private final TableService tableService;

    /**
     * Lấy danh sách tất cả bàn
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TableResponse>>> getAllTables() {
        List<TableResponse> tables = tableService.getAllTables();
        
        return ResponseEntity.ok(
                ApiResponse.<List<TableResponse>>builder()
                        .code("200")
                        .success(true)
                        .message("Lấy danh sách bàn thành công")
                        .data(tables)
                        .build()
        );
    }

    /**
     * Lấy thông tin bàn theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> getTableById(@PathVariable Long id) {
        TableResponse table = tableService.getTableById(id);
        
        return ResponseEntity.ok(
                ApiResponse.<TableResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Lấy thông tin bàn thành công")
                        .data(table)
                        .build()
        );
    }

    /**
     * Tạo bàn mới (Admin only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TableResponse>> createTable(
            @Valid @RequestBody CreateTableRequest request) {
        
        TableResponse table = tableService.createTable(request.getTableNumber(), request.getSeat());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TableResponse>builder()
                        .code("201")
                        .success(true)
                        .message("Tạo bàn thành công")
                        .data(table)
                        .build());
    }

    /**
     * Cập nhật thông tin bàn (Admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody CreateTableRequest request) {
        
        TableResponse table = tableService.updateTable(id, request.getTableNumber(), request.getSeat());
        
        return ResponseEntity.ok(
                ApiResponse.<TableResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Cập nhật bàn thành công")
                        .data(table)
                        .build()
        );
    }

    /**
     * Xóa bàn (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("200")
                        .success(true)
                        .message("Xóa bàn thành công")
                        .build()
        );
    }

    /**
     * Khởi tạo lại bàn mặc định (Admin only - dùng khi cần reset)
     */
    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse<String>> initializeTables() {
        tableService.initializeDefaultTables();
        
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code("200")
                        .success(true)
                        .message("Khởi tạo bàn thành công")
                        .data("Đã khởi tạo 10 bàn mặc định với mã QR")
                        .build()
        );
    }
}
