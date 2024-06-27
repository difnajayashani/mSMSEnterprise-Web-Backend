package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile,Integer> {
    @Query("SELECT cp FROM CustomerProfile cp where cp.accountNo = :accountNo")
    CustomerProfile findByAccountNo(@Param("accountNo") Integer accountNo);

    @Query("SELECT cp.accountNo AS accountNo, cp.name AS name, cp.status AS status, cp.haltServices AS haltServices, cp.dateDeactivated AS dateDeactivated, cp.dateReactivated AS dateReactivated FROM CustomerProfile cp where cp.accountNo = :accountNo")
    Map<String, Object> getCustomerProfile(@Param("accountNo") Integer accountNo);
}
