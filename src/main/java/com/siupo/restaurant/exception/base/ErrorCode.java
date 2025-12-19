package com.siupo.restaurant.exception.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ================= AUTH =================
    UNAUTHORIZED(401, "AUTH_001", "Unauthorized"),
    INVALID_CREDENTIALS(401, "AUTH_002", "Invalid email or password"),
    TOKEN_EXPIRED(401, "AUTH_003", "Token has expired"),
    TOKEN_INVALID(401, "AUTH_004", "Invalid token"),
    ACCESS_DENIED(403, "AUTH_005", "Access denied"),
    ACCOUNT_DISABLED(403, "AUTH_006", "Account is disabled"),
    ACCOUNT_LOCKED(403, "AUTH_007", "Account is locked"),
    NOT_CUSTOMER(403, "AUTH_008", "Only customers are allowed to perform this action"),
    FORBIDDEN(403, "AUTH_009", "Forbidden"),

    // ================= TOKEN =================
    REFRESH_TOKEN_NOT_FOUND(404, "TOKEN_001", "Refresh token not found"),
    REFRESH_TOKEN_EXPIRED(400, "TOKEN_002", "Refresh token has expired"),
    INVALID_REFRESH_TOKEN(401, "TOKEN_003", "Invalid refresh token"),

    // ================= USER =================
    USER_NOT_FOUND(404, "USER_001", "User not found"),
    EMAIL_ALREADY_EXISTS(409, "USER_002", "Email already exists"),
    PHONE_ALREADY_EXISTS(409, "USER_003", "Phone number already exists"),
    USER_NOT_ACTIVE(403, "USER_004", "User is not active"),
    INVALID_PASSWORD(400, "USER_005", "Invalid password"),
    PASSWORD_MISMATCH(400, "USER_006", "Password and confirm password do not match"),
    NEW_PASSWORD_SAME_AS_OLD(400, "USER_007", "New password cannot be the same as the old password"),

    // ================= OTP =================
    OTP_EXPIRED(400, "OTP_001", "OTP has expired"),
    OTP_INVALID(400, "OTP_002", "Invalid OTP"),
    OTP_ATTEMPTS_EXCEEDED(400, "OTP_003", "OTP attempts exceeded"),
    OTP_NOT_FOUND_OR_EXPIRED(404, "OTP_004", "OTP request not found or expired"),
    OTP_STILL_VALID(400, "OTP_005", "An unexpired OTP already exists"),

    // ================= ADDRESS =================
    ADDRESS_NOT_FOUND(404, "ADDRESS_001", "Address not found"),
    CANNOT_DELETE_DEFAULT_ADDRESS(400, "ADDRESS_002", "Cannot delete default address"),

    // ================= PRODUCT =================
    PRODUCT_NOT_FOUND(404, "PRODUCT_001", "Product not found"),
    PRODUCT_OUT_OF_STOCK(400, "PRODUCT_002", "Product out of stock"),
    PRODUCT_ALREADY_EXISTS(409, "PRODUCT_003", "Product already exists"),
    CANNOT_CHANGE_STATUS_OF_PRODUCT(400, "PRODUCT_004", "Cannot change status of product"),
    SEARCH_PRICE_INVALID(400, "PRODUCT_005", "Invalid price range for search"),
    SEARCH_CATEGORY_NOT_BLANK(400, "PRODUCT_006", "Category ID list cannot be blank"),

    // ================= CART =================
    MISSING_SELECTION(400, "CART_001", "Missing product or combo selection"),
    CONFLICTING_SELECTION(400, "CART_002", "Conflicting product and combo selection"),
    INVALID_QUANTITY(400, "CART_003", "Invalid quantity"),
    CART_ITEM_NOT_FOUND(404, "CART_004", "Cart item not found"),

    // ================ CATEGORY =================
    CATEGORY_NOT_FOUND(404, "CATEGORY_001", "Category not found"),
    CATEGORY_NOT_EMPTY(400, "CATEGORY_002", "Category is not empty"),
    CANNOT_DELETE_CATEGORY(400, "CATEGORY_003", "Cannot delete category"),

    // ================= COMBO =================
    COMBO_NOT_FOUND(404, "COMBO_001", "Combo not found"),
    CANNOT_UPDATE_DELETED_COMBO(400, "COMBO_002", "Cannot update a deleted combo"),

    // ================= ORDER =================
    ORDER_NOT_FOUND(404, "ORDER_001", "Order not found"),
    ORDER_INVALID_STATUS(400, "ORDER_002", "Invalid order status"),
    ORDER_CANNOT_CANCEL(400, "ORDER_003", "Order cannot be cancelled"),
    ORDER_ITEM_NOT_FOUND(404, "ORDER_004", "Order item not found"),

   // ================= VOUCHER =================
    VOUCHER_NOT_FOUND(404, "VOUCHER_001", "Voucher not found"),
    VOUCHER_EXPIRED(400, "VOUCHER_002", "Voucher has expired"),
    VOUCHER_ALREADY_USED(400, "VOUCHER_003", "Voucher has already been used"),
    VOUCHER_ALREADY_EXISTS(409, "VOUCHER_004", "Voucher code already exists"),
    VOUCHER_START_DATE_INVALID(400, "VOUCHER_005", "Voucher start date must be before end date"),
    CANNOT_APPLY_VOUCHER(400, "VOUCHER_006", "Cannot apply voucher to this order"),
    YOU_CANNOT_APPLY_VOUCHER(400, "VOUCHER_007", "You cannot apply this voucher"),

    // ================= WISHLIST =================
    WISHLIST_NOT_FOUND(404, "WISHLIST_001", "Wishlist not found"),
    WISHLIST_CONFLICT(409, "WISHLIST_002", "Wishlist conflict"),
    WISHLIST_ITEM_NOT_FOUND(404, "WISHLIST_003", "Wishlist item not found"),

    // ================= PAYMENT =================
    PAYMENT_FAILED(400, "PAYMENT_001", "Payment failed"),
    PAYMENT_METHOD_NOT_SUPPORTED(400, "PAYMENT_002", "Payment method not supported"),

    // ================= IMAGE / FILE =================
    FILE_UPLOAD_FAILED(400, "FILE_001", "File upload failed"),
    FILE_TOO_LARGE(400, "FILE_002", "File size exceeds limit"),
    FILE_TYPE_NOT_SUPPORTED(400, "FILE_003", "File type not supported"),

    // ================= VALIDATION =================
    VALIDATION_ERROR(400, "VALID_001", "Validation error"),
    INVALID_REQUEST(400, "VALID_002", "Invalid request data"),

    // ================= EMAIL =================
    EMAIL_SENDING_FAILED(500, "EMAIL_001", "Failed to send email"),

    // ================= PENDING =================
    REGISTRATION_NOT_FOUND_OR_EXPIRED(404, "PEND_001", "Registration request not found or expired"),
    FORGOT_PASSWORD_REQUEST_NOT_FOUND_OR_EXPIRED(404, "PEND_002", "Forgot password request not found or expired"),

    // ================= TAG =================
    TAG_NOT_FOUND(404, "TAG_001", "Tag not found"),
    TAG_ALREADY_EXISTS(409, "TAG_002", "Tag already exists"),

    // ================= REVIEW =================
    REVIEW_NOT_FOUND(404, "REVIEW_001", "Review not found"),
    REVIEW_ALREADY_EXISTS(409, "REVIEW_002", "Review for this order item already exists"),
    STAR_RATING_OUT_OF_BOUNDS(400, "REVIEW_003", "Star rating must be between 1 and 5"),
    CANNOT_REVIEW_OWN_ORDER(400, "REVIEW_004", "Cannot review your own order"),
    CANNOT_REVIEW_BEFORE_DELIVERY(400, "REVIEW_005", "Can only review orders that are delivered or completed"),

    // ================= SYSTEM =================
    INTERNAL_ERROR(500, "SYS_001", "Internal server error"),
    SERVICE_UNAVAILABLE(503, "SYS_002", "Service unavailable"),

    // ================= TABLE =================
    MONEY_NOT_VALID(400, "TABLE_001", "Money amount is not valid"),
    TABLE_NOT_FOUND(404, "TABLE_002", "Table not found"),

    LOI_CHUA_DAT(999, "TEST_001", "Loi chua dat");

    private final int httpStatus;
    private final String code;
    private final String message;
}
