package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.request.ProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerController {
    @PostMapping("/upload-profile")
    public ResponseEntity<?> uploadBuyerProfile(@PathVariable Long id,
                                                @Valid @ModelAttribute ProfileRequest profileRequest,
                                                @RequestParam(value = "avatar_url", required = true) MultipartFile avatarFile) {

    }
}
