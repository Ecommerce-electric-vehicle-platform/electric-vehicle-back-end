package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.WishListing;
import Green_trade.green_trade_platform.repository.WishListingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class WishListingServiceImpl {
    private final WishListingRepository wishListingRepository;

    public WishListing addWishList(WishListing wishListing) {
        log.info(">>> [Wishlist Service] Add product to wish list: Started.");
        log.info(">>> [Wishlist Service] WishListing before save: id={}, buyerId={}, postId={}",
                wishListing.getId(),
                wishListing.getBuyer() != null ? wishListing.getBuyer().getBuyerId() : null,
                wishListing.getPostProduct() != null ? wishListing.getPostProduct().getId() : null);
        return wishListingRepository.save(wishListing);
    }

    public void removePostProduct(long id) {
        WishListing wishListing = wishListingRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Can not find wish list with id: " + id)
        );

        wishListingRepository.delete(wishListing);
    }
}
