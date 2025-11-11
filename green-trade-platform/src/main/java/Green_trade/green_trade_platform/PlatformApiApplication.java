package Green_trade.green_trade_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlatformApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApiApplication.class, args);
    }

}
