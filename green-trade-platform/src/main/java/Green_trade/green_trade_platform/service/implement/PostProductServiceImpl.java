package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Category;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.ProductImage;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.CategoryRepository;
import Green_trade.green_trade_platform.repository.ProductImageRepository;
import Green_trade.green_trade_platform.repository.PostProductRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
import Green_trade.green_trade_platform.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    public PostProductServiceImpl(
            PostProductRepository postProductRepository,
            CategoryRepository categoryRepository,
            FileUtils fileUtils,
            CloudinaryService cloudinaryService,
            SellerRepository sellerRepository,
            ProductImageRepository productImageRepository) {
        this.postProductRepository = postProductRepository;
        this.categoryRepository = categoryRepository;
        this.fileUtils = fileUtils;
        this.cloudinaryService = cloudinaryService;
        this.sellerRepository = sellerRepository;
        this.productImageRepository = productImageRepository;
    }
    public PostProduct createNewPostProduct(UploadPostProductRequest request, Map<String, MultipartFile> files) throws Exception {
        try {
            Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category is not existed"));
            Seller seller = sellerRepository.findById(request.getSellerId()).orElseThrow(() -> new RuntimeException("Seller is not existed"));

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
            files.forEach((key, multipartFile) -> {
                fileUtils.validateFile(multipartFile);
                log.info(">>> Checked File name: {}", key);
            });
            newPost = postProductRepository.save(newPost);

            String product1 = cloudinaryService.upload(files.get("picture1"), "PostImages/" + newPost.getId() + ":" + seller.getBuyer().getUsername() + "/product_image_1");
            log.info(">>> Passed uploaded picture1");
            String product2 = cloudinaryService.upload(files.get("picture2"), "PostImages/" + newPost.getId() + ":" + seller.getBuyer().getUsername() + "/product_image_2");
            log.info(">>> Passed uploaded picture2");
            String product3 = cloudinaryService.upload(files.get("picture3"), "PostImages/" + newPost.getId() + ":" + seller.getBuyer().getUsername() + "/product_image_3");
            log.info(">>> Passed uploaded picture3");
            String product4 = cloudinaryService.upload(files.get("picture4"), "PostImages/" + newPost.getId() + ":" + seller.getBuyer().getUsername() + "/product_image_4");
            log.info(">>> Passed uploaded picture4");
            String product5 = cloudinaryService.upload(files.get("picture5"), "PostImages/" + newPost.getId() + ":" + seller.getBuyer().getUsername() + "/product_image_5");
            log.info(">>> Passed uploaded picture5");
            log.info(">>> Passed uploaded file");

            ProductImage productImage = ProductImage.builder()
                    .imageUrl(product1)
                    .orderImage((long) 1)
                    .postProduct(newPost)
                    .build();
            productImageRepository.save(productImage);

            productImage = ProductImage.builder()
                    .imageUrl(product2)
                    .orderImage((long) 2)
                    .postProduct(newPost)
                    .build();
            productImageRepository.save(productImage);

            productImage = ProductImage.builder()
                    .imageUrl(product3)
                    .orderImage((long) 3)
                    .postProduct(newPost)
                    .build();
            productImageRepository.save(productImage);

            productImage = ProductImage.builder()
                    .imageUrl(product4)
                    .orderImage((long) 4)
                    .postProduct(newPost)
                    .build();
            productImageRepository.save(productImage);

            productImage = ProductImage.builder()
                    .imageUrl(product5)
                    .orderImage((long) 5)
                    .postProduct(newPost)
                    .build();
            productImageRepository.save(productImage);

            return newPost;
        } catch (Exception e) {
            log.info("Error at createNewPostProduct: {}", e.getMessage());
            throw e;
        }
    }
}
