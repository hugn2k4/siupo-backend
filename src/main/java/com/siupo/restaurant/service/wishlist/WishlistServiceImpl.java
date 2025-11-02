package com.siupo.restaurant.service.wishlist;

import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.response.WishlistResponse;
import com.siupo.restaurant.dto.response.WishlistItemResponse;
import com.siupo.restaurant.exception.ConflictException;
import com.siupo.restaurant.exception.NotFoundException;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.Wishlist;
import com.siupo.restaurant.model.WishlistItem;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.repository.WishlistItemRepository;
import com.siupo.restaurant.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.siupo.restaurant.model.ProductImage;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {
        log.info("Getting wishlist for user: {}", userId);

        Wishlist wishlist = wishlistRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createWishlistForUser(userId));

        return convertToResponse(wishlist);
    }

    @Override
    public WishlistResponse addToWishlist(Long userId, Long productId) {
        log.info("Adding product {} to wishlist for user: {}", productId, userId);

        // Get or create wishlist
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createWishlistForUser(userId));

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        // Check if product already in wishlist
        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new ConflictException("Sản phẩm đã có trong danh sách yêu thích");
        }

        // Add product to wishlist
        wishlist.addProduct(product);
        wishlist = wishlistRepository.save(wishlist);

        log.info("Product {} added to wishlist successfully", productId);
        return convertToResponse(wishlist);
    }

    @Override
    public WishlistResponse removeFromWishlist(Long userId, Long productId) {
        log.info("Removing product {} from wishlist for user: {}", productId, userId);

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh sách yêu thích"));

        // Check if product exists in wishlist
        if (!wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new NotFoundException("Sản phẩm không có trong danh sách yêu thích");
        }

        // Remove product from wishlist
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        wishlist.removeProduct(product);
        wishlist = wishlistRepository.save(wishlist);

        log.info("Product {} removed from wishlist successfully", productId);
        return convertToResponse(wishlist);
    }

    @Override
    public void clearWishlist(Long userId) {
        log.info("Clearing wishlist for user: {}", userId);

        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh sách yêu thích"));

        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);

        log.info("Wishlist cleared successfully for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(Long userId, Long productId) {
        log.debug("Checking if product {} is in wishlist for user: {}", productId, userId);

        return wishlistRepository.findByUserId(userId)
                .map(wishlist -> wishlistItemRepository.existsByWishlistIdAndProductId(
                        wishlist.getId(), productId))
                .orElse(false);
    }

    /**
     * Create wishlist for user
     */
    private Wishlist createWishlistForUser(Long userId) {
        log.info("Creating new wishlist for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .build();

        return wishlistRepository.save(wishlist);
    }

    /**
     * Convert Wishlist entity to Response
     */
    private WishlistResponse convertToResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .items(wishlist.getItems().stream()
                        .map(this::convertItemToResponse)
                        .collect(Collectors.toList()))
                .totalItems(wishlist.getItems().size())
                .build();
    }

    private WishlistItemResponse convertItemToResponse(WishlistItem item) {
        Product product = item.getProduct();

        // Lấy list URL ảnh
        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream()
                .map(ProductImage::getUrl)
                .toList()
                : null;

        return WishlistItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .productPrice(product.getPrice())
                .productImages(imageUrls)
                .build();
    }


}