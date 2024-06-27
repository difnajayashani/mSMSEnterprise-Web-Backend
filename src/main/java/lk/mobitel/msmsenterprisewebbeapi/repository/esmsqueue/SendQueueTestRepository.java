package lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueueTest;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;

@Repository
public interface SendQueueTestRepository extends JpaRepository<SendQueueTest,Long> {
    @Transactional
    @Modifying
    @Query("INSERT INTO SendQueueTest(message, recipient, alias, customer, user, applicationType, esmClass, campaignId, campaignStatus, messageType, timeSubmitted) " +
            "VALUES (:campaignMessage, :recipient, :campaignAlias, :customer, :userId, :appType, :esmClass, :campaignId, :campaignStatus, :promoTax, :campaignScheduleDate)")
    void insertIntoSendQueueTest(@Param("campaignMessage") String campaignMessage, @Param("recipient") String recipient, @Param("campaignAlias") String campaignAlias, @Param("customer") Integer customer, @Param("userId") Integer userId, @Param("campaignScheduleDate") Date campaignScheduleDate, @Param("appType") String appType, @Param("esmClass") int esmClass, @Param("campaignId") Integer campaignId, @Param("campaignStatus") Integer campaignStatus, @Param("promoTax") int promoTax);

    @Transactional
    void deleteByCampaignId(@Param("campaignId") int campaignId);

    @Transactional
    @Modifying
    @Query("UPDATE SendQueueTest sq SET sq.timeSubmitted = :campaignScheduleDate, sq.campaignStatus = :campaignStatus WHERE sq.campaignId = :campaignId")
    Integer updateSendQueue(@Param("campaignScheduleDate") LocalDateTime campaignScheduleDate, @Param("campaignStatus") Integer campaignStatus, @Param("campaignId") Integer campaignId);

}
