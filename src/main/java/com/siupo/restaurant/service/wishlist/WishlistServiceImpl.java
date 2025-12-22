package com.siupo.restaurant.service.wishlist;

import com.siupo.restaurant.dto.response.WishlistResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.ConflictException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.mapper.WishListMapper;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.WishlistItemRepository;
import com.siupo.restaurant.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final WishListMapper wishListMapper;

    @Override
    @Transactional
    public WishlistResponse getWishlist(User user) {
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> createWishlistForUser(user));
        return wishListMapper.toResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistResponse addToWishlist(User user, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseGet(() -> createWishlistForUser(user));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new ConflictException(ErrorCode.WISHLIST_CONFLICT);
        }
        wishlist.addProduct(product);
        return wishListMapper.toResponse(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public WishlistResponse removeFromWishlist(User user, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WISHLIST_NOT_FOUND));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new NotFoundException(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        wishlist.removeProduct(product);
        return wishListMapper.toResponse(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public void clearWishlist(User user) {
        Wishlist wishlist = wishlistRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WISHLIST_NOT_FOUND));
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(User user, Long productId) {
        return wishlistRepository.findByUser(user)
                .map(wishlist -> wishlistItemRepository.existsByWishlistIdAndProductId(
                        wishlist.getId(), productId))
                .orElse(false);
    }

    @Transactional
    protected Wishlist createWishlistForUser(User user) {
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .build();
        return wishlistRepository.save(wishlist);
    }
}
