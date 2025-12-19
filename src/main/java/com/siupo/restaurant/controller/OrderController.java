package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.OrderDTO;
import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.CreateOrderResponse;
import com.siupo.restaurant.dto.response.OrderReviewsResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.model.Customer;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.order.OrderService;
import com.siupo.restaurant.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;
	private final ReviewService reviewService;

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

	@GetMapping("/{id}/reviews")
	public ResponseEntity<ApiResponse<OrderReviewsResponse>> getOrderReviews(
			@AuthenticationPrincipal User user,
			@PathVariable Long id) {

		OrderReviewsResponse response = reviewService.getReviewsByOrderId(id, user);

		return ResponseEntity.ok(
				ApiResponse.<OrderReviewsResponse>builder()
						.code("200")
						.success(true)
						.message("Reviews retrieved successfully")
						.data(response)
						.build()
		);
	}

	// ============== ADMIN ENDPOINTS ==============

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
			@RequestParam(required = false) EOrderStatus status,
			@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<OrderDTO> orders = orderService.getAllOrders(pageable, status);

		return ResponseEntity.ok(
				ApiResponse.<Page<OrderDTO>>builder()
						.code("200")
						.success(true)
						.message("Lấy danh sách đơn hàng thành công")
						.data(orders)
						.build()
		);
	}

	@GetMapping("/admin/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetailById(@PathVariable Long id) {
		OrderDTO order = orderService.getOrderDetailById(id);

		return ResponseEntity.ok(
				ApiResponse.<OrderDTO>builder()
						.code("200")
						.success(true)
						.message("Lấy chi tiết đơn hàng thành công")
						.data(order)
						.build()
		);
	}

	@PatchMapping("/admin/{id}/status")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
			@PathVariable Long id,
			@RequestParam EOrderStatus status) {

		OrderDTO order = orderService.updateOrderStatus(id, status);

		return ResponseEntity.ok(
				ApiResponse.<OrderDTO>builder()
						.code("200")
						.success(true)
						.message("Cập nhật trạng thái đơn hàng thành công")
						.data(order)
						.build()
		);
	}

	@DeleteMapping("/admin/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
		orderService.deleteOrder(id);

		return ResponseEntity.ok(
				ApiResponse.<Void>builder()
						.code("200")
						.success(true)
						.message("Xóa đơn hàng thành công")
						.build()
		);
	}

}
