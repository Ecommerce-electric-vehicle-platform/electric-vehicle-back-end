package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.DuplicateProfileException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.ProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BuyerServiceImpl {
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    public Map<String, Object> uploadBuyerProfile(Long id, ProfileRequest request, MultipartFile avatarFile) throws IOException {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Can not find buyer with this id."));

        Map<String, Object> body = new HashMap<>();
        String avatarUrl = (buyer.getAvatarUrl() == null) ? "" : buyer.getAvatarUrl();
        if(!avatarFile.isEmpty()) {
            throw new DuplicateProfileException("Profile already exits.");
        }
        log.info(">>> Profile request: {}", request.toString());

        try {
            if(!avatarFile.isEmpty() && !avatarFile.isEmpty()) {
                avatarUrl = cloudinaryService.upload(avatarFile, "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar");
                body.put("avatar", avatarUrl);
            }
            buyer.setAvatarUrl(avatarUrl);
            buyer.setDefaultShippingAddress(request.getDefaultShippingAddress());
            buyer.setFullName(request.getFullName());
            buyer.setPhoneNumber(request.getPhoneNumber());
            buyerRepository.save(buyer);
            body.put("profile", buyer.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return body;
    }
}
