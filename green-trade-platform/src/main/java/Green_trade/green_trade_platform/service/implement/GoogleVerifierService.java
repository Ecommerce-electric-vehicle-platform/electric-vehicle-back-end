package Green_trade.green_trade_platform.service.implement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service dùng để xác minh Google ID Token (JWT từ Google Identity Service).
 * Token này được FE gửi về sau khi user login bằng Google.
 */
@Service
public class GoogleVerifierService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleVerifierService() {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList("YOUR_GOOGLE_CLIENT_ID"))
                .build();
    }

    /**
     * Xác minh idToken nhận từ FE.
     * @param idTokenString token mà FE gửi lên
     * @return payload chứa thông tin user (email, name, picture, ...)
     * @throws Exception nếu token không hợp lệ
     */
    public GoogleIdToken.Payload verify(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        }
        throw new IllegalArgumentException("Invalid Google ID token.");
    }
}
