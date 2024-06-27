package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.Alias;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.Campaign;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.CampaignArchive;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueue;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueueTest;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.*;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.BlacklistRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class CampaignService {

    @Autowired
    NumbersRepository numbersRepository;
    @Autowired
    AliasRepository aliasRepository;
    @Autowired
    CampaignRepository campaignRepository;
    @Autowired
    BlacklistRepository blacklistRepository;
    @Autowired
    SendQueueRepository sendQueueRepository;
    @Autowired
    SendQueueTestRepository sendQueueTestRepository;
    @Autowired
    CampaignArchiveRepository campaignArchiveRepository;

    @Autowired
    TestUserRepository testUserRepository;
    public List<Map<String, Object>> getNumbers(Integer campaignNumberListId) {
        return numbersRepository.getNumbers(campaignNumberListId);
    }

    public String verifyAlias(Integer customer, Integer userId, String campaignAlias) {
        String output = "INVALID";
        Alias alias;
        if(customer == 80808080){
            alias = aliasRepository.existsByCustomerAndAlias(customer,campaignAlias);
        }else {
            alias = aliasRepository.existsByCustomerAndUserIdAndAlias(customer,userId,campaignAlias);
        }

        if(alias != null){
            output = "VALID";
        }
        return output;
    }

    public Integer createCampaign(String campaignName, String campaignMessage, Integer campaignNumberListId, Date campaignScheduleDate, Integer campaignStatus, String campaignAlias, Integer customer, Integer userId, Integer userType, int esmClass, int stopAd) {
        Campaign campaign = campaignRepository.createCampaign(campaignName,campaignMessage,campaignNumberListId,campaignScheduleDate,campaignStatus,campaignAlias,customer,userId,esmClass,stopAd);
        return campaign.getId();
    }

    public List<String> checkOptOutBulk(List<String> numbersChunkOneMsisdn, String campaignAlias) {
        return blacklistRepository.checkOptOutBulk(Collections.singletonList(numbersChunkOneMsisdn),campaignAlias);
    }

    public List<Map<String,Object>> getAllCampaigns(int customer, int userId, int userType) {
        if (userType == 0) {
            return campaignRepository.getAllCampaignsForCustomer(customer);
        } else if (userType == 101 || userType == 10) {
            return campaignRepository.getAllCampaignsForCustomerAndUser(customer,userId);
        }
        return Collections.emptyList();
    }

    public String updateCampaign(Integer campaignId, String campaignAlias, Date campaignScheduleDate, Integer campaignNumberListId, Integer campaignStatus, String campaignMessage, int esmClass, int stopAd, int promoTax, Integer customer) {
        Integer uCampaign = campaignRepository.updateCampaign(campaignAlias,campaignScheduleDate,campaignNumberListId,campaignStatus,campaignMessage,campaignId);
        String dbTable = getQdBTable(customer);

        Integer uSendQueue = 0;
        if(Objects.equals(dbTable, "SendQueue")){
            uSendQueue = sendQueueRepository.updateSendQueue(campaignScheduleDate,campaignStatus,campaignId);
        } else if (Objects.equals(dbTable, "SendQueueTest")) {
            LocalDateTime localDateTime = campaignScheduleDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            uSendQueue = sendQueueTestRepository.updateSendQueue(localDateTime,campaignStatus,campaignId);
        }
        if(uCampaign == 1 && uSendQueue > 0){
            return "EDIT";
        }else {
            return "failed";
        }
    }

    public String deleteCampaign(int customer, int campaignId) {
        List<Campaign> camp = campaignRepository.verify_campaign(customer,campaignId);
        if(camp == null){
            return "INVALID CAMPAIGN";
        }else {
            String dbTable = getQdBTable(customer);
            if(Objects.equals(dbTable, "SendQueue")){
                sendQueueRepository.deleteByCampaignId(campaignId);
            } else if (Objects.equals(dbTable, "SendQueueTest")) {
                sendQueueTestRepository.deleteByCampaignId(campaignId);
            }
            List<Campaign> campaigns = campaignRepository.findAllById(Collections.singleton(campaignId));
            List<CampaignArchive> campaignArchives = new ArrayList<>();

            for (Campaign campaign : campaigns) {
                CampaignArchive campaignArchive = new CampaignArchive();
                campaignArchive.setName(campaign.getName());
                campaignArchive.setAlias(campaign.getAlias());
                campaignArchive.setNumberListId(campaign.getNumberListId());
                campaignArchive.setMessage(campaign.getMessage());
                campaignArchive.setScheduleDate(campaign.getScheduleDate());
                campaignArchive.setStatus(campaign.getStatus());
                campaignArchive.setCreatedDate(campaign.getCreatedDate());
                campaignArchive.setCustomer(campaign.getCustomer());
                campaignArchive.setUser(campaign.getUser());
                campaignArchive.setEsmClass(campaign.getEsmClass());
                campaignArchive.setStopAd(campaign.getStopAd());

                campaignArchives.add(campaignArchive);
            }

            campaignArchiveRepository.saveAll(campaignArchives);
            campaignRepository.deleteById(campaignId);
            return "SUCCESS";
        }
    }

    public String getQdBTable(int customer) {
        return switch (customer) {
            case 42682340 -> "send_queue_ceb";
            case 18281660 -> "send_queue_laughs";
            case 3163580 -> "send_queue_hcs";
            case 24794895 -> "send_queue_ceylinco";
            case 9999991 -> "SendQueueTest";
            case 80808080 -> "send_queue_madv";
            default -> "SendQueue";
        };
    }

    public String send_test_message_campaign(String recipient, String type, String campaignMessage, String campaignAlias, int esmClass, int stopAd, Date campaignScheduleDate, Integer campaignId, Integer customer, Integer userId, Integer userType, int promoTax) {
        String appType = "WEB";
        String status = "";
        List<Map<String,Object>> balances = testUserRepository.getTestUserDetails(userId);
        boolean sendSMS = true;
        if(promoTax == 1){
            String bl_status = checkOptOutSingle(recipient,campaignAlias);
            if(bl_status.equals("OPTEDOUT"))
            {
                sendSMS = false;
                status = "OPTEDOUT";
            }
        }

        String dbTable = getQdBTable(customer);
        if(sendSMS){
            if(Objects.equals(type, "M2M") && (Integer) balances.get(0).get("m2mBalance") > 0){
                if(Objects.equals(dbTable, "SendQueue")){
                    sendQueueRepository.insertIntoSendQueue(campaignMessage,recipient,campaignAlias,customer,userId,campaignScheduleDate,appType,esmClass,campaignId,1,promoTax);
                    testUserRepository.UpdateTUM2MBalance(userId);
                    status = "SUCCESS";
                } else if (Objects.equals(dbTable, "SendQueueTest")) {
                    sendQueueTestRepository.insertIntoSendQueueTest(campaignMessage,recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                    testUserRepository.UpdateTUM2MBalance(userId);
                    status = "SUCCESS";
                }
            }else if(Objects.equals(type, "M2O") && (Integer) balances.get(0).get("m2oBalance") > 0 && recipient.length() == 11){
                if(Objects.equals(dbTable, "SendQueue")){
                    sendQueueRepository.insertIntoSendQueue(campaignMessage,recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                    testUserRepository.UpdateTUM20Balance(userId);
                    status = "SUCCESS";
                }else if (Objects.equals(dbTable, "SendQueueTest")) {
                    sendQueueTestRepository.insertIntoSendQueueTest(campaignMessage,recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                    testUserRepository.UpdateTUM20Balance(userId);
                    status = "SUCCESS";
                }
            }else if(Objects.equals(type, "M2O") && (Integer) balances.get(0).get("iddBalance") > 0 && recipient.length() == 11){
                if(Objects.equals(dbTable, "SendQueue")){
                    sendQueueRepository.insertIntoSendQueue(campaignMessage, recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                    testUserRepository.UpdateTUIDDBalance(userId);
                    status = "SUCCESS";
                }else if (Objects.equals(dbTable, "SendQueueTest")) {
                    sendQueueTestRepository.insertIntoSendQueueTest(campaignMessage,recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                    testUserRepository.UpdateTUIDDBalance(userId);
                    status = "SUCCESS";
                }
            }else{
                status = "FAILED";
            }
        }
        return status;
    }

    private String checkOptOutSingle(String recipient, String campaignAlias) {
        List<String> response = blacklistRepository.checkOptOutSingle(recipient,campaignAlias);
        if(!response.isEmpty()){
            return "OPTEDOUT";
        }else {
            return "OPTEDIN";
        }
    }
}
