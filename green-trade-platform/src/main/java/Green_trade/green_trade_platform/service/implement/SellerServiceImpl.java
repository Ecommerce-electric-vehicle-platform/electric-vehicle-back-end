package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.Decision;
import Green_trade.green_trade_platform.enumerate.SellerStatus;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.mapper.SubscriptionMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.repository.AdminRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import Green_trade.green_trade_platform.repository.SubscriptionRepository;
import Green_trade.green_trade_platform.request.ApproveSellerRequest;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.response.SubscriptionResponse;
import Green_trade.green_trade_platform.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;

    private final SellerMapper sellerMapper;

    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionMapper subscriptionMapper;

    private final AdminServiceImpl adminService;


    public SubscriptionResponse checkServicePackageValidity(Long id) throws Exception {
        try {
            Optional<Seller> sellerOpt = sellerRepository.findById(id);
            if(sellerOpt.isEmpty()) {
                throw new Exception("Seller is not existed");
            }

            Subscription subscription = subscriptionRepository.findBySeller_SellerIdOrderByEndDayDesc(id);

            if(LocalDateTime.now().isAfter(subscription.getEndDay())) {
                throw new Exception("Subscription is expired");
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
    public Seller handlePendingSeller(ApproveSellerRequest request) {
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ seller này: " + request.getSellerId()));

        Admin admin = adminService.getCurrentUser();

        if(request.getDecision().equals(Decision.OK)) {
            seller.setStatus(SellerStatus.ACCEPTED);
            return sellerRepository.save(seller);
        } else {
            sellerRepository.delete(seller);
            return null;
        }
    }
}
