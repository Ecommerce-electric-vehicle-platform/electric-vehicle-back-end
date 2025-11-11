package Green_trade.green_trade_platform.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String, String> upload(MultipartFile file, String folder) throws IOException;

    boolean delete(String publicId, String folder);

    Map<String, String> uploadFile(File file, String folder);
}

