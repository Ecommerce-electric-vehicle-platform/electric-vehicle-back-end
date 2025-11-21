package Green_trade.green_trade_platform.advisor;

import Green_trade.green_trade_platform.exception.ThirdPartyAIException;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionAdvisor {
    private final ResponseMapper responseMapper;

    public ExceptionAdvisor(ResponseMapper responseMapper) {
        this.responseMapper = responseMapper;
    }

    @ExceptionHandler(ThirdPartyAIException.class)
    public ResponseEntity<RestResponse<Object, Map<String, String>>> handleThirdPartyAIException(
            ThirdPartyAIException exception
    ) {
        RestResponse response = responseMapper.toDto(
                false,
                "Dịch vụ AI bên thứ ba đang gặp sự cố, vui lòng thử lại sau.",
                null,
                Map.of(
                        "origin", "AI_THIRD_PARTY_SERVICE",
                        "message", exception.getMessage()
                )
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object, Map<String, String>>> handleExceptionHandler(Exception e) {
        Throwable rootCause = e;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        List<String> projectStackTraces = Arrays.stream(rootCause.getStackTrace())
                .filter(ste -> ste.getClassName().startsWith("Green_trade"))
                .map(StackTraceElement::toString)
                .toList();

        StackTraceElement originElement = projectStackTraces.isEmpty()
                ? (rootCause.getStackTrace().length > 0 ? rootCause.getStackTrace()[0] : null)
                : Arrays.stream(rootCause.getStackTrace())
                .filter(ste -> ste.getClassName().startsWith("Green_trade"))
                .findFirst()
                .orElse(null);

        RestResponse response = responseMapper.toDto(
                false,
                "Internal Server Error",
                null,
                Map.of(
                        "origin", e.getStackTrace()[0].toString(),
                        "message", e.getMessage(),
                        "errorType", e.getClass().getSimpleName(),
                        "file", originElement != null ? originElement.getFileName() : "Unknown file",
                        "lineNumber", originElement != null ? originElement.getLineNumber() : -1
                )
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(response);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<RestResponse<Object, Map<String, String>>> handleExceptionHandler(Exception e) {
//        // Lọc stack trace để lấy dòng đầu tiên thuộc về project của bạn (bắt đầu bằng package gốc)
//        StackTraceElement originElement = Arrays.stream(e.getStackTrace())
//                .filter(ste -> ste.getClassName().startsWith("Green_trade"))
//                .findFirst()
//                .orElse(e.getStackTrace()[0]); // fallback nếu không có
//
//        RestResponse<Object, Map<String, String>> response = responseMapper.toDto(
//                false,
//                "Internal Server Error",
//                null,
//                Map.of(
//                        "origin", originElement.toString(),
//                        "file", originElement.getFileName(),
//                        "lineNumber", String.valueOf(originElement.getLineNumber()),
//                        "message", e.getMessage(),
//                        "errorType", e.getClass().getSimpleName()
//                )
//        );
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<RestResponse<Object, Map<String, Object>>> handleExceptionHandler(Exception e) {
//        // 🔹 1️⃣ Tìm nguyên nhân gốc (root cause)
//        Throwable rootCause = e;
//        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
//            rootCause = rootCause.getCause();
//        }
//
//        // 🔹 2️⃣ Lấy các stack trace thuộc về project (lọc theo package)
//        List<String> projectStackTraces = Arrays.stream(rootCause.getStackTrace())
//                .filter(ste -> ste.getClassName().startsWith("Green_trade"))
//                .map(StackTraceElement::toString)
//                .toList();
//
//        // 🔹 3️⃣ Lấy dòng đầu tiên trong project làm origin
//        StackTraceElement originElement = projectStackTraces.isEmpty()
//                ? (rootCause.getStackTrace().length > 0 ? rootCause.getStackTrace()[0] : null)
//                : Arrays.stream(rootCause.getStackTrace())
//                .filter(ste -> ste.getClassName().startsWith("Green_trade"))
//                .findFirst()
//                .orElse(null);
//
//        // 🔹 4️⃣ Tạo thông tin lỗi chi tiết
//        Map<String, Object> errorInfo = new LinkedHashMap<>();
//        errorInfo.put("errorType", rootCause.getClass().getName());
//        errorInfo.put("message", rootCause.getMessage());
//        errorInfo.put("file", originElement != null ? originElement.getFileName() : "Unknown file");
//        errorInfo.put("lineNumber", originElement != null ? originElement.getLineNumber() : -1);
//        errorInfo.put("origin", originElement != null ? originElement.toString() : "Unknown origin");
//        errorInfo.put("fullStackTrace", Arrays.stream(rootCause.getStackTrace())
//                .map(StackTraceElement::toString)
//                .toList());
//        errorInfo.put("projectStackTrace", projectStackTraces);
//
//        // 🔹 5️⃣ Gộp message + cause nếu có
//        if (rootCause != e) {
//            errorInfo.put("causedBy", e.getClass().getName() + ": " + e.getMessage());
//        }
//
//        // 🔹 6️⃣ Trả response JSON chi tiết
//        RestResponse<Object, Map<String, Object>> response = responseMapper.toDto(
//                false,
//                "Internal Server Error",
//                null,
//                errorInfo
//        );
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }

}
