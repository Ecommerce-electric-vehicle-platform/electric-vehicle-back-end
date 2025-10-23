package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import Green_trade.green_trade_platform.model.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Page<Dispute> findAllByStatus(DisputeStatus disputeStatus, Pageable pageable);
}
