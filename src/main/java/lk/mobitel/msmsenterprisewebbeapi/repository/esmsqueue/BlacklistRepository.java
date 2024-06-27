package lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue;

import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist,String> {
    @Query("SELECT b.msisdn as msisdn, b.alias as alias FROM Blacklist b WHERE b.msisdn = :recipient")
    List<Map<String,Object>> findAliasAndMsisdn(@Param("recipient") String msisdn);

    @Query(value = "SELECT msisdn FROM blacklist WHERE msisdn IN :numbersChunkOneMsisdn " +
            "AND (alias LIKE 'ALL' " +
            "OR alias LIKE '%|ALL' " +
            "OR alias LIKE 'ALL|%' " +
            "OR alias LIKE '%|ALL|%' " +
            "OR alias LIKE %:campaignAlias " +
            "OR alias LIKE CONCAT('%|', :campaignAlias) " +
            "OR alias LIKE CONCAT(:campaignAlias, '|%') " +
            "OR alias LIKE CONCAT('%|', :campaignAlias, '|%'))", nativeQuery = true)
    List<String> checkOptOutBulk(@Param("numbersChunkOneMsisdn") List<Object> msisdn,
                                              @Param("campaignAlias") String campaignAlias);

    @Query(value = "SELECT msisdn FROM blacklist WHERE msisdn IN :recipient " +
            "AND (alias LIKE 'ALL' " +
            "OR alias LIKE '%|ALL' " +
            "OR alias LIKE 'ALL|%' " +
            "OR alias LIKE '%|ALL|%' " +
            "OR alias LIKE %:campaignAlias " +
            "OR alias LIKE CONCAT('%|', :campaignAlias) " +
            "OR alias LIKE CONCAT(:campaignAlias, '|%') " +
            "OR alias LIKE CONCAT('%|', :campaignAlias, '|%'))", nativeQuery = true)
    List<String> checkOptOutSingle(@Param("recipient") String recipient, @Param("campaignAlias") String campaignAlias);
}
