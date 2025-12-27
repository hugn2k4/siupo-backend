package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.OrderAtTableRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.OrderAtTableResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import com.siupo.restaurant.service.orderAtTable.OrderAtTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders-at-table")
@RequiredArgsConstructor
public class OrderAtTableController {

    private final OrderAtTableService orderAtTableService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderAtTableResponse>> createOrder(
            @Valid @RequestBody OrderAtTableRequest request) {

        OrderAtTableResponse response = orderAtTableService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderAtTableResponse>builder()
                        .code("201")
                        .success(true)
                        .message("Đặt món tại bàn thành công")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderAtTableResponse>> getOrderById(
            @PathVariable Long id) {

        OrderAtTableResponse response = orderAtTableService.getOrderById(id);

        return ResponseEntity.ok(
                ApiResponse.<OrderAtTableResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Lấy thông tin đơn hàng thành công")
                        .data(response)
                        .build()
        );
    }

    // ============== ADMIN ENDPOINTS ==============

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderAtTableResponse>>> getAllOrders(
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) EOrderStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<OrderAtTableResponse> orders = orderAtTableService.getAllOrders(tableId, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<OrderAtTableResponse>>builder()
                        .code("200")
                        .success(true)
                        .message("Lấy danh sách đơn hàng tại bàn thành công")
                        .data(orders)
                        .build()
        );
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderAtTableResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam EOrderStatus status) {

        OrderAtTableResponse order = orderAtTableService.updateOrderStatus(id, status);

        return ResponseEntity.ok(
                ApiResponse.<OrderAtTableResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Cập nhật trạng thái đơn hàng thành công")
                        .data(order)
                        .build()
        );
    }

    @PostMapping("/admin/{id}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderAtTableResponse>> processPayment(
            @PathVariable Long id,
            @RequestParam EPaymentMethod paymentMethod) {

        OrderAtTableResponse order = orderAtTableService.processPayment(id, paymentMethod);

        return ResponseEntity.ok(
                ApiResponse.<OrderAtTableResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Thanh toán thành công")
                        .data(order)
                        .build()
        );
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderAtTableService.deleteOrder(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code("200")
                        .success(true)
                        .message("Xóa đơn hàng thành công")
                        .build()
        );
    }
}
