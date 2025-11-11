package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.enumerate.WishListPriority;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.WishListing;
import org.springframework.data.domain.Page;

public interface WishListingService {
    WishListing addWishList(WishListing wishListing);

    void removePostProduct(long id);

    Page<WishListing> getWishList(Buyer buyer, int page, int size, WishListPriority priority);
}

