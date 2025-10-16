package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.DuplicateProfileException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import Green_trade.green_trade_platform.util.DateUtils;
import Green_trade.green_trade_platform.util.FileUtils;
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
    @Autowired
    private FileUtils fileUtils;

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
        String username = authentication.getName(); // Lấy username hiện tại

        return buyerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    public Buyer getBuyerFromVnPayRequest(String vnpOtherType) {
        String[] temp = vnpOtherType.split(" ");
        return buyerRepository.findById(Long.parseLong(temp[0])).
                orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + temp[0]));
    }

    public BigDecimal getWalletBalance() {
        Buyer buyer = getCurrentUser();
        return buyerRepository.findBalanceByBuyerId(buyer.getBuyerId());
    }
}
