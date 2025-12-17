package com.siupo.restaurant.service.table;

import com.siupo.restaurant.dto.response.TableResponse;
import com.siupo.restaurant.exception.NotFoundException;
import com.siupo.restaurant.exception.ResourceNotFoundException;
import com.siupo.restaurant.model.TableEntity;
import com.siupo.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableServiceImpl implements TableService {

    private final TableRepository tableRepository;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultTables() {
        long count = tableRepository.count();
        
        if (count == 0) {
            log.info("Initializing 10 default tables with QR codes...");
            
            for (int i = 1; i <= 10; i++) {
                String tableNumber = "Bàn " + i;
                Integer seats = (i <= 5) ? 4 : 6; // Bàn 1-5: 4 chỗ, Bàn 6-10: 6 chỗ
                String qrCode = generateQRCode(i);
                
                TableEntity table = TableEntity.builder()
                        .tableNumber(tableNumber)
                        .seat(seats)
                        .qr(qrCode)
                        .build();
                
                tableRepository.save(table);
                log.info("Created table: {} with {} seats - QR: {}", tableNumber, seats, qrCode);
            }
            
            log.info("Successfully initialized 10 tables");
        } else {
            log.info("Tables already exist ({}), skipping initialization", count);
        }
    }

    @Override
    public List<TableResponse> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TableResponse getTableById(Long id) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + id));
        return mapToResponse(table);
    }

    @Override
    @Transactional
    public TableResponse createTable(String tableNumber, Integer seat) {
        String qrCode = generateQRCode(null);
        
        TableEntity table = TableEntity.builder()
                .tableNumber(tableNumber)
                .seat(seat)
                .qr(qrCode)
                .build();
        
        TableEntity saved = tableRepository.save(table);
        log.info("Created new table: {} - QR: {}", tableNumber, qrCode);
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TableResponse updateTable(Long id, String tableNumber, Integer seat) {
        TableEntity table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + id));
        
        if (tableNumber != null && !tableNumber.trim().isEmpty()) {
            table.setTableNumber(tableNumber);
        }
        
        if (seat != null && seat > 0) {
            table.setSeat(seat);
        }
        
        TableEntity updated = tableRepository.save(table);
        log.info("Updated table ID {}: {} - {} seats", id, tableNumber, seat);
        
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        if (!tableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bàn với ID: " + id);
        }
        
        tableRepository.deleteById(id);
        log.info("Deleted table ID: {}", id);
    }

    // ===== HELPER METHODS =====

    /**
     * Tạo mã QR cho bàn
     * Format: TABLE_{id}_{uuid} hoặc TABLE_NEW_{uuid}
     * Trong production, có thể tạo URL đầy đủ: https://yourapp.com/order-at-table?tableId={id}
     */
    private String generateQRCode(Integer tableId) {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        if (tableId != null) {
            // Format: TABLE_1_A1B2C3D4
            return "TABLE_" + tableId + "_" + uuid;
        } else {
            // Format: TABLE_NEW_A1B2C3D4 (cho bàn mới chưa có ID)
            return "TABLE_NEW_" + uuid;
        }
    }

    private TableResponse mapToResponse(TableEntity table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .seat(table.getSeat())
                .qr(table.getQr())
                .createdAt(table.getCreatedAt())
                .updatedAt(table.getUpdatedAt())
                .build();
    }
}
