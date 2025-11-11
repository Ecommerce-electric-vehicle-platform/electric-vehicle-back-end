package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.AccountStatus;
import Green_trade.green_trade_platform.service.AdminService;
import Green_trade.green_trade_platform.mapper.AdminMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.repository.AdminRepository;
import Green_trade.green_trade_platform.request.CreateAdminRequest;
import Green_trade.green_trade_platform.request.MailRequest;
import Green_trade.green_trade_platform.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final StringUtils stringUtils;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final MailServiceImpl mailSender;

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

        Admin savedAdmin = adminRepository.save(admin);
        // ✅ Gửi email thông báo đến admin con
        String htmlMessage = """
                    <div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                        <h2 style='color: #4CAF50;'>🌿 Chào mừng đến với Green Trade Platform</h2>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Bạn đã được <strong>cấp quyền quản trị viên</strong> trên nền tảng Green Trade.</p>
                        <p>Dưới đây là thông tin đăng nhập của bạn:</p>
                        <ul>
                            <li><strong>Số nhân viên:</strong> %s</li>
                            <li><strong>Mật khẩu:</strong> %s</li>
                        </ul>
                        <p>Vui lòng đăng nhập ngay và thay đổi mật khẩu sau lần đầu truy cập.</p>
                        <hr style='border:none;border-top:1px solid #ccc;margin:20px 0;'/>
                        <p>💚 Cảm ơn bạn đã đồng hành cùng đội ngũ quản trị Green Trade Platform!</p>
                    </div>
                """.formatted(
                admin.getEmployeeNumber(),
                admin.getEmail(),
                request.getPassword()
        );

        MailRequest mailRequest = MailRequest.builder()
                .from("green.trade.platform.391@gmail.com")
                .to(admin.getEmail())
                .subject("Green Trade Platform - Tài khoản quản trị viên mới")
                .message(htmlMessage)
                .build();

        mailSender.sendBeautifulMail(mailRequest);

        log.info(">>> [Admin Service] Created new admin: {}", admin.getEmail());

        return savedAdmin;
    }

    public void blockAccount(long id, String message, String activity) {
        Admin admin = adminRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can not find admin with this id: " + id)
        );
        if (activity.equalsIgnoreCase("block")) {
            admin.setStatus(AccountStatus.INACTIVE);
        } else if (activity.equalsIgnoreCase("unblock")) {
            admin.setStatus(AccountStatus.ACTIVE);
        } else {
            throw new IllegalArgumentException("Activity must be 'block' or 'unblock'");
        }
        adminRepository.save(admin);
        // ✅ Chuẩn bị nội dung HTML cho email thông báo
        String action = activity.equalsIgnoreCase("block") ? "bị khóa tạm thời" : "được mở khóa lại";
        String color = activity.equalsIgnoreCase("block") ? "#e74c3c" : "#4CAF50";

        String htmlMessage = """
                <div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                    <h2 style='color: #4CAF50;'>🌿 Thông báo từ Green Trade Platform</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Tài khoản quản trị của bạn đã <strong style='color:%s;'>%s</strong> bởi <strong>Super Admin</strong> của hệ thống.</p>
                    <p><strong>Lý do:</strong> %s</p>
                    <hr style='border: none; border-top: 1px solid #ccc; margin: 20px 0;'/>
                    <p>Nếu bạn có thắc mắc hoặc cần khiếu nại, vui lòng liên hệ 
                        <a href='mailto:green.trade.platform.391@gmail.com' 
                           style='color:#4CAF50;font-weight:bold;text-decoration:none;'>
                            đội ngũ hỗ trợ Green Trade
                        </a> để được xem xét và hỗ trợ.</p>
                    <p>💚 Cảm ơn bạn đã đồng hành cùng Green Trade Platform!</p>
                </div>
                """.formatted(
                admin.getFullName(),
                color,
                action.toUpperCase(),
                message
        );

        // ✅ Gửi mail thông báo đến admin bị chặn/mở khóa
        MailRequest mailRequest = MailRequest.builder()
                .from("green.trade.platform.391@gmail.com")
                .to(admin.getEmail())
                .subject("Green Trade Platform - Tài khoản của bạn đã " + (activity.equalsIgnoreCase("block") ? "bị khóa" : "được mở khóa"))
                .message(htmlMessage)
                .build();

        mailSender.sendBeautifulMail(mailRequest);
    }

    public Page<Admin> getAdminList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return adminRepository.findAll(pageable);
    }

    public Admin getAdminProfile(long id) {
        return adminRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can not find admin with this id: " + id)
        );
    }
}
