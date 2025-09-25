package Green_trade.green_trade_platform.util;

import org.springframework.stereotype.Component;

@Component
public class Acceptable {
    // Username must contains at-least 8 characters and do not contains space or special character or number
    public final String USERNAME_REGEX = "^[a-zA-Z]{8,}$";
    // Password must contains at-least 8 lengths and contains at-least one number, one character, one special character.
    // DO NOT contains space
    public final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$\n";
    public final String PHONE_NUMBER_REGEX = "^(?:\\\\+84|0)(3|5|7|8|9)\\\\d{8}$";
    public final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$\n";
    public final boolean isValid(String data, String regex) {
        return data.matches(regex);
    }
}
