package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.AccountType;
import Green_trade.green_trade_platform.enumerate.SellerStatus;
import Green_trade.green_trade_platform.enumerate.VerifiedDecisionStatus;
import Green_trade.green_trade_platform.exception.AuthException;
import Green_trade.green_trade_platform.exception.ProfileException;
import Green_trade.green_trade_platform.exception.SubscriptionExpiredException;
import Green_trade.green_trade_platform.mapper.RegisterShopShippingServiceMapper;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.mapper.SubscriptionMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.*;
import Green_trade.green_trade_platform.request.ApproveSellerRequest;
import Green_trade.green_trade_platform.request.MailRequest;
import Green_trade.green_trade_platform.response.ApproveSellerResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.response.SubscriptionResponse;
import Green_trade.green_trade_platform.service.SellerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final AdminServiceImpl adminService;
    private final BuyerServiceImpl buyerService;
    private final NotificationRepository notificationRepository;
    private final GhnServiceImpl ghnService;
    private final RegisterShopShippingServiceMapper registerShopShippingServiceMapper;
    private final BuyerRepository buyerRepository;
    private final PostProductRepository postProductRepository;
    private final MailServiceImpl mailService;

    public Seller createShippingShop(String dataRaw, Seller seller) throws JsonProcessingException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(dataRaw);
            JsonNode data = root.path("data");
            int shopId = data.path("shop_id").asInt();
            seller.setGhnShopId(shopId + "");
            return sellerRepository.save(seller);
        } catch (Exception e) {
            throw e;
        }
    }


    public SubscriptionResponse checkServicePackageValidity(String username) throws Exception {
        try {
            Buyer buyer = buyerRepository.findByUsername(username).orElseThrow(() -> new ProfileException("Profile is not existed"));
            Optional<Seller> sellerOpt = sellerRepository.findByBuyer(buyer);
            if (sellerOpt.isEmpty()) {
                throw new ProfileException("Seller is not existed");
            }

            Subscription subscription = subscriptionRepository.findFirstBySeller_SellerIdOrderByEndDayDesc(sellerOpt.get().getSellerId()).orElseThrow(() -> new Exception("Seller doesn't subscribe service"));

            if (LocalDateTime.now().isAfter(subscription.getEndDay())) {
                throw new SubscriptionExpiredException();
            }

            return subscriptionMapper.toDto(true, subscription.getEndDay(), subscription.getSubscriptionPackage().getName());
        } catch (Exception e) {
            log.info("Error at checkServicePackageValidity: {}", e);
            throw e;
        }
    }

    public Page<SellerResponse> getAllPendingSeller(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sellerId").ascending());
        Page<Seller> sellers = sellerRepository.findAllByStatus(SellerStatus.PENDING, pageable);

        List<SellerResponse> responses = sellers.getContent()
                .stream()
                .map(sellerMapper::toDto)
                .toList();

        return new PageImpl<>(responses, pageable, sellers.getTotalElements());
    }

    @Transactional
    public ApproveSellerResponse handlePendingSeller(ApproveSellerRequest request) throws JsonProcessingException {
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new ProfileException("Không tìm thấy hồ sơ seller này: " + request.getSellerId())
                );

        // Create mail request to send to seller
        MailRequest mailRequest = MailRequest.builder()
                .from("green.trade.platform.391@gmail.com")
                .to(seller.getBuyer().getEmail())
                .subject("UPGRADE ACCOUNT RESULT")
                .build();

        Admin admin = adminService.getCurrentUser();
        Notification notice = null;
        ApproveSellerResponse response = ApproveSellerResponse.builder()
                .sellerId(seller.getSellerId())
                .reason(request.getMessage())
                .decision(request.getDecision())
                .decidedAt(LocalDateTime.now())
                .build();

        if (request.getDecision().equals(VerifiedDecisionStatus.APPROVED)) {
            seller.setAdmin(admin);
            seller.setStatus(SellerStatus.ACCEPTED);
            Seller tempSeller = sellerRepository.save(seller);
            Map<String, Object> ghnBody = registerShopShippingServiceMapper.toDto(seller);
            tempSeller = createShippingShop(ghnService.registerShop(ghnBody), seller);
            tempSeller = sellerRepository.save(seller);

            notice = Notification.builder()
                    .receiverId(seller.getSellerId())
                    .type(AccountType.SELLER)
                    .title("UPGRADE ACCOUNT INFORMATION RESULT")
                    .content(request.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();

            mailRequest.setMessage("""
                    🎉 <strong>Chúc mừng bạn!</strong><br><br>" +
                    "Yêu cầu nâng cấp tài khoản của bạn đã được <strong>Green Trade</strong> phê duyệt thành công.<br>" +
                    "Từ bây giờ, bạn có thể đăng bán sản phẩm, quản lý đơn hàng và giao dịch trực tiếp với khách hàng.<br><br>" +
                    "Vui lòng tuân thủ <a href='https://green-trade-platform.com/policies' style='color:#4CAF50;font-weight:bold;'>chính sách người bán</a> " +
                    "để đảm bảo môi trường kinh doanh minh bạch và bền vững.<br><br>" +
                    "💚 Chúc bạn kinh doanh thuận lợi cùng Green Trade!""");

        } else {
            String reason = request.getMessage();
            mailRequest.setMessage("""
        ⚠️ <strong>Rất tiếc!</strong><br><br>
        Yêu cầu nâng cấp tài khoản lên Seller của bạn hiện chưa được phê duyệt.<br>
        Nguyên nhân có thể do thông tin cung cấp chưa đầy đủ hoặc chưa đáp ứng điều kiện của nền tảng.<br><br>
        <strong>Lý do cụ thể:</strong> %s<br><br>
        Vui lòng kiểm tra lại hồ sơ và gửi yêu cầu mới sau khi hoàn thiện thông tin cần thiết.<br><br>
        Nếu cần hỗ trợ, hãy liên hệ 
        <a href='mailto:green.trade.platform.391@gmail.com' style='color:#4CAF50;font-weight:bold;'>
            đội ngũ hỗ trợ Green Trade
        </a> để được giúp đỡ.<br><br>
        💚 Cảm ơn bạn đã quan tâm đến Green Trade Platform!
        """.formatted(reason));

            sellerRepository.delete(seller);
            notice = Notification.builder()
                    .receiverId(seller.getBuyer().getBuyerId())
                    .type(AccountType.BUYER)
                    .title("UPGRADE ACCOUNT INFORMATION RESULT")
                    .content(request.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        notificationRepository.save(notice);
        response.setNotification(notice);
        mailService.sendBeautifulMail(mailRequest);
        return response;
    }

    public Seller getCurrentUser() {
        log.info(">>> [Seller Service] Get current user.");
        Buyer buyer = buyerService.getCurrentUser();
        return sellerRepository.findByBuyer(buyer).orElseThrow(
                () -> new AuthException("User not existed."));
    }
}
