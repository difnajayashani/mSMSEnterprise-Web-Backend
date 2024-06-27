package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Numbers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NumbersRepository extends JpaRepository<Numbers,Integer> {
    @Query(value = "SELECT msisdn AS msisdn, type AS type FROM numbers WHERE numberListId = :campaignNumberListId", nativeQuery = true)
    List<Map<String, Object>> getNumbers(@Param("campaignNumberListId") Integer campaignNumberListId);

    @Transactional
    @Modifying
    @Query("INSERT INTO Numbers(msisdn, type, numberListId) " +
            "VALUES (:recipient, :type, :id)")
    void addNumber(@Param("recipient") String recipient, @Param("type") String type, @Param("id") int id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Numbers n WHERE n.numberListId = :numberListId")
    void deleteNumbers(@Param("numberListId") Integer numberListId);
}
