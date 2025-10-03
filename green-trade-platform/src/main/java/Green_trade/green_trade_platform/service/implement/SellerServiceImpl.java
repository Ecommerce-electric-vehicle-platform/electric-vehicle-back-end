package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.ProfileNotFoundException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SellerServiceImpl {
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private BuyerRepository buyerRepository;

    public Map<String, String> uploadSellerDocuments(Long sellerId,
                                                     String storeName,
                                                     String taxNumber,
                                                     String identityNumber,
                                                     MultipartFile identityFile,
                                                     MultipartFile businessLicenseFile,
                                                     MultipartFile storePolicyFile) throws IOException{
        // Get buyer information who want to upgrade account
        Buyer buyer = buyerRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Check buyer profile
        if(buyer.getFullName().equalsIgnoreCase("Not have yet")) {
            throw new ProfileNotFoundException("Complete your profile to upgrade to a seller account.");
        }

        String identityUrl = "", businessLicenseUrl = "", storePolicyUrl = "";
        Seller seller = null;

        log.info(">>> identity number: {}", identityNumber);
        log.info(">>> tax number: {}", taxNumber);
        log.info(">>> store name: {}", storeName);

        Map<String, String> uploadedUrls = new HashMap<>();

        try {
            if (identityFile != null && !identityFile.isEmpty()) {
                identityUrl = cloudinaryService.upload(identityFile, "sellers/" + sellerId + ":" + buyer.getUsername() + "/identity");
                uploadedUrls.put("identity", identityUrl);
            }

            if (businessLicenseFile != null && !businessLicenseFile.isEmpty()) {
                businessLicenseUrl = cloudinaryService.upload(businessLicenseFile, "sellers/" + sellerId + ":" + buyer.getUsername() + "/business_license");
                uploadedUrls.put("business_license", businessLicenseUrl);
            }

            if (storePolicyFile != null && !storePolicyFile.isEmpty()) {
                storePolicyUrl = cloudinaryService.upload(storePolicyFile, "sellers/" + sellerId + ":" + buyer.getUsername() + "/store_policy");
                uploadedUrls.put("store_policy", storePolicyUrl);
            }
        } catch (IOException e) {
            log.info("Error when upload image at seller service: {}", e.getMessage());
            throw e;
        }

        seller = Seller.builder()
                        .buyerId(buyer)
                        .businessLicenseUrl(businessLicenseUrl)
                        .identityImageUrl(identityUrl)
                        .storePolicyUrl(storePolicyUrl)
                        .storeName(storeName)
                        .taxNumber(taxNumber)
                        .identityNumber(identityNumber)
                        .build();

        sellerRepository.save(seller);

        return uploadedUrls;
    }
}
