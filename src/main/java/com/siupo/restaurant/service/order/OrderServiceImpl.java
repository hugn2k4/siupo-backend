package com.siupo.restaurant.service.order;

import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.dto.OrderDTO;
import com.siupo.restaurant.dto.OrderItemDTO;
import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.*;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import com.siupo.restaurant.enums.EPaymentStatus;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.exception.NotFoundException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.*;
import com.siupo.restaurant.service.payment.MomoPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final MomoPaymentService momoPaymentService;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Giỏ hàng trống"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách sản phẩm đặt hàng không được để trống");
        }

        // Kiểm tra sản phẩm/combo request có trong cart
        for (CartItemDTO item : request.getItems()) {
            CartItem ci = null;
            String itemName = "";

            // Tìm cart item tương ứng (product hoặc combo)
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                ci = cartItems.stream()
                        .filter(c -> c.getProduct() != null && c.getProduct().getId().equals(item.getProduct().getId()))
                        .findFirst()
                        .orElse(null);
                if (ci != null) {
                    itemName = ci.getProduct().getName();
                }
            } else if (item.getCombo() != null && item.getCombo().getId() != null) {
                ci = cartItems.stream()
                        .filter(c -> c.getCombo() != null && c.getCombo().getId().equals(item.getCombo().getId()))
                        .findFirst()
                        .orElse(null);
                if (ci != null) {
                    itemName = ci.getCombo().getName();
                }
            }

            if (ci == null) {
                throw new BadRequestException("Sản phẩm/Combo không tồn tại trong giỏ hàng");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("Số lượng không hợp lệ");
            }

            if (!item.getQuantity().equals(ci.getQuantity())) {
                throw new BadRequestException("Số lượng " + itemName + " không khớp");
            }
        }

        // Khởi tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .items(new ArrayList<>())
                .shippingAddress(request.getShippingAddress())
                .status(EOrderStatus.PENDING)
                .build();

        double subTotal = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO item : request.getItems()) {
            CartItem cartItem = null;
            double price = 0.0;

            // Tìm cart item tương ứng (product hoặc combo)
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                cartItem = cartItems.stream()
                        .filter(ci -> ci.getProduct() != null && ci.getProduct().getId().equals(item.getProduct().getId()))
                        .findFirst()
                        .orElse(null);
                if (cartItem != null) {
                    price = cartItem.getProduct().getPrice();
                }
            } else if (item.getCombo() != null && item.getCombo().getId() != null) {
                cartItem = cartItems.stream()
                        .filter(ci -> ci.getCombo() != null && ci.getCombo().getId().equals(item.getCombo().getId()))
                        .findFirst()
                        .orElse(null);
                if (cartItem != null) {
                    price = cartItem.getCombo().getBasePrice();
                }
            }

            if (cartItem == null) {
                continue; // Skip nếu không tìm thấy
            }

            subTotal += price * item.getQuantity();

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .combo(cartItem.getCombo())
                    .quantity(item.getQuantity())
                    .price(price)
                    .reviewed(false)
                    .build();
            orderItems.add(oi);
        }

        orderItemRepository.saveAll(orderItems);
        order.setItems(orderItems);

        double vat = Math.round(subTotal * 0.1 * 100) / 100.0;
        double shippingFee = (request.getPaymentMethod() == EPaymentMethod.COD) ? 2.0 : 0.0;
        double total = subTotal + vat + shippingFee;

        order.setVat(vat);
        order.setShippingFee(shippingFee);
        order.setTotalPrice(total);

        // Xử lý thanh toán
        Payment payment = handlePayment(order, total, request.getPaymentMethod());
        order.setPayment(payment);
        orderRepository.save(order);

        // Xóa sản phẩm/combo trong cart
        List<Long> itemIdsToDelete = new ArrayList<>();
        for (CartItemDTO item : request.getItems()) {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                cartItems.stream()
                        .filter(ci -> ci.getProduct() != null && ci.getProduct().getId().equals(item.getProduct().getId()))
                        .findFirst()
                        .ifPresent(ci -> itemIdsToDelete.add(ci.getId()));
            } else if (item.getCombo() != null && item.getCombo().getId() != null) {
                cartItems.stream()
                        .filter(ci -> ci.getCombo() != null && ci.getCombo().getId().equals(item.getCombo().getId()))
                        .findFirst()
                        .ifPresent(ci -> itemIdsToDelete.add(ci.getId()));
            }
        }
        cartItemRepository.deleteAllById(itemIdsToDelete);

        // Trả response
        return buildCreateOrderResponse(order, request);
    }

    private Payment handlePayment(Order order, double total, EPaymentMethod method) {
        method = (method == null) ? EPaymentMethod.COD : method;

        if (method == EPaymentMethod.COD) {
            CODPayment payment = CODPayment.builder()
                    .amount(total)
                    .status(EPaymentStatus.PAID)
                    .paymentMethod(EPaymentMethod.COD)
                    .note("Thanh toán khi nhận hàng")
                    .build();
            order.setStatus(EOrderStatus.PENDING);
            return paymentRepository.save(payment);
        } else if (method == EPaymentMethod.MOMO) {
            MomoPayment momo = MomoPayment.builder()
                    .amount(total)
                    .status(EPaymentStatus.PROCESSING)
                    .paymentMethod(EPaymentMethod.MOMO)
                    .build();
            order.setStatus(EOrderStatus.PENDING);
            return paymentRepository.save(momo);
        }

        throw new BadRequestException("Phương thức thanh toán không hợp lệ");
    }

    private CreateOrderResponse buildCreateOrderResponse(Order order, CreateOrderRequest request) {

        CreateOrderResponse.CreateOrderResponseBuilder responseBuilder = CreateOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .shippingFee(order.getShippingFee())
                .vat(order.getVat())
                .paymentMethod(order.getPayment().getPaymentMethod())
                .items(order.getItems()
                        .stream()
                        .map(OrderItemDTO::toDTO)
                        .toList()
                );

        if (order.getPayment() != null && order.getPayment().getPaymentMethod() == EPaymentMethod.MOMO) {
            try {
                MomoPaymentResponse momoResponse = momoPaymentService.createPayment(order);
                responseBuilder.payUrl(momoResponse.getPayUrl())
                        .qrCodeUrl(momoResponse.getQrCodeUrl())
                        .deeplink(momoResponse.getDeeplink());
            } catch (Exception e) {
                log.error("Failed to create MoMo payment URL for Order #{}: {}", order.getId(), e.getMessage());
            }
        }

        return responseBuilder.build();
    }


    @Override
    @Transactional(readOnly = true)
    public CreateOrderResponse getOrderById(Long id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xem đơn hàng này");
        }

        return buildCreateOrderResponse(order, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUser(User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(OrderDTO::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public OrderDTO cancelOrderByCustomer(Long id, User user) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        // Check quyền sở hữu đơn
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền hủy đơn hàng này");
        }

        // Check trạng thái cho phép hủy
        if (!(order.getStatus() == EOrderStatus.PENDING ||
                order.getStatus() == EOrderStatus.CONFIRMED ||
                order.getStatus() == EOrderStatus.WAITING_FOR_PAYMENT)) {
            throw new BadRequestException("Đơn hàng không thể hủy ở trạng thái hiện tại");
        }

        // Xử lý thanh toán nếu là Momo
        if (order.getPayment().getPaymentMethod() == EPaymentMethod.MOMO) {

            if (order.getPayment().getStatus() == EPaymentStatus.PAID) {
                // TODO: Gọi API Momo refund nếu muốn
                order.getPayment().setStatus(EPaymentStatus.REFUND);
            } else {
                order.getPayment().setStatus(EPaymentStatus.CANCELED);
            }
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus(EOrderStatus.CANCELED);
        orderRepository.save(order);

        return OrderDTO.toDTO(order);
    }

    // ============== ADMIN METHODS ==============

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable, EOrderStatus status) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(OrderDTO::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderDetailById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với ID: " + id));
        return OrderDTO.toDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long id, EOrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với ID: " + id));

        // Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new BadRequestException("Không thể chuyển trạng thái từ " + order.getStatus() + " sang " + newStatus);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        return OrderDTO.toDTO(order);
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với ID: " + id));

        // Only allow deletion of canceled orders
        if (order.getStatus() != EOrderStatus.CANCELED) {
            throw new BadRequestException("Chỉ có thể xóa đơn hàng đã bị hủy");
        }

        orderRepository.delete(order);
        return true;
    }

    private boolean isValidStatusTransition(EOrderStatus currentStatus, EOrderStatus newStatus) {
        // Define valid status transitions
        return switch (currentStatus) {
            case WAITING_FOR_PAYMENT -> newStatus == EOrderStatus.PENDING || newStatus == EOrderStatus.CANCELED;
            case PENDING -> newStatus == EOrderStatus.CONFIRMED || newStatus == EOrderStatus.CANCELED;
            case CONFIRMED -> newStatus == EOrderStatus.SHIPPING || newStatus == EOrderStatus.CANCELED;
            case SHIPPING -> newStatus == EOrderStatus.DELIVERED || newStatus == EOrderStatus.CANCELED;
            case DELIVERED -> newStatus == EOrderStatus.COMPLETED;
            case COMPLETED, CANCELED -> false; // Terminal states
        };
    }
}
