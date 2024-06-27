package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.Type;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueTestRepository;
import lk.mobitel.msmsenterprisewebbeapi.service.CampaignService;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class CampaignController {
    @Autowired
    CampaignService campaignService;
    @Autowired
    SendQueueRepository sendQueueRepository;
    @Autowired
    SendQueueTestRepository sendQueueTestRepository;
    @Autowired
    LoggingService loggingService;

    @GetMapping("/getAllCampaigns")
    public ResponseEntity<List<Map<String,Object>>> getAllCampaigns(@RequestHeader HttpHeaders headers){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int userId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userId")));
        int userType = Integer.parseInt(Objects.requireNonNull(headers.getFirst("userType")));

        List<Map<String,Object>> campaigns = campaignService.getAllCampaigns(customer, userId, userType);

        if (campaigns != null) {
            return new ResponseEntity<>(campaigns, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/newCampaign")
    public ResponseEntity<String> addCampaign(@RequestBody CampaignRequest campaignRequest, @RequestHeader HttpHeaders headers){
        String sessionId = headers.getFirst("sessionId");
        Integer customer = Integer.valueOf(headers.getFirst("customer"));
        String userName = headers.getFirst("userName");
        Integer userId = Integer.valueOf(headers.getFirst("userId"));
        Integer userType = Integer.valueOf(headers.getFirst("userType"));

        String campaignName = campaignRequest.getCampaignName();
        String campaignAlias = campaignRequest.getSenderAlias();
        Date campaignScheduleDate = campaignRequest.getScheduleDate();
        Integer campaignNumberListId = campaignRequest.getNumberListId();
        Integer campaignStatus = campaignRequest.getStatus();
        String campaignMessage = campaignRequest.getMessage();
        Boolean campaignMessageOptOut = campaignRequest.getOptOutInformation();

        String sourceIP = "";
        String campaignStopAd = "on";
        String campaignPromoMessage = "on";
        int esmClass = campaignRequest.getEsmClass() ? 3 : 0;
        int promoTax = campaignRequest.getNonPromoTax() ? 1 : 0;
        int stopAd;

        if (campaignStopAd != null && (campaignStopAd.equals("on") || campaignStopAd.equals("checked"))) {
            stopAd = 1;
        } else {
            stopAd = 0;
        }

        if(promoTax == 1 || (promoTax == 0 && stopAd == 1)){
            campaignMessage += campaignMessageOptOut;
        }

        int optedOutCount = 0;
        int validSentCount = 0;
        int failedCount = 0;

        if (esmClass == 3) {
            byte[] utf16Bytes = campaignMessage.getBytes(StandardCharsets.UTF_16);

            StringBuilder hexString = new StringBuilder();
            for (byte b : utf16Bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex.toUpperCase());
            }

            campaignMessage = hexString.toString();
        }

        String log = "New Campaign - " + campaignName + " | Alias - " + campaignAlias + " | Campaign NumberList - " + campaignNumberListId;
        loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);

        List<Map<String,Object>> numbers = campaignService.getNumbers(campaignNumberListId);
        String status = "";

        if(numbers.isEmpty()){
            log = "New Campaign - " + campaignName + " | Alias - " + campaignAlias + " | Null NumberList";
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);
            status = "NULL_NUMBERLIST";
        }else {
            String verifyAlias = campaignService.verifyAlias(customer, userId, campaignAlias);
            if (Objects.equals(verifyAlias, "INVALID")) {
                log = "New Campaign - " + campaignName + " | Invalid Alias - " + campaignAlias;
                loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);
                status = "INVALID_ALIAS";
            } else {
                log = "New Campaign - " + campaignName + " | Alias - " + campaignAlias + " | Campaign NumberList - " + campaignNumberListId + " | Schedule date - " + campaignScheduleDate;
                loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);

                Integer campaignId = campaignService.createCampaign(campaignName, campaignMessage, campaignNumberListId, campaignScheduleDate, campaignStatus, campaignAlias, customer, userId, userType, esmClass, stopAd);
                if (campaignId == null) {
                    log = "New Campaign - " + campaignName + " | Campaign Creation Failed.";
                    loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);
                    status = "CREATE_CAMPAIGN_FAILED";
                } else {
                    log = "New Campaign - " + campaignName + " | New Campaign created.";
                    loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);
                    status = "CREATE_CAMPAIGN_SUCCESS";

                    int chunkSize = 1000;
                    int size1 = (numbers.size() + chunkSize - 1) / chunkSize;

                    List<?> optOutNumbers = new ArrayList<>();
                    for (int Q = 0; Q < size1; Q++) {
                        List<Map<String,Object>> numbersChunkOne;
                        numbersChunkOne = numbers.subList(Q * chunkSize, Math.min((Q + 1) * chunkSize, numbers.size()));

                        List<Map<String,Object>> validRecipientArray = new ArrayList<>();
                        List<Map<String,Object>> verifyBlNumbersArray = new ArrayList<>();
                        List<String> numbersChunkOneMsisdn = new ArrayList<>();

                        for (Map<String,Object> number : numbersChunkOne) {
                            if (number.get("msisdn") != null) {
                                numbersChunkOneMsisdn.add((String) number.get("msisdn"));
                            }
                        }

                        if (promoTax == 1) {
                            optOutNumbers = campaignService.checkOptOutBulk(numbersChunkOneMsisdn, campaignAlias);
                            optedOutCount += optOutNumbers.size();
                        }

                        String appType = "WEB";
                        String dBTable = campaignService.getQdBTable(customer);

                        for (Map<String,Object> number : numbersChunkOne) {
                            if (number.get("msisdn") != null) {
                                boolean sendSMS = true;

                                if (promoTax == 1 && optOutNumbers.contains(number.get("msisdn"))) {
                                    sendSMS = false;
                                }

                                if (sendSMS) {
                                    validSentCount++;
                                    validRecipientArray.add(number);

                                    String recipient = (String) number.get("msisdn");
                                    String type = (String) number.get("type");

                                    if (userType == 10) {
                                        String id = campaignService.send_test_message_campaign(recipient, type, campaignMessage,
                                                campaignAlias, esmClass, stopAd, campaignScheduleDate, campaignId,
                                                customer, userId, userType, promoTax);

                                        if (Objects.equals(id, "") || id.isEmpty()) {
                                            failedCount++;
                                        }
                                    } else {
                                        if(Objects.equals(dBTable, "SendQueue")){
                                            sendQueueRepository.insertIntoSendQueue(campaignMessage,recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                                        } else if (Objects.equals(dBTable, "SendQueueTest")) {
                                            Type type1 = null;
                                            if(Objects.equals(type, "IDD")){
                                                type1 = Type.IDD;
                                            } else if (Objects.equals(type, "M2O")) {
                                                type1 = Type.M2O;
                                            }else if(Objects.equals(type, "M2M")){
                                                type1 = Type.M2M;
                                            }
                                            sendQueueTestRepository.insertIntoSendQueueTest(campaignMessage,recipient, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                                        }
                                    }
                                }
                            }
                        }

                        if (userType == 0 || userType == 101){
//                            sendQueueRepository.insertIntoSendQueue(campaignMessage,recipient, type, campaignAlias, customer, userId, campaignScheduleDate, appType, esmClass, campaignId, 1, promoTax);
                        }
                    }

//                   Integer result = sendQueueRepository.insertIntoEsmTxQueue(campaignMessage,"9999991",campaignAlias,customer,userId,"Test",esmClass,campaignId,campaignStatus,2, LocalDateTime.now());
                }
            }
        }
        log = "New Campaign - " + campaignName+ " | Campaign Status - " + status + " | Valid_sent_count - " + validSentCount + " | Optedout_count - " + optedOutCount + " | Failed_count - " + failedCount;
        loggingService.logActivity(sourceIP, sessionId, customer, userName, "create_campaign",log);

        System.out.println("response:::::" + status);
        if(status.equals("CREATE_CAMPAIGN_SUCCESS")){
            return ResponseEntity.ok(status);
        }else {
            return ResponseEntity.status(300).body(status);
        }
    }

    @PutMapping("/updateCampaign")
    public ResponseEntity<?> updateCampaign(@RequestBody CampaignRequest campaignRequest, @RequestHeader HttpHeaders headers){
        String sessionId = headers.getFirst("sessionId");
        Integer customer = Integer.valueOf(headers.getFirst("customer"));
        String userName = headers.getFirst("userName");
        Integer userId = Integer.valueOf(headers.getFirst("userId"));
        Integer userType = Integer.valueOf(headers.getFirst("userType"));

        String campaignName = campaignRequest.getCampaignName();
        String campaignAlias = campaignRequest.getSenderAlias();
        Date campaignScheduleDate = campaignRequest.getScheduleDate();
        Integer campaignNumberListId = campaignRequest.getNumberListId();
        Integer campaignStatus = campaignRequest.getStatus();
        String campaignMessage = campaignRequest.getMessage();
        Boolean campaignMessageOptOut = campaignRequest.getOptOutInformation();

        String sourceIP = "";
        String campaignStopAd = "on";
        String campaignPromoMessage = "on";
        int esmClass = campaignRequest.getEsmClass() ? 3 : 0;
        int promoTax = campaignRequest.getNonPromoTax() ? 0 : 1;

        int stopAd;
        if (campaignStopAd != null && (campaignStopAd.equals("on") || campaignStopAd.equals("checked"))) {
            stopAd = 1;
        } else {
            stopAd = 0;
        }

        if(promoTax == 1 || (promoTax == 0 && stopAd == 1)){
            campaignMessage += campaignMessageOptOut;
        }

        Integer optedOutCount = 0;
        int validSentCount = 0;
        Integer failedCount = 0;
        if (esmClass == 3) {
            try {
                // Convert the campaign_message to UTF-16 encoding
                byte[] utf16Bytes = campaignMessage.getBytes("UTF-16");

                // Convert the UTF-16 bytes to hexadecimal string
                StringBuilder hexString = new StringBuilder();
                for (byte b : utf16Bytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex.toUpperCase());
                }

                // Assign the hexadecimal string back to the campaign_message
                campaignMessage = hexString.toString();
                System.out.println("campaignMessage:::" + campaignMessage);
            } catch (UnsupportedEncodingException e) {
                // Handle encoding exception
                e.printStackTrace();
            }
        }

        String log = "New Campaign - " + campaignName+ " | Alias - " + campaignAlias + " | Campaign NumberList - " + campaignNumberListId + " | Campaign Update Success with Valid_sent_count - " + validSentCount;
        loggingService.logActivity(sourceIP, sessionId, customer, userName, "update_campaign",log);

        String result = campaignService.updateCampaign(campaignRequest.getCampaignId(),campaignAlias,campaignScheduleDate,campaignNumberListId,campaignStatus,campaignMessage,esmClass,stopAd,promoTax,customer);
        if(result.equals("EDIT")){
            return ResponseEntity.ok(result);
        }else {
            return ResponseEntity.status(300).body(result);
        }
    }

    @DeleteMapping("/deleteCampaign")
    public ResponseEntity<?> deleteCampaign(@RequestHeader HttpHeaders headers){
        int customer = Integer.parseInt(Objects.requireNonNull(headers.getFirst("customer")));
        int campaignId = Integer.parseInt(Objects.requireNonNull(headers.getFirst("id")));
        String sessionId = headers.getFirst("sessionId");
        String userName = headers.getFirst("userName");
        String sourceIP = "";
        String result = campaignService.deleteCampaign(customer,campaignId);
        if(Objects.equals(result, "SUCCESS")){
            String log = "Campaign Id - " + campaignId + " has been deleted successfully.";
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "delete_campaign",log);
            return ResponseEntity.ok(result);
        }else {
            String log = "Campaign Id - " + campaignId + " is invalid.";
            loggingService.logActivity(sourceIP, sessionId, customer, userName, "delete_campaign",log);
            return ResponseEntity.status(300).body(result);
        }
    }

    public static List<List<Map<String, Object>>> chunkList(List<Map<String, Object>> list, int chunkSize) {
        List<List<Map<String, Object>>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignRequest{
        private Integer campaignId;
        private String campaignName;
        private Date scheduleDate;
        private String senderAlias;
        private Boolean esmClass;
        private Boolean nonPromoTax;
        private Integer status;
        private Boolean optOutInformation;
        private String message;
        private Integer numberListId;
    }
}
