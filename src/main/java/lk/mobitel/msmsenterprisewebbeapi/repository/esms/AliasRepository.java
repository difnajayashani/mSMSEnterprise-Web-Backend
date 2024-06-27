package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AliasRepository extends JpaRepository<Alias,Integer> {
    @Query("SELECT a.id FROM Alias a WHERE a.userId = :userId")
    Integer findByUserId(@Param("userId") Integer userId);

    @Query("SELECT a FROM Alias a WHERE a.customer = :customer AND a.alias = :alias")
    Alias existsByCustomerAndAlias(@Param("customer") Integer customer,@Param("alias") String alias);

    @Query("SELECT a FROM Alias a WHERE a.customer = :customer AND a.userId = :userId AND a.alias = :alias")
    Alias existsByCustomerAndUserIdAndAlias(@Param("customer") Integer customer,@Param("userId") Integer userId,@Param("alias") String alias);

    @Query("SELECT a FROM Alias a GROUP BY a.alias")
    List<Alias> getAlias();

    @Query("SELECT a FROM Alias a WHERE a.customer = :customer GROUP BY a.alias")
    List<Alias> getAliasWithCustomer(@Param("customer") Long customer);

    @Query("SELECT a FROM Alias a WHERE a.customer = :customer AND a.userId = :userId")
    List<Alias> getAliasWithCustomerAndUserId(@Param("customer") Long customer,@Param("userId") int userId);

    @Query("SELECT l.longNumber AS inbox FROM LongNumber l UNION SELECT s.shortcode AS inbox FROM Shortcode s GROUP BY inbox")
    List<Map<String, Object>> getInbox();

    @Query("SELECT longNumber AS inbox \n" +
            "FROM LongNumber \n" +
            "WHERE customer = :customer AND userId = :userId \n" +
            "UNION \n" +
            "SELECT shortcode AS inbox \n" +
            "FROM Shortcode \n" +
            "WHERE customer = :customer AND userId = :userId \n" +
            "GROUP BY inbox")
    List<Map<String, Object>> getInboxWithCustomerAndUserId(@Param("customer") int customer, @Param("userId") int userId);

    @Query("SELECT a FROM Alias a WHERE a.customer = 888 AND a.userId = 546888")
    List<Alias> getAliasCustomerAndUserId();
}
