package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessage;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageCeb;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageCeylinco;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageInternal;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageMadv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface SentMessageRepository extends JpaRepository<SentMessage, Integer> {
    @Query("SELECT sm FROM SentMessage sm where sm.customer = :customer AND sm.status = 200")
    List<SentMessage> getSentMessages(@Param("customer") int customer);

    @Query("SELECT sm FROM SentMessage sm WHERE sm.customer = :customer AND sm.user = :userId AND sm.status = 200")
    List<SentMessage> getSentMessagesWithUserId(@Param("customer") int customer, @Param("userId") int userId);

    @Query("SELECT sc FROM SentMessageCeb sc where sc.customer = :customer AND sc.status = 200")
    List<SentMessageCeb> getSentMessagesCeb(@Param("customer") int customer);

    @Query("SELECT smc FROM SentMessageCeylinco smc where smc.customer = :customer AND smc.status = 200")
    List<SentMessageCeylinco> getSentMessagesCeylinco(@Param("customer") int customer);

    @Query("SELECT smi FROM SentMessageInternal smi where smi.customer = :customer AND smi.status = 200")
    List<SentMessageInternal> getSentMessagesInternal(@Param("customer") int customer);

    @Query("SELECT smm FROM SentMessageMadv smm where smm.customer = :customer AND smm.status = 200")
    List<SentMessageMadv> getSentMessagesMadv(@Param("customer") int customer);

    @Query("SELECT sc FROM SentMessageCeb sc WHERE sc.customer = :customer AND sc.user = :userId AND sc.status = 200")
    List<SentMessageCeb> getSentMessagesCebWithUserId(@Param("customer") int customer, @Param("userId") int userid);

    @Query("SELECT smc FROM SentMessageCeylinco smc WHERE smc.customer = :customer AND smc.user = :userId AND smc.status = 200")
    List<SentMessageCeylinco> getSentMessagesCeylincoWithUserId(@Param("customer") int customer, @Param("userId") int userid);

    @Query("SELECT smi FROM SentMessageInternal smi WHERE smi.customer = :customer AND smi.user = :userId AND smi.status = 200")
    List<SentMessageInternal> getSentMessagesInternalWithUserId(@Param("customer") int customer, @Param("userId") int userid);

    @Query("SELECT smm FROM SentMessageMadv smm WHERE smm.customer = :customer AND smm.user = :userId AND smm.status = 200")
    List<SentMessageMadv> getSentMessagesMadvWithUserId(@Param("customer") int customer, @Param("userId") int userid);

//    @Query("SELECT sm.sender AS recipient, sm.recipient AS sender,sm.status AS status, sm.time AS time, sm.message AS message FROM SentMessage sm WHERE sm.time BETWEEN :fromDate AND :toDate ORDER BY sm.time")
    @Query("SELECT count(sm) AS count FROM SentMessage sm WHERE sm.user = :userId AND sm.customer = :customer AND sm.timeSent BETWEEN :fromDate  AND :toDate")
    Integer download_sent_items_count(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("customer") Integer customer, @Param("userId") Integer userId);

    @Query("SELECT sm.recipient AS recipient, sm.alias AS alias, sm.type AS type, sm.timeSent AS timeSent, sm.message AS msg, sm.esmClass AS esmClass FROM SentMessage sm WHERE sm.customer = :customer AND sm.user = :userId AND sm.timeSent BETWEEN :fromDate AND :toDate ORDER BY sm.timeSent")
    List<Map<String, Object>> download_sent_items(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("customer") Integer customer, @Param("userId") Integer userId);

    @Query("SELECT count(sm) AS count FROM SentMessage sm WHERE sm.customer = :customer AND sm.timeSent BETWEEN :fromDate  AND :toDate")
    Integer download_sent_items_count_for_zeroUserType(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("customer") Integer customer);

    @Query("SELECT sm.recipient AS recipient, sm.alias AS alias, sm.type AS type, sm.timeSent AS timeSent, sm.message AS msg, sm.esmClass AS esmClass FROM SentMessage sm WHERE sm.customer = :customer AND sm.timeSent BETWEEN :fromDate AND :toDate ORDER BY sm.timeSent")
    List<Map<String, Object>> download_sent_items_forZeroUserType(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate, @Param("customer") Integer customer);
}
