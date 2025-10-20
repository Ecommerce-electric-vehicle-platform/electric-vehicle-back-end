package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dispute")
public class DisputeController {
    @PostMapping("/raise-dispute")
    public ResponseEntity<RestResponse<?, ?>> receiveDispute() {

        return ResponseEntity.status(HttpStatus.OK.value()).body(null);
    }
}
