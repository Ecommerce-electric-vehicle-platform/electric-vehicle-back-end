package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Invoice;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.service.InvoiceService;
import org.springframework.stereotype.Service;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    public Invoice createInvoice(Order order, String note, double taxRate, String pdfUrl) {
        Invoice newInvoice = Invoice.builder()
                .invoiceNumber("")
                .note(note)
                .concurrency("VND")
                .taxRate(taxRate)
                .pdfUrl(pdfUrl)
                .build();
        return null;
    }

    public String invoiceNumberGenerator() {
        return "";
    }
}
