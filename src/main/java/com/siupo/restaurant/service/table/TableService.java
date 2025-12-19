package com.siupo.restaurant.service.table;

import com.siupo.restaurant.dto.response.TableResponse;

import java.util.List;

public interface TableService {
    
    /**
     * Khởi tạo bàn mặc định (10 bàn) nếu chưa có
     */
    void initializeDefaultTables();
    
    /**
     * Lấy tất cả bàn
     */
    List<TableResponse> getAllTables();
    
    /**
     * Lấy thông tin bàn theo ID
     */
    TableResponse getTableById(Long id);
    
    /**
     * Tạo bàn mới với mã QR
     */
    TableResponse createTable(String tableNumber, Integer seat);
    
    /**
     * Cập nhật thông tin bàn
     */
    TableResponse updateTable(Long id, String tableNumber, Integer seat);
    
    /**
     * Xóa bàn
     */
    void deleteTable(Long id);
}
