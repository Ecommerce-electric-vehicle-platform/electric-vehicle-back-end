package Green_trade.green_trade_platform.repository;


import Green_trade.green_trade_platform.model.PackagePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackagePriceRepository extends JpaRepository<PackagePrice, Long> {
    @Query("SELECT p FROM PackagePrice p WHERE p.subscriptionPackage.id = :packageId")
    List<PackagePrice> findBySubscriptionPackageId(@Param("packageId") Long packageId);
}