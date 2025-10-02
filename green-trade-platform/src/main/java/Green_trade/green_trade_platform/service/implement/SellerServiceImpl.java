package Green_trade.green_trade_platform.service.implement;

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
                                                     MultipartFile storePolicyFile) throws IOException {
        Buyer buyer = buyerRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        String identityUrl = "", businessLicenseUrl = "", storePolicyUrl = "";
        Seller seller = null;

        Map<String, String> uploadedUrls = new HashMap<>();

        if (identityFile != null && !identityFile.isEmpty()) {
            identityUrl = cloudinaryService.upload(identityFile, "sellers/" + sellerId + "/identity");
            uploadedUrls.put("identity", identityUrl);
        }

        if (businessLicenseFile != null && !businessLicenseFile.isEmpty()) {
            businessLicenseUrl = cloudinaryService.upload(businessLicenseFile, "sellers/" + sellerId + "/business_license");
            uploadedUrls.put("business_license", businessLicenseUrl);
        }

        if (storePolicyFile != null && !storePolicyFile.isEmpty()) {
            storePolicyUrl = cloudinaryService.upload(storePolicyFile, "sellers/" + sellerId + "/store_policy");
            uploadedUrls.put("store_policy", storePolicyUrl);
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
