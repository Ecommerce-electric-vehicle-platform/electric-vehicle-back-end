package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.VerifiedDecisionStatus;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.*;
import Green_trade.green_trade_platform.request.PostProductDecisionRequest;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
import Green_trade.green_trade_platform.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PostProductServiceImpl {

    private final PostProductRepository postProductRepository;

    private final CategoryRepository categoryRepository;

    private final FileUtils fileUtils;
    private final CloudinaryService cloudinaryService;
    private final SellerRepository sellerRepository;
    private final ProductImageRepository productImageRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BuyerRepository buyerRepository;
    private final AdminRepository adminRepository;

    public PostProductServiceImpl(
            PostProductRepository postProductRepository,
            CategoryRepository categoryRepository,
            FileUtils fileUtils,
            CloudinaryService cloudinaryService,
            SellerRepository sellerRepository,
            ProductImageRepository productImageRepository, SubscriptionRepository subscriptionRepository, BuyerRepository buyerRepository, AdminRepository adminRepository) {
        this.postProductRepository = postProductRepository;
        this.categoryRepository = categoryRepository;
        this.fileUtils = fileUtils;
        this.cloudinaryService = cloudinaryService;
        this.sellerRepository = sellerRepository;
        this.productImageRepository = productImageRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.buyerRepository = buyerRepository;
        this.adminRepository = adminRepository;
    }

    public PostProduct createNewPostProduct(
            UploadPostProductRequest request,
            List<MultipartFile> files
    ) throws Exception {
        try {
            Subscription subscription = subscriptionRepository.findBySeller_SellerIdOrderByEndDayDesc(request.getSellerId()).orElseThrow(() -> new Exception("Seller doesn't subscribe service"));
            Long maxImg = subscription.getSubscriptionPackage().getMaxImgPerPost();
            if (subscription.getEndDay().isBefore(LocalDateTime.now())) {
                throw new Exception("Seller subsrciption is expired");
            }
            if(files.size() > maxImg) {
                throw new Exception("Your subscription only allowed " + maxImg + "per post");
            }

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new RuntimeException("Category is not existed")
                    );

            Seller seller = sellerRepository.findById(request.getSellerId())
                    .orElseThrow(
                            () -> new RuntimeException("Seller is not existed")
                    );

            PostProduct newPost = PostProduct.builder()
                    .seller(seller)
                    .title(request.getTitle())
                    .brand(request.getBrand())
                    .model(request.getModel())
                    .manufactureYear((request.getManufactureYear()))
                    .usedDuration(request.getUsedDuration())
                    .rejectedReason("No decision yet")
                    .conditionLevel(request.getConditionLevel())
                    .price(request.getPrice())
                    .description(request.getDescription())
                    .locationTrading(request.getLocationTrading())
                    .active(true)
                    .verifiedDecisionstatus(VerifiedDecisionStatus.UNVAILABLE)
                    .verified(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .deletedAt(null)
                    .category(category)
                    .admin(null)
                    .build();
            log.info(">>> request data: {}", request.toString());
            log.info(">>> files data: {}", files);
            files.forEach((file) -> {
                fileUtils.validateFile(file);
                log.info(">>> Checked File name: {}", file.toString());
            });
            newPost = postProductRepository.save(newPost);

            for(int i = 0; i <= files.size() - 1; i++) {
                Map<String, String> uploadResult = cloudinaryService.upload(files.get(i), "PostImages/" + newPost.getId() + ":" + seller.getBuyer().getUsername() + "/product_image_" + i);
                String imageUrl =uploadResult.get("fileUrl");
                log.info(">>> Passed uploaded picture {}", i);
                ProductImage productImage = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .orderImage((long) i + 1)
                        .postProduct(newPost)
                        .build();
                productImageRepository.save(productImage);
            }
            log.info(">>> Passed uploaded file");

            return newPost;
        } catch (Exception e) {
            log.info("Error at createNewPostProduct: {}", e.getMessage());
            throw e;
        }
    }

    public Page<PostProduct> getAllPostProduct(int page, int size) {
        try {
            // Lấy tất cả PostProduct theo phân trang
            Page<PostProduct> postProducts = postProductRepository.findAll(PageRequest.of(page, size));

            // Lọc danh sách PostProduct thỏa điều kiện
            List<PostProduct> filteredProducts = postProducts.getContent().stream()
                    .filter(postProduct -> {
                        try {
                            Subscription subscription = subscriptionRepository
                                    .findBySeller_SellerIdOrderByEndDayDesc(postProduct.getSeller().getSellerId())
                                    .orElseThrow(() -> new Exception("Seller doesn't subscribe service"));
                            return subscription.getSubscriptionPackage().getId() >= 2;
                        } catch (Exception e) {
                            log.warn(">>> Seller {} does not have a valid subscription: {}", postProduct.getSeller().getSellerId(), e.getMessage());
                            return false;
                        }
                    })
                    .toList();

            // Trả về Page chứa các kết quả đã lọc
            return new PageImpl<>(filteredProducts, PageRequest.of(page, size), postProducts.getTotalElements());

        } catch (Exception e) {
            log.error(">>> Error at PostProductServiceImpl: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public PostProduct getPostProductById(Long postProductId) throws Exception {
        try {
            PostProduct foundPostProduct = postProductRepository.findById(postProductId).orElseThrow(
                    () -> new Exception("Post is not existed")
            );
            return foundPostProduct;
        } catch (Exception e) {
            throw e;
        }
    }

    public PostProduct checkPostProductVerification(PostProductDecisionRequest request) throws Exception {
        try {
            Admin admin = adminRepository.findByEmployeeNumber(request.getAdminUsername()).orElseThrow(() -> new Exception("Admin is not existed"));
            PostProduct postProduct = postProductRepository.findById(
                    request.getPostProductId()).orElseThrow(() -> new Exception("Post Product is not existed")
            );

            if(!request.isPassed()) {
                postProduct.setVerifiedDecisionstatus(VerifiedDecisionStatus.REJECTED);
                postProduct.setVerified(false);
                postProduct.setActive(false);
                postProduct.setAdmin(admin);
                postProduct.setRejectedReason(request.getRejectedReason());
            } else {
                postProduct.setVerifiedDecisionstatus(VerifiedDecisionStatus.APPROVED);
                postProduct.setVerified(true);
                postProduct.setActive(true);
                postProduct.setAdmin(admin);
                postProduct.setRejectedReason("");
            }

            return postProduct;
        } catch(Exception e) {
            log.info(">>> Error at decidePostContentValidation: {}" + e.getMessage());
            throw e;
        }
    }
}
