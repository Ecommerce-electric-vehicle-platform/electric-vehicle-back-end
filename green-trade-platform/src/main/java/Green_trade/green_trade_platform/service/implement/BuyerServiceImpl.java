package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.DuplicateProfileException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BuyerServiceImpl {
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private DateUtils dateUtils;

    public Map<String, Object> uploadBuyerProfile(Long id, ProfileRequest request, MultipartFile avatarFile) throws IOException {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Can not find buyer with this id."));

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
                avatarUrl = cloudinaryService.upload(avatarFile, "buyers/" + buyer.getBuyerId() + ":" + buyer.getUsername() + "/avatar");
                body.put("avatar", avatarUrl);
            }
            buyer.setAvatarUrl(avatarUrl);
            buyer.setDefaultShippingAddress(request.getDefaultShippingAddress());
            buyer.setFullName(request.getFullName());
            buyer.setPhoneNumber(request.getPhoneNumber());
            buyerRepository.save(buyer);
            buyer.setDob(dob);
            buyer.setGender(request.getGender());
            body.put("profile", buyer.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return body;
    }
}
