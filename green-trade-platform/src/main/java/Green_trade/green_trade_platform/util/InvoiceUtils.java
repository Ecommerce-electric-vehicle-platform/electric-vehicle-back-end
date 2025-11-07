package Green_trade.green_trade_platform.util;

import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.service.implement.CloudinaryService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Slf4j
public class InvoiceUtils {

    private final CloudinaryService cloudinaryService;

    public InvoiceUtils(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    private static final String TEMP_DIR = "invoices/";

    public String generateInvoicePDFAndUpload(Invoice invoice) {
        try {
            // 1️⃣ Tạo thư mục tạm nếu chưa có
            Files.createDirectories(Paths.get(TEMP_DIR));

            String fileName = "invoice_" + invoice.getInvoiceNumber() + ".pdf";
            String filePath = TEMP_DIR + fileName;

            Path tempDirPath = Paths.get(TEMP_DIR).toAbsolutePath();
            log.info("📂 Đường dẫn tuyệt đối tới thư mục hóa đơn: {}", tempDirPath);

            // 2️⃣ Sinh file PDF cục bộ
            Document document = new Document(PageSize.A4, 36, 36, 72, 36);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Paragraph header = new Paragraph("GREEN TRADE PLATFORM - HÓA ĐƠN MUA BÁN", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Mã hóa đơn: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Ngày lập: " +
                    invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            document.add(new Paragraph(" "));

            // === Người mua ===
            Buyer buyer = invoice.getOrder().getBuyer();
            document.add(new Paragraph("NGƯỜI MUA HÀNG", new Font(Font.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("Tên: " + buyer.getFullName()));
            document.add(new Paragraph("Email: " + buyer.getEmail()));
            document.add(new Paragraph("SĐT: " + buyer.getPhoneNumber()));
            document.add(new Paragraph("Địa chỉ: " +
                    buyer.getStreet() + ", " +
                    buyer.getWardName() + ", " +
                    buyer.getDistrictName() + ", " +
                    buyer.getProvinceName()));
            document.add(new Paragraph(" "));

            // === Người bán ===
            Seller seller = invoice.getOrder().getPostProduct().getSeller();
            document.add(new Paragraph("NGƯỜI BÁN HÀNG", new Font(Font.HELVETICA, 14, Font.BOLD)));
            document.add(new Paragraph("Cửa hàng: " + seller.getStoreName()));
            document.add(new Paragraph("Tên người bán: " + seller.getSellerName()));
            document.add(new Paragraph("Mã số thuế: " + seller.getTaxNumber()));
            document.add(new Paragraph(" "));

            // === Thông tin sản phẩm ===
            PostProduct product = invoice.getOrder().getPostProduct();
            document.add(new Paragraph("THÔNG TIN SẢN PHẨM", new Font(Font.HELVETICA, 14, Font.BOLD)));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell("Tên sản phẩm");
            table.addCell(product.getTitle());
            table.addCell("Thương hiệu");
            table.addCell(product.getBrand());
            table.addCell("Mẫu");
            table.addCell(product.getModel());
            table.addCell("Năm SX");
            table.addCell(String.valueOf(product.getManufactureYear()));
            table.addCell("Tình trạng");
            table.addCell(product.getConditionLevel());
            table.addCell("Mô tả");
            table.addCell(product.getDescription());
            document.add(table);
            document.add(new Paragraph(" "));

            // === Giá trị hóa đơn ===
            BigDecimal basePrice = product.getPrice();
            BigDecimal shipping = invoice.getOrder().getShippingFee() != null
                    ? invoice.getOrder().getShippingFee() : BigDecimal.ZERO;
            BigDecimal taxAmount = basePrice.multiply(BigDecimal.valueOf(invoice.getTaxRate() / 100));
            BigDecimal total = basePrice.add(shipping).add(taxAmount);

            document.add(new Paragraph("TỔNG KẾT", new Font(Font.HELVETICA, 14, Font.BOLD)));
            PdfPTable priceTable = new PdfPTable(2);
            priceTable.setWidthPercentage(100);
            priceTable.addCell("Giá sản phẩm");
            priceTable.addCell(formatCurrency(basePrice));
            priceTable.addCell("Phí vận chuyển");
            priceTable.addCell(formatCurrency(shipping));
            priceTable.addCell("Thuế VAT (" + invoice.getTaxRate() + "%)");
            priceTable.addCell(formatCurrency(taxAmount));
            priceTable.addCell("Tổng cộng");
            PdfPCell totalCell = new PdfPCell(new Phrase(formatCurrency(total)));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceTable.addCell(totalCell);
            document.add(priceTable);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("CẢM ƠN ĐÃ TIN TƯỞNG LỰA CHỌN GREEN TRADE PLATFORM",
                    new Font(Font.HELVETICA, 12, Font.ITALIC)));
            document.close();

            // 3️⃣ Upload trực tiếp file PDF lên Cloudinary (không cần MultipartFile)
            File pdfFile = new File(filePath);
            Map<String, String> uploadResult = cloudinaryService.uploadFile(pdfFile, "invoices/" + invoice.getInvoiceNumber() + "-" + invoice.getOrder().getOrderCode());
            log.info(">>> [InvoiceUtils] uploadResult: {}", uploadResult);


            String cloudUrl = (String) uploadResult.get("fileUrl");

            // 4️⃣ Cập nhật lại Invoice
            invoice.setPdfUrl(cloudUrl);

            // 5️⃣ Xóa file local sau khi upload
            pdfFile.delete();

            return cloudUrl;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String formatCurrency(BigDecimal value) {
        return String.format("%,.0f VNĐ", value);
    }
}
