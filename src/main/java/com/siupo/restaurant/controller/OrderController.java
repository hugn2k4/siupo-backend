package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.CreateOrderResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
