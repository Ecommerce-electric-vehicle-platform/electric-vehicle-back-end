package Green_trade.green_trade_platform.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload file lên Cloudinary, trả về secure_url (String).
     * @param file MultipartFile từ request
     * @param folder folder trên Cloudinary (ví dụ: "sellers/123")
     * @return secure_url
     */
    public String upload(MultipartFile file, String folder) throws IOException {
        String publicId = UUID.randomUUID().toString();
        Map<?,?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "public_id", publicId,
                        "resource_type", "auto"
                )
        );
        Object secureUrl = res.get("secure_url");
        if (secureUrl != null) return secureUrl.toString();
        // fallback: try "url"
        Object url = res.get("url");
        return url != null ? url.toString() : null;
    }
}
