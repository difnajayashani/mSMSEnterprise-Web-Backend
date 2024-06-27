package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.TestUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SMSRepository extends JpaRepository<TestUser,Integer> {
//    void insertMessage(String message, String recipient, String type, String alias, Integer customer, Integer userId, String appType, Integer esmClass, int i, int i1, Integer promoTax);

    @Query("SELECT tu.m2mBalance, tu.m2oBalance, tu.iddBalance FROM TestUser tu WHERE tu.user = :userId")
    List<Map<String, Object>> getUserBalances(@Param("userId") Integer userId);

//    @Query("SELECT b. FROM Blacklist b WHERE tu.user = :userId")
//    List<Map<String, Object>> getBlacklist(String recipient);

    @Query("UPDATE TestUser tu SET tu.m2mBalance=(tu.m2mBalance-1) WHERE tu.user = :userId")
    void updateM2MBalance(@Param("userId") Integer userId);

    @Query("UPDATE TestUser tu SET tu.m2mBalance=(tu.m2mBalance-1) WHERE tu.user = :userId")
    void updateM2OBalance(Integer userId);

    @Query("UPDATE TestUser tu SET tu.m2mBalance=(tu.m2mBalance-1) WHERE tu.user = :userId")
    void updateIDDBalance(Integer userId);
}
