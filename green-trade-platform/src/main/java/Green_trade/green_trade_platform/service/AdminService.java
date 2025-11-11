package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.request.CreateAdminRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AdminService {
    Admin getCurrentUser();

    Admin handleCreateAdminAccount(MultipartFile avatarFile, CreateAdminRequest request) throws IOException;

    void blockAccount(long id, String message, String activity);

    Page<Admin> getAdminList(int page, int size);

    Admin getAdminProfile(long id);
}

