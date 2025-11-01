package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.mapper.AdminMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.AdminRepository;
import Green_trade.green_trade_platform.request.CreateAdminRequest;
import Green_trade.green_trade_platform.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl {
    private final AdminRepository adminRepository;
    private final StringUtils stringUtils;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public Admin getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Lấy username hiện tại

        return adminRepository.findByEmployeeNumber(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    public Admin handleCreateAdminAccount(MultipartFile avatarFile, CreateAdminRequest request) throws IOException {
        if (adminRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new IllegalArgumentException("Duplicate employee number.");
        }

        if (adminRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("This phone number is already in use. PLease try another phone number.");
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("This email is already in use. Please try another email.");
        }

        Admin admin = adminMapper.toEntity(request);
        admin.setFullName(stringUtils.formatFullName(admin.getFullName()));

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        Map<String, String> temp = cloudinaryService.upload(avatarFile, "admin/" + admin.getFullName() + "/avatar");
        admin.setAvatarPublicId(temp.get("publicId"));
        admin.setAvatarUrl(temp.get("fileUrl"));

        return adminRepository.save(admin);
    }
}
