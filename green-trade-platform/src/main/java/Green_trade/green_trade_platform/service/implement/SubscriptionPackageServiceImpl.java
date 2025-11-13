package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.mapper.SubscriptionMapper;
import Green_trade.green_trade_platform.service.SubscriptionPackageService;
import Green_trade.green_trade_platform.mapper.SubscriptionPackageMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.PackagePrice;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.model.SubscriptionPackages;
import Green_trade.green_trade_platform.repository.PackagePriceRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import Green_trade.green_trade_platform.repository.SubscriptionPackagesRepository;
import Green_trade.green_trade_platform.repository.SubscriptionRepository;
import Green_trade.green_trade_platform.request.CreateSubscriptionPackageRequest;
import Green_trade.green_trade_platform.request.PackagePriceRequest;
import Green_trade.green_trade_platform.request.SignPackageRequest;
import Green_trade.green_trade_platform.request.UpdateSubscriptionPackageRequest;
import Green_trade.green_trade_platform.response.SignPackageResponse;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import Green_trade.green_trade_platform.util.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionPackageServiceImpl implements SubscriptionPackageService {

    private final SubscriptionPackagesRepository subscriptionPackageRepository;
    private final SubscriptionPackageMapper subscriptionPackageMapper;
    private final BuyerServiceImpl buyerService;
    private final WalletServiceImpl walletService;
    private final SubscriptionRepository subscriptionRepository;
    private final SellerRepository sellerRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PackagePriceRepository packagePriceRepository;

    public Page<SubscriptionPackages> getActivePackages(Pageable pageable) {
        return subscriptionPackageRepository.findByIsActiveTrue(pageable);
    }

    public Page<SubscriptionPackageResponse> getActivePackageResponses(Pageable pageable) {
        return getActivePackages(pageable)
                .map(subscriptionPackageMapper::toResponse);
    }

    public Map<String, Object> handlesignPackage(SignPackageRequest request) {
        Map<String, Object> result = new HashMap<>();
        Buyer buyer = buyerService.getCurrentUser();
        Seller seller = sellerRepository.findByBuyer(buyer)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người bán với id: " + buyer.getBuyerId()));
        SubscriptionPackages subscriptionPackages = subscriptionPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói người bán với id " + request.getPackageId()));


        Optional<Subscription> exitsSubscription = subscriptionRepository.findFirstBySeller_SellerIdOrderByEndDayDesc(seller.getSellerId());
        if (exitsSubscription.isPresent() && exitsSubscription.get().getIsActive() == true) {
            throw new IllegalArgumentException("Bạn đã đăng kí gói. Vui lòng hủy gói để đăng kí gói mới.");
        }

        boolean isValidBalance = isValidWalletBalance(request);
        if (!isValidBalance) {
            result.put("success", false);
            result.put("data", null);
            return result;
        }

        Map<String, Object> walletResult = walletService.handleSignPackageForSeller(buyer, request.getPrice());

        LocalDateTime startDate = DateUtils.getCurrentVietnamTime();
        LocalDateTime endDate = startDate.plusDays(request.getDurationByDay());
        Subscription subscription = Subscription.builder()
                .seller(seller)
                .subscriptionPackage(subscriptionPackages)
                .startDay(startDate)
                .endDay(endDate)
                .remainPost(subscriptionPackages.getMaxProduct())
                .priceAtPurchase(request.getPrice())
                .build();

        Subscription temp = subscriptionRepository.save(subscription);
        SignPackageResponse signPackageResponse = subscriptionMapper.
                toSignPackageResponse(subscriptionPackages.getName(),
                        buyer.getFullName(),
                        request.getPrice(), ChronoUnit.DAYS.between(startDate, endDate),
                        startDate,
                        endDate);

        result.put("success", true);
        result.put("subscription", signPackageResponse);

        return result;
    }

    public boolean isValidWalletBalance(SignPackageRequest request) {
        BigDecimal walletBalance = buyerService.getWalletBalance();
        log.info(">>> Buyer's wallet balance: {}", walletBalance);
        log.info(">>> Package price: {}", request.getPrice());
        return (walletBalance.doubleValue() >= request.getPrice());
    }

    public void cancelSubscription(Seller seller) {
        Subscription subscription = subscriptionRepository.findFirstBySeller_SellerIdOrderByEndDayDesc(seller.getSellerId()).orElseThrow(
                () -> new IllegalArgumentException("This seller has not been sign any packages yet.")
        );

        if (subscription.getIsActive() == true) {
            subscription.setIsActive(false);
            subscription.setEndDay(DateUtils.getCurrentVietnamTime());
        } else {
            throw new IllegalArgumentException("This seller has not been sign any packages yet.");
        }

        subscriptionRepository.save(subscription);
    }

    public Subscription getCurrentSubscription(Seller seller) {
        log.info(">>> [Subscription service] Started");
        Subscription subscription = subscriptionRepository.findFirstBySeller_SellerIdOrderByEndDayDesc(seller.getSellerId()).orElseThrow(
                () -> new IllegalArgumentException("This seller has not been sign any packages yet.")
        );

        if (subscription.getIsActive() == false) {
            throw new IllegalArgumentException("The current subscription is out of date.");
        }

        log.info(">>> [Subscription service] Ended");
        return subscription;
    }

    public Subscription updateRemainPost(Seller seller) {
        Subscription subscription = getCurrentSubscription(seller);
        long remainPost = subscription.getRemainPost();
        subscription.setRemainPost(remainPost--);
        return subscriptionRepository.save(subscription);
    }

    public double getTotalRevenue() {
        return subscriptionRepository.getTotalRevenue();
    }

    public SubscriptionPackages createSubscriptionPackage(CreateSubscriptionPackageRequest request) {
        log.info(">>> [SubscriptionPackageServiceImpl] createSubscriptionPackage - request: {}", request);
        
        // Check if package name already exists
        subscriptionPackageRepository.findByName(request.getName())
                .ifPresent(pkg -> {
                    throw new IllegalArgumentException("Package name already exists: " + request.getName());
                });
        
        SubscriptionPackages subscriptionPackage = SubscriptionPackages.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .maxProduct(request.getMaxProduct())
                .maxImgPerPost(request.getMaxImgPerPost())
                .canSendVerifyRequest(request.getCanSendVerifyRequest())
                .build();
        
        SubscriptionPackages saved = subscriptionPackageRepository.save(subscriptionPackage);
        log.info(">>> [SubscriptionPackageServiceImpl] createSubscriptionPackage - created package ID: {}", saved.getId());
        
        // Create package prices if provided
        if (request.getPrices() != null && !request.getPrices().isEmpty()) {
            for (PackagePriceRequest priceRequest : request.getPrices()) {
                PackagePrice packagePrice = PackagePrice.builder()
                        .subscriptionPackage(saved)
                        .price(priceRequest.getPrice())
                        .isActive(priceRequest.getIsActive())
                        .durationByDay(priceRequest.getDurationByDay())
                        .currency(priceRequest.getCurrency())
                        .discountPercent(priceRequest.getDiscountPercent())
                        .build();
                packagePriceRepository.save(packagePrice);
                log.info(">>> [SubscriptionPackageServiceImpl] createSubscriptionPackage - created price ID: {}", packagePrice.getId());
            }
        }
        
        return saved;
    }

    public SubscriptionPackages updateSubscriptionPackage(Long packageId, UpdateSubscriptionPackageRequest request) {
        log.info(">>> [SubscriptionPackageServiceImpl] updateSubscriptionPackage - packageId: {}, request: {}", packageId, request);
        
        SubscriptionPackages subscriptionPackage = subscriptionPackageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription package not found with id: " + packageId));
        
        // Check if new name conflicts with existing package (excluding current package)
        subscriptionPackageRepository.findByName(request.getName())
                .ifPresent(pkg -> {
                    if (!pkg.getId().equals(packageId)) {
                        throw new IllegalArgumentException("Package name already exists: " + request.getName());
                    }
                });
        
        subscriptionPackage.setName(request.getName());
        subscriptionPackage.setDescription(request.getDescription());
        subscriptionPackage.setActive(request.getIsActive());
        subscriptionPackage.setMaxProduct(request.getMaxProduct());
        subscriptionPackage.setMaxImgPerPost(request.getMaxImgPerPost());
        subscriptionPackage.setCanSendVerifyRequest(request.getCanSendVerifyRequest());
        
        SubscriptionPackages updated = subscriptionPackageRepository.save(subscriptionPackage);
        log.info(">>> [SubscriptionPackageServiceImpl] updateSubscriptionPackage - updated package ID: {}", updated.getId());
        
        // Update package prices if provided
        if (request.getPrices() != null && !request.getPrices().isEmpty()) {
            // Get all existing prices for this package
            List<PackagePrice> existingPrices = packagePriceRepository.findBySubscriptionPackageId(packageId);
            
            // Get IDs from request
            List<Long> requestPriceIds = request.getPrices().stream()
                    .map(PackagePriceRequest::getId)
                    .filter(id -> id != null)
                    .toList();
            
            // Soft delete prices that are not in the request
            for (PackagePrice existingPrice : existingPrices) {
                if (!requestPriceIds.contains(existingPrice.getId())) {
                    existingPrice.setDeletedAt(DateUtils.getCurrentVietnamTime());
                    existingPrice.setActive(false);
                    packagePriceRepository.save(existingPrice);
                    log.info(">>> [SubscriptionPackageServiceImpl] updateSubscriptionPackage - soft deleted price ID: {}", existingPrice.getId());
                }
            }
            
            // Create or update prices from request
            for (PackagePriceRequest priceRequest : request.getPrices()) {
                if (priceRequest.getId() != null) {
                    // Update existing price
                    PackagePrice existingPrice = packagePriceRepository.findById(priceRequest.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Package price not found with id: " + priceRequest.getId()));
                    
                    if (!existingPrice.getSubscriptionPackage().getId().equals(packageId)) {
                        throw new IllegalArgumentException("Package price does not belong to this subscription package");
                    }
                    
                    existingPrice.setPrice(priceRequest.getPrice());
                    // Nếu isActive là null, giữ nguyên giá trị cũ
                    if (priceRequest.getIsActive() != null) {
                        existingPrice.setActive(priceRequest.getIsActive());
                    }
                    existingPrice.setDurationByDay(priceRequest.getDurationByDay());
                    existingPrice.setCurrency(priceRequest.getCurrency());
                    existingPrice.setDiscountPercent(priceRequest.getDiscountPercent());
                    existingPrice.setDeletedAt(null); // Restore if was deleted
                    packagePriceRepository.save(existingPrice);
                    log.info(">>> [SubscriptionPackageServiceImpl] updateSubscriptionPackage - updated price ID: {}", existingPrice.getId());
                } else {
                    // Create new price
                    // Nếu isActive là null, set default là true
                    Boolean isActive = priceRequest.getIsActive() != null ? priceRequest.getIsActive() : true;
                    PackagePrice newPrice = PackagePrice.builder()
                            .subscriptionPackage(updated)
                            .price(priceRequest.getPrice())
                            .isActive(isActive)
                            .durationByDay(priceRequest.getDurationByDay())
                            .currency(priceRequest.getCurrency())
                            .discountPercent(priceRequest.getDiscountPercent())
                            .build();
                    packagePriceRepository.save(newPrice);
                    log.info(">>> [SubscriptionPackageServiceImpl] updateSubscriptionPackage - created new price ID: {}", newPrice.getId());
                }
            }
        }
        
        return updated;
    }
}