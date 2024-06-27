package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.ReceivedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ReceivedMessageRepository extends JpaRepository<ReceivedMessage,Integer> {
    @Query("SELECT longNumber AS inbox FROM LongNumber UNION SELECT shortcode AS inbox FROM Shortcode GROUP BY inbox")
    List<Map<String, Object>> getAllInboxes();

    @Query("SELECT longNumber AS inbox FROM LongNumber WHERE customer = :customer AND userId = :userId UNION SELECT shortcode AS inbox FROM Shortcode WHERE customer = :customer AND userId = :userId")
    List<Map<String, Object>> getAllInboxesWithUserId(@Param("customer") int customer,@Param("userId") int userId);

    @Query("SELECT rm FROM ReceivedMessage rm WHERE rm.recipient = :inbox")
    List<ReceivedMessage> getAllReceivedItems(@Param("inbox") String inbox);

    @Query("SELECT rm FROM ReceivedMessage rm WHERE rm.recipient = :currentInbox AND rm.time BETWEEN :fromDate AND :toDate ORDER BY rm.time")
    List<ReceivedMessage> downloadInboxWithinDates(@Param("currentInbox") String currentInbox, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
