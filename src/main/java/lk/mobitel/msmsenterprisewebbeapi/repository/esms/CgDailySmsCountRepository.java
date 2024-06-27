package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.CgDailySmsCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CgDailySmsCountRepository extends JpaRepository<CgDailySmsCount, Integer> {
    @Query("SELECT dsc.date AS date FROM CgDailySmsCount dsc where dsc.customer = :accountNo ORDER BY dsc.date DESC LIMIT 1")
    Map<String,Object> DateFromCgDailySmsCount(@Param("accountNo") String accountNo);
}
