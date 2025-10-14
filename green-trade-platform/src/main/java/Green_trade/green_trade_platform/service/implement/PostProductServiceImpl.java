package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.*;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
import Green_trade.green_trade_platform.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    public PostProductServiceImpl(
            PostProductRepository postProductRepository,
            CategoryRepository categoryRepository,
            FileUtils fileUtils,
            CloudinaryService cloudinaryService,
            SellerRepository sellerRepository,
            ProductImageRepository productImageRepository, SubscriptionRepository subscriptionRepository) {
        this.postProductRepository = postProductRepository;
        this.categoryRepository = categoryRepository;
        this.fileUtils = fileUtils;
        this.cloudinaryService = cloudinaryService;
        this.sellerRepository = sellerRepository;
        this.productImageRepository = productImageRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public PostProduct createNewPostProduct(
            UploadPostProductRequest request,
            List<MultipartFile> files
    ) throws Exception {
        try {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new RuntimeException("Category is not existed")
                    );

            Seller seller = sellerRepository.findById(request.getSellerId())
                    .orElseThrow(
                            () -> new RuntimeException("Seller is not existed")
                    );

            Subscription subscription = subscriptionRepository.findBySeller_SellerIdOrderByEndDayDesc(request.getSellerId());
            Long maxImg = subscription.getSubscriptionPackage().getMaxImgPerPost();
            if(files.size() > maxImg) {
                throw new Exception("Your subscription only allowed " + maxImg + "per post");
            }

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
                    .status(false)
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

    public List<PostProduct> getAllPostProduct() {
        try {
            return postProductRepository.findAll();
        } catch (Exception e) {
            log.info(">>> Error at PostProductServiceImpl: {}", e.getMessage());
            throw e;
        }
    }
}
