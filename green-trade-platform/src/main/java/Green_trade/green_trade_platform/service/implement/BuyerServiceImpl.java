package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.DuplicateProfileException;
import Green_trade.green_trade_platform.exception.ProfileNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final TransactionServiceImpl transactionService;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private WalletServiceImpl walletService;
    private PostProductRepository postProductRepository;

    public Map<String, Object> uploadBuyerProfile(ProfileRequest request, MultipartFile avatarFile) throws IOException {
        Buyer buyer = getCurrentUser();

        Map<String, Object> body = new HashMap<>();
        String avatarUrl = (buyer.getAvatarUrl() == null) ? "" : buyer.getAvatarUrl();
        if(!avatarUrl.isEmpty()) {
            throw new DuplicateProfileException("Profile already exits.");
        }
        // Check date and parse into LocalDate
        LocalDate dob = dateUtils.parseAndValidateDob(request.getDob());
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
            Buyer buyer = getCurrentUser();
            Long id = buyer.getBuyerId();
            if (avatarFile != null && !avatarFile.isEmpty()) {
                fileUtils.validateFile(avatarFile);
                log.info(">>> Passed validate file");
            }

            log.info(">>> Passed buyer existed");
            buyer.setFullName(request.getFullName() == null ? "" : request.getFullName());
            buyer.setEmail(request.getEmail() == null ? "" : request.getEmail());
            buyer.setGender(request.getGender());
            buyer.setDob(request.getBirthDay());
            buyer.setPhoneNumber(request.getPhoneNumber() == null ? "" : request.getPhoneNumber());
            buyer.setDefaultShippingAddress(request.getDefaultShippingAddress());
            log.info(">>> Passed buyer update text information");

            //delete old avatar on cloudinary
            if(avatarFile != null && !avatarFile.isEmpty()) {
                log.info(">>> Passed avatarFile existed to update Avatar");
                if (buyer.getAvatarUrl() != null && !buyer.getAvatarUrl().equals("")) {
                    log.info(">>> Passed avatar existed before but update new");
                    boolean isDeleted = cloudinaryService.delete(
                            buyer.getAvatarPublicId(),
                            "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar"
                    );
                    log.info(">>> Passed avatar cloudinary delete working");

                    if(!isDeleted) {
                        throw new Exception("Avatar Profile is deleted failed");
                    }
                    log.info(">>> Passed avatar cloudinary delete successfully");
                }

                //upload new avatar on cloudinary
                Map<String, String> uploadResult = cloudinaryService.upload(
                        avatarFile,
                        "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar"
                );
                log.info(">>> Passed avatar cloudinary update working");

                if(uploadResult == null) {
                    throw new Exception("Avatar Profile is saved failed");
                }
                log.info(">>> Passed avatar cloudinary update successfully");

                buyer.setAvatarUrl(uploadResult.get("fileUrl"));
                buyer.setAvatarPublicId(uploadResult.get("publicId"));
            }
            log.info(">>> Passed Save Buyer Profile New Information");
            return buyerRepository.save(buyer);
        } catch (Exception e) {
             log.info(">>> Error at buyerServiceImpl: {}", e.getMessage());
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

    public Order placeOrder(PlaceOrderRequest request) throws Exception {
        //kiểm tra các thứ
        if (!isBuyerExisted(request.getUsername())) {
            throw new ProfileNotFoundException("User is not existed");
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
        //lưu đơn hàng mới
        //để lấy order gán cho transaction
        newOrder = orderRepository.save(newOrder);

        //kiểm tra phương thức thanh toán
        Payment payment = paymentRepository.findById(
                request.getPaymentId()).orElseThrow(() -> new Exception("Payment method is not supported")
        );

        //phân chia cách xử lý dựa trên phương thức thanh toán
        if(!payment.getGatewayName().equals("COD")) {
            //tạo transaction cho việc thanh toán
            Transaction transaction = transactionService.checkoutWalletPayment(
                    request.getUsername(),
                    request.getPostProductId(),
                    request.getPaymentId(),
                    newOrder
            );
            //luôn luôn success vì nếu failed thì đã kiểm tra bên trong với ném lỗi rồi
            if(transaction.getStatus().equals("SUCCESS")) {
                //lấy các lần giao dịch trước nếu có
                //nếu thành công ở lần thanh toán đầu thì danh sách transaction chỉ có một
                List<Transaction> transactions = transactionRepository.findAllByOrder(newOrder);
                //set transactions cho order
                newOrder.setTransactions(transactions);
                //cập nhật trạng thái
                newOrder.setStatus("PAID");
            }
        } else {
            //đây là luồng xử lý cho thanh toán COD
            Transaction transaction = transactionService.checkoutCODPayment(
                    request.getUsername(),
                    request.getPostProductId(),
                    request.getPaymentId(),
                    newOrder
            );
            //luôn luôn success vì nếu failed thì đã kiểm tra bên trong với ném lỗi rồi
            if(transaction.getStatus().equals("SUCCESS")) {
                List<Transaction> transactions = transactionRepository.findAllByOrder(newOrder);
                newOrder.setTransactions(transactions);
            }
        }
        return null;
    }

    public Wallet getWallet() {
        Buyer buyer = getCurrentUser();
        return walletRepository.findByBuyer(buyer).orElseThrow();
    }
}
