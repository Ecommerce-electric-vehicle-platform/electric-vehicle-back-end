package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.DuplicateProfileException;
import Green_trade.green_trade_platform.exception.ProfileException;
import Green_trade.green_trade_platform.exception.WalletNotFoundException;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.*;
import Green_trade.green_trade_platform.request.PlaceOrderRequest;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.repository.WalletRepository;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import Green_trade.green_trade_platform.util.DateUtils;
import Green_trade.green_trade_platform.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class BuyerServiceImpl {
    private final BuyerRepository buyerRepository;
    private final CloudinaryService cloudinaryService;
    private final DateUtils dateUtils;
    private final FileUtils fileUtils;
    private final WalletRepository walletRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final ShippingPartnerRepository shippingPartnerRepository;
    private WalletServiceImpl walletService;
    private PostProductRepository postProductRepository;

    public Map<String, Object> uploadBuyerProfile(ProfileRequest request, MultipartFile avatarFile) throws IOException {
        Buyer buyer = getCurrentUser();

        Map<String, Object> body = new HashMap<>();
        String avatarUrl = (buyer.getAvatarUrl() == null) ? "" : buyer.getAvatarUrl();
        if(!avatarUrl.isEmpty()) {
            throw new DuplicateProfileException("Profile already exits.");
        }
//        // Check date and parse into LocalDate
////        LocalDate dob = dateUtils.parseAndValidateDob(request.getDob());
        LocalDate dob = LocalDate.parse(request.getDob());
        log.info(">>> Profile request: {}", request.toString());

        try {
            if(!avatarFile.isEmpty() && !avatarFile.isEmpty()) {
                Map<String, String> uploadResult = cloudinaryService.upload(avatarFile, "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar");
                avatarUrl = uploadResult.get("fileUrl");
                buyer.setAvatarPublicId(uploadResult.get("publicId"));
                body.put("avatar", avatarUrl);
            }
            buyer.setAvatarUrl(avatarUrl);
            buyer.setDefaultShippingAddress(request.getDefaultShippingAddress());
            buyer.setWardName(request.getWardName());
            buyer.setDistrictName(request.getDistrictName());
            buyer.setProvinceName(request.getProvinceName());
            buyer.setFullName(request.getFullName());
            buyer.setPhoneNumber(request.getPhoneNumber());
            buyer.setDob(dob);
            buyer.setGender(request.getGender());
            buyerRepository.save(buyer);
            body.put("profile", buyer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return body;
    }

    public Buyer updateProfile(UpdateBuyerProfileRequest request, MultipartFile avatarFile) throws Exception {
        try {
            log.info(">>> [Update user profile service] Starting update profile service");
            Buyer buyer = getCurrentUser();
            Long id = buyer.getBuyerId();

            log.info(">>> [Update profile services] profile request: {}.", request);
            buyer.setFullName(request.getFullName() == null ? "" : request.getFullName());
            buyer.setEmail(request.getEmail() == null ? "" : request.getEmail());
            buyer.setGender(request.getGender());
            buyer.setDob(request.getDob());
            buyer.setPhoneNumber(request.getPhoneNumber() == null ? "" : request.getPhoneNumber());
            buyer.setDefaultShippingAddress(request.getDefaultShippingAddress());
            buyer.setWardName(request.getWardName());
            buyer.setDistrictName(request.getDistrictName());
            buyer.setProvinceName(request.getProvinceName());
            log.info(">>> [Update profile services] set text data into buyer profile.");

            //delete old avatar on cloudinary
            if(avatarFile != null && !avatarFile.isEmpty()) {
                log.info(">>> [Update profile services] Starting delete old avatar on Cloudinary.");
                if (buyer.getAvatarUrl() != null) {
                    boolean isDeleted = cloudinaryService.delete(
                            buyer.getAvatarPublicId(),
                            "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar"
                    );

                    if(!isDeleted) {
                        log.info(">>> [Update profile services] Error occur when deleting old avatar.");
                        throw new ProfileException("Avatar Profile is deleted failed");
                    }
                    log.info(">>> [Update profile services] Delete old avatar successfully.");
                }

                //upload new avatar on cloudinary
                log.info(">>> [Update profile services] Starting upload new avatar into Cloudinary");
                Map<String, String> uploadResult = cloudinaryService.upload(
                        avatarFile,
                        "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar"
                );

                if(uploadResult == null) {
                    log.info(">>> [Update profile services] Error occur when uploading new avatar into Cloudinary.");
                    throw new Exception("Avatar Profile is saved failed");
                }

                log.info(">>> [Update profile services] Avatar is uploaded into Cloudinary.");

                buyer.setAvatarUrl(uploadResult.get("fileUrl"));
                buyer.setAvatarPublicId(uploadResult.get("publicId"));
            }
            log.info(">>> [Update profile services] Update buyer profile successfully.");
            return buyerRepository.save(buyer);
        } catch (Exception e) {
             log.info(">>> [Update profile services] Error at buyerServiceImpl: {}", e.getMessage());
             throw e;
        }
    }

    public Buyer getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return buyerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User is not existed: " + username));
    }

    public Buyer getBuyerFromVnPayRequest(String vnpOtherType) {
        String[] temp = vnpOtherType.split(" ");
        return buyerRepository.findById(Long.parseLong(temp[0])).
                orElseThrow(() -> new UsernameNotFoundException("User is not existed: " + temp[0]));
    }

    public BigDecimal getWalletBalance() {
        Buyer buyer = getCurrentUser();
        return buyerRepository.findBalanceByBuyerId(buyer.getBuyerId());
    }

    public boolean isBuyerExisted(Long buyerId) {
        boolean result = false;
        Optional<Buyer> buyerOpt = buyerRepository.findById(buyerId);
        if(buyerOpt.isPresent()){
            result = true;
        }
        return result;
    }

    public boolean isBuyerExisted(String username) {
        boolean result = false;
        Optional<Buyer> buyerOpt = buyerRepository.findByUsername(username);
        if(buyerOpt.isPresent()){
            result = true;
        }
        return result;
    }

    public Order placeOrderCOD(PlaceOrderRequest request) throws Exception {
        Optional<Buyer> buyerOpt = buyerRepository.findByUsername(request.getUsername());
        Optional<PostProduct> postProductOpt = postProductRepository.findById(request.getPostProductId());

        //kiểm tra các thứ
        if (!isBuyerExisted(request.getUsername())) {
            throw new ProfileException("User is not existed");
        }

        if (postProductOpt.isEmpty()) {
            throw new Exception("Post is not existed");
        }

        if (postProductOpt.get().isSold()) {
            throw new Exception("The product has been sold");
        }

        //tạo mới một đơn hàng
        Order newOrder = Order.builder()
                .admin(null)
                .buyer(buyerOpt.get())
                .orderCode(String.format("%09d", new Random().nextInt(1_000_000_000)))
                .shippingAddress(
                        request.getShippingAddress().isBlank() ?
                                buyerOpt.get().getDefaultShippingAddress() :
                                request.getShippingAddress()
                )
                .phoneNumber(
                        request.getPhoneNumber().isBlank() ?
                                buyerOpt.get().getPhoneNumber() :
                                request.getPhoneNumber()
                )
                .transactions(null)
                .price(postProductOpt.get().getPrice())
                .status("PENDING")
                .cancelReason("Not Canceled Yet")
                .canceledAt(null)
                .build();

        return orderRepository.save(newOrder);
    }

    public Order updateOrderStatus(Order order, String status) {
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order updateOrderTransactions(Order order, List<Transaction> transactions) {
        order.setTransactions(transactions);
        return orderRepository.save(order);
    }

    public Order placeOrder(PlaceOrderRequest request, String shippingFee) throws Exception {
        //kiểm tra các thứ
        if (!isBuyerExisted(request.getUsername())) {
            throw new ProfileException("User is not existed");
        }

        Optional<Buyer> buyerOpt = buyerRepository.findByUsername(request.getUsername());
        if (!walletService.isBuyerHasWallet(buyerOpt.get())) {
            throw new WalletNotFoundException("The wallet of User is not existed");
        }

        Optional<PostProduct> postProductOpt = postProductRepository.findById(request.getPostProductId());
        if (postProductOpt.isEmpty()) {
            throw new Exception("Post is not existed");
        }

        if (postProductOpt.get().isSold()) {
            throw new Exception("The product has been sold");
        }

        ShippingPartner shippingPartner = shippingPartnerRepository.findById(request.getShippingPartnerId())
                .orElseThrow(
                        () -> new Exception("Shipping Partner is not existed")
                );

        //tạo mới một đơn hàng
        Order newOrder = Order.builder()
                .admin(null)
                .postProduct(postProductOpt.get())
                .buyer(buyerOpt.get())
                .orderCode(null)
                .shippingAddress(
                        request.getShippingAddress().isBlank() ?
                                buyerOpt.get().getDefaultShippingAddress() :
                                request.getShippingAddress()
                )
                .phoneNumber(
                        request.getPhoneNumber().isBlank() ?
                                buyerOpt.get().getPhoneNumber() :
                                request.getPhoneNumber()
                )
                .shippingPartner(shippingPartner)
                .shippingFee(new BigDecimal(shippingFee))
                .transactions(null)
                .price(postProductOpt.get().getPrice().add(new BigDecimal(shippingFee)))
                .status("PENDING")
                .cancelReason("Not Canceled Yet")
                .canceledAt(null)
                .build();

        return orderRepository.save(newOrder);
    }

    public Order updateOrderCode(Order newOrder, String shippingCode) {
        newOrder.setOrderCode(shippingCode);
        return orderRepository.save(newOrder);
    }



    public Wallet getWallet() {
        Buyer buyer = getCurrentUser();
        return walletRepository.findByBuyer(buyer).orElseThrow();
    }
}
