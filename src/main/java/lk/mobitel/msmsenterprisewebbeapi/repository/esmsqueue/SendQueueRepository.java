package lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;

@Repository
public interface SendQueueRepository extends JpaRepository<SendQueue, Long> {
    @Transactional
    @Modifying
    @Query("INSERT INTO SendQueue(message, recipients, alias, customer, user, applicationType, esmClass, campaignId, campaignStatus, messageType, timeSubmitted) " +
            "VALUES (:message, :recipients, :alias, :customer, :user, :applicationType, :esmClass, :campaignId, :campaignStatus, :messageType, :timeSubmitted)")
    Integer insertIntoEsmTxQueue(@Param("message") String message,
                              @Param("recipients") String recipients,
                              @Param("alias") String alias,
                              @Param("customer") Integer customer,
                              @Param("user") Integer user,
                              @Param("applicationType") String applicationType,
                              @Param("esmClass") Integer esmClass,
                              @Param("campaignId") Integer campaignId,
                              @Param("campaignStatus") Integer campaignStatus,
                              @Param("messageType") Integer messageType,
                              @Param("timeSubmitted") LocalDateTime timeSubmitted
    );

    @Transactional
    @Modifying
    @Query("UPDATE SendQueue sq SET sq.timeSubmitted = :campaignScheduleDate, sq.campaignStatus = :campaignStatus WHERE sq.campaignId = :campaignId")
    Integer updateSendQueue(@Param("campaignScheduleDate") Date campaignScheduleDate, @Param("campaignStatus") Integer campaignStatus, @Param("campaignId") Integer campaignId);

    @Transactional
    void deleteByCampaignId(@Param("campaignId") int campaignId);

    @Transactional
    @Modifying
    @Query("INSERT INTO SendQueue(message, recipients, alias, customer, user, applicationType, esmClass, campaignId, campaignStatus, messageType, timeSubmitted) " +
            "VALUES (:campaignMessage, :recipient, :campaignAlias, :customer, :userId, :appType, :esmClass, :campaignId, :campaignStatus, :promoTax, :campaignScheduleDate)")
    void insertIntoSendQueue(@Param("campaignMessage") String campaignMessage, @Param("recipient") String recipient, @Param("campaignAlias") String campaignAlias, @Param("customer") Integer customer, @Param("userId") Integer userId, @Param("campaignScheduleDate") Date campaignScheduleDate, @Param("appType") String appType, @Param("esmClass") int esmClass, @Param("campaignId") Integer campaignId, @Param("campaignStatus") Integer campaignStatus, @Param("promoTax") int promoTax);
}
