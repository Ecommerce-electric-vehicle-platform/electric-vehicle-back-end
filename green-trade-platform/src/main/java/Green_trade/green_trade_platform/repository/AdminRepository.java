package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUsername(String username);
}
