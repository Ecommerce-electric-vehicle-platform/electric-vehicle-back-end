package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Invoice;
import Green_trade.green_trade_platform.response.InvoiceResponse;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toDto(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .pdfUrl(invoice.getPdfUrl())
                .build();
    }
}
