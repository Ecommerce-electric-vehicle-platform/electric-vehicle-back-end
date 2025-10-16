package Green_trade.green_trade_platform.util;

import org.springframework.stereotype.Component;

@Component
public class StringUtils {
    public String formatFullName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        String[] words = name.trim().toLowerCase().split("\\s+");

        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formattedName.toString().trim();
    }
}
