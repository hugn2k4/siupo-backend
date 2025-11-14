package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.OrderDTO;
import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.CreateOrderResponse;
import com.siupo.restaurant.model.Customer;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody CreateOrderRequest request) {

		CreateOrderResponse response = orderService.createOrder(request, user);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<CreateOrderResponse>builder()
						.code("200")
						.success(true)
						.message("Đặt hàng thành công")
						.data(response)
						.build());
	}
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CreateOrderResponse>> getOrderById(
			@AuthenticationPrincipal User user,
			@PathVariable Long id) {

		CreateOrderResponse response = orderService.getOrderById(id, user);

		return ResponseEntity.ok(
				ApiResponse.<CreateOrderResponse>builder()
						.code("200")
						.success(true)
						.message("Lấy đơn hàng thành công")
						.data(response)
						.build()
		);
	}

	@GetMapping("/my-orders")
	public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(@AuthenticationPrincipal User user) {
		if (!(user instanceof Customer)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
					ApiResponse.<List<OrderDTO>>builder()
							.code("403")
							.success(false)
							.message("Access denied: Only customers can view their orders")
							.build()
			);
		}
		List<OrderDTO> orders = orderService.getOrdersByUser(user);

		return ResponseEntity.ok(
				ApiResponse.<List<OrderDTO>>builder()
						.code("200")
						.success(true)
						.message("Orders retrieved successfully")
						.data(orders)
						.build()
		);
	}

	@PatchMapping("/{id}/customer-cancel")
	public ResponseEntity<ApiResponse<OrderDTO>> cancelOrderByCustomer(
			@AuthenticationPrincipal User user,
			@PathVariable Long id) {

		OrderDTO response = orderService.cancelOrderByCustomer(id, user);

		return ResponseEntity.ok(
				ApiResponse.<OrderDTO>builder()
						.code("200")
						.success(true)
						.message("Hủy đơn hàng thành công")
						.data(response)
						.build()
		);
	}

}
