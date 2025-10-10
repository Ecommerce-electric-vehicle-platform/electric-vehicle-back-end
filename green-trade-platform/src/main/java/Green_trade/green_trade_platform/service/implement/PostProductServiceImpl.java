package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Category;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.repository.CategoryRepository;
import Green_trade.green_trade_platform.repository.PostProductRepository;
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

    public PostProductServiceImpl(
            PostProductRepository postProductRepository,
            CategoryRepository categoryRepository,
            FileUtils fileUtils
    ) {
        this.postProductRepository = postProductRepository;
        this.categoryRepository = categoryRepository;
        this.fileUtils = fileUtils;
    }
    public PostProduct createNewPostProduct(UploadPostProductRequest request, Map<String, MultipartFile> files) throws Exception {
        try {
            Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category is not existed"));

            PostProduct newPost = PostProduct.builder()
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
                log.info(">>> Uploaded File name: {}", key);
            });


            return null;
        } catch (Exception e) {
            log.info("Error at createNewPostProduct: {}", e.getMessage());
            throw e;
        }
    }
}
