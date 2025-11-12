package Green_trade.green_trade_platform.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateUtils {
    
    // Ngăn khởi tạo instance
    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    public static LocalDate parseAndValidateDob(String dobStr) {
        if (dobStr == null || dobStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày sinh không được để trống.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày sinh không hợp lệ. Vui lòng nhập theo dạng dd-MM-yyyy.");
        }

        LocalDate today = LocalDate.now();

        if (dob.isAfter(today)) {
            throw new IllegalArgumentException("Ngày sinh không thể là ngày trong tương lai.");
        }

        int age = Period.between(dob, today).getYears();

        if (age > 100) {
            throw new IllegalArgumentException("Tuổi không hợp lệ (quá 100 tuổi).");
        }

        return dob;
    }

    /**
     * Lấy thời gian hiện tại theo giờ Việt Nam (Asia/Ho_Chi_Minh)
     * 
     * @return LocalDateTime hiện tại theo giờ Việt Nam
     */
    public static LocalDateTime getCurrentVietnamTime() {
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        return LocalDateTime.now(vietnamZone);
    }

    /**
     * Chuyển đổi LocalDateTime từ UTC sang giờ địa phương Việt Nam (UTC+7)
     * 
     * LƯU Ý: Hàm này CHỈ dùng khi bạn chắc chắn input là UTC và cần chuyển sang VN.
     * KHÔNG dùng với LocalDateTime.now() vì nó đã là giờ VN rồi.
     * Để lấy thời gian hiện tại, dùng getCurrentVietnamTime() thay vì convertToVietnamTime(LocalDateTime.now())
     * 
     * @param dateTime LocalDateTime cần chuyển đổi (PHẢI là UTC)
     * @return LocalDateTime đã được chuyển đổi sang giờ Việt Nam
     */
    public static LocalDateTime convertToVietnamTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        // Giả định input là UTC, chuyển sang timezone Việt Nam
        ZoneId utcZone = ZoneId.of("UTC");
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");

        ZonedDateTime utcZonedDateTime = dateTime.atZone(utcZone);
        ZonedDateTime vietnamZonedDateTime = utcZonedDateTime.withZoneSameInstant(vietnamZone);

        return vietnamZonedDateTime.toLocalDateTime();
    }
}
