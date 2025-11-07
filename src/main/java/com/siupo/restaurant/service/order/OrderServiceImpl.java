package com.siupo.restaurant.service.order;

import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.dto.request.CreateOrderRequest;
import com.siupo.restaurant.dto.response.CreateOrderResponse;
import com.siupo.restaurant.dto.response.MomoPaymentResponse;
import com.siupo.restaurant.dto.response.OrderItemResponse;
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

        // Validate request items hợp lệ
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách sản phẩm đặt hàng không được để trống");
        }

        // Kiểm tra sản phẩm trong request có trong cart
        for (CartItemDTO item : request.getItems()) {
            CartItem ci = cartItems.stream()
                    .filter(c -> c.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Sản phẩm không tồn tại trong giỏ hàng"));

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException("Số lượng sản phẩm không hợp lệ");
            }

            if (!item.getQuantity().equals(ci.getQuantity())) {
                throw new BadRequestException("Số lượng sản phẩm " + ci.getProduct().getName() + " không khớp");
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
            CartItem cartItem = cartItems.stream()
                    .filter(ci -> ci.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .get();

            double price = cartItem.getProduct().getPrice();
            subTotal += price * item.getQuantity();

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
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

        // Xóa sản phẩm trong cart
        List<Long> productIds = request.getItems().stream()
                .map(i -> i.getProduct().getId())
                .toList();
        cartItemRepository.deleteByCartAndProductIdIn(cart, productIds);

        // Trả response
        return buildCreateOrderResponse(order, orderItems,request);
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
            order.setStatus(EOrderStatus.CONFIRMED);
            return paymentRepository.save(payment);
        } else if (method == EPaymentMethod.MOMO) {
            MomoPayment momo = MomoPayment.builder()
                    .amount(total)
                    .status(EPaymentStatus.PROCESSING)
                    .paymentMethod(EPaymentMethod.MOMO)
                    .build();
            order.setStatus(EOrderStatus.WAITING_FOR_PAYMENT);
            return paymentRepository.save(momo);
        }

        throw new BadRequestException("Phương thức thanh toán không hợp lệ");
    }

    private CreateOrderResponse buildCreateOrderResponse(Order order, List<OrderItem> items, CreateOrderRequest request) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(oi -> OrderItemResponse.builder()
                        .itemId(oi.getId())
                        .productId(oi.getProduct().getId())
                        .productName(oi.getProduct().getName())
                        .quantity(oi.getQuantity())
                        .price(oi.getPrice())
                        .subtotal(oi.getPrice() * oi.getQuantity())
                        .build())
                .toList();

        CreateOrderResponse.CreateOrderResponseBuilder responseBuilder = CreateOrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .vat(order.getVat())
                .shippingFee(order.getShippingFee())
                .totalPrice(order.getTotalPrice())
                .paymentMethod(order.getPayment().getPaymentMethod())
                .items(itemResponses);

        // Nếu thanh toán MoMo, gọi API MoMo và thêm payUrl
        if (order.getPayment().getPaymentMethod() == EPaymentMethod.MOMO) {
            try {
                MomoPaymentResponse momoResponse = momoPaymentService.createPayment(order);
                responseBuilder.payUrl(momoResponse.getPayUrl())
                        .qrCodeUrl(momoResponse.getQrCodeUrl())
                        .deeplink(momoResponse.getDeeplink());
            } catch (Exception e) {
                // Log lỗi chi tiết khi gọi MoMo thất bại
                log.error("Failed to create MoMo payment URL for Order #{}: {}", order.getId(), e.getMessage(), e);
                // Vẫn trả về response nhưng không có payUrl
                responseBuilder.payUrl(null)
                        .qrCodeUrl(null)
                        .deeplink(null);
            }
        }

        return responseBuilder.build();
    }

    @Override
    @Transactional(readOnly = true)
    public CreateOrderResponse getOrderById(Long id, User user) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        if (order.getUser() != null && !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xem đơn hàng này");
        }

        List<OrderItem> items = order.getItems();
        return buildCreateOrderResponse(order, items, null);
    }
}
