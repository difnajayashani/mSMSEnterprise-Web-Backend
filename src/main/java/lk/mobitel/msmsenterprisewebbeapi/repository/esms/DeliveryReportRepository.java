package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.DeliveryReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface DeliveryReportRepository extends JpaRepository<DeliveryReport,Integer> {
    @Query("SELECT count(dr) AS count FROM DeliveryReport dr WHERE dr.time BETWEEN :fromDate AND :toDate")
    Integer download_delivery_reports_count(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query("SELECT dr.sender AS recipient, dr.recipient AS sender,dr.status AS status, dr.time AS time, dr.message AS message FROM DeliveryReport dr WHERE dr.recipient= :alias AND dr.time BETWEEN :fromDate AND :toDate ORDER BY dr.time")
    List<Map<String, Object>> download_delivery_reports(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate, @Param("alias") String alias);
}
