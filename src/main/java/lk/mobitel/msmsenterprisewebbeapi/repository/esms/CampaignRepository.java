package lk.mobitel.msmsenterprisewebbeapi.repository.esms;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
    default Campaign createCampaign(String campaignName, String campaignMessage, Integer campaignNumberListId, Date campaignScheduleDate,
                                    Integer campaignStatus, String campaignAlias, Integer customer, Integer userId,
                                    int esmClass, int stopAd) {
        Campaign campaign = new Campaign();
        campaign.setName(campaignName);
        campaign.setMessage(campaignMessage);
        campaign.setNumberListId(campaignNumberListId);
        campaign.setScheduleDate(campaignScheduleDate);
        campaign.setStatus(campaignStatus);
        campaign.setAlias(campaignAlias);
        campaign.setCustomer(customer);
        campaign.setUser(userId);
        campaign.setEsmClass(esmClass);
        campaign.setStopAd(stopAd);
        campaign.setCreatedDate(new Date());
        return save(campaign);
    }

    @Query("SELECT c.id AS id, c.name AS campaignName, c.alias AS alias,c.numberListId AS numberListId,c.message AS message, c.scheduleDate AS scheduleDate, if(c.status = 1 ,'ACTIVE','PAUSED') AS campaignStatus, c.createdDate AS createdDate, c.customer AS customer, c.user AS user, c.esmClass AS esmClass, c.stopAd AS stopAd, u.userName AS userName FROM Campaign c INNER JOIN User u ON c.user = u.id WHERE c.customer = :customer ORDER BY c.scheduleDate DESC")
    List<Map<String,Object>> getAllCampaignsForCustomer(@Param("customer") int customer);

    @Transactional
    @Modifying
    @Query("UPDATE Campaign c SET c.alias = :campaignAlias, c.scheduleDate = :campaignScheduleDate, c.numberListId = :campaignNumberListId, c.status = :campaignStatus, c.message = :campaignMessage WHERE c.id = :campaignId")
    Integer updateCampaign(@Param("campaignAlias") String campaignAlias, @Param("campaignScheduleDate") Date campaignScheduleDate, @Param("campaignNumberListId") Integer campaignNumberListId, @Param("campaignStatus") Integer campaignStatus, @Param("campaignMessage") String campaignMessage, @Param("campaignId") Integer campaignId);

    @Query("SELECT c FROM Campaign c WHERE c.customer = :customer AND c.id = :campaignId")
    List<Campaign> verify_campaign(@Param("customer") int customer, @Param("campaignId") int campaignId);

    @Query("SELECT c.id AS id, c.name AS campaignName, c.alias AS alias,c.numberListId AS numberListId,c.message AS message, c.scheduleDate AS scheduleDate, if(c.status = 1 ,'ACTIVE','PAUSED') AS campaignStatus, c.createdDate AS createdDate, c.customer AS customer, c.user AS user, c.esmClass AS esmClass, c.stopAd AS stopAd, u.userName AS userName FROM Campaign c INNER JOIN User u ON c.user = u.id WHERE c.customer = :customer AND c.user = :userId ORDER BY c.scheduleDate DESC")
    List<Map<String, Object>> getAllCampaignsForCustomerAndUser(@Param("customer") int customer, @Param("userId") int userId);
}
