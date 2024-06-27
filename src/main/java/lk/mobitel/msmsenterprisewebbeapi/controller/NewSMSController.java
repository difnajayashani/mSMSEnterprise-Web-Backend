package lk.mobitel.msmsenterprisewebbeapi.controller;

import lk.mobitel.msmsenterprisewebbeapi.service.AliasService;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lk.mobitel.msmsenterprisewebbeapi.service.NewSMSService;
import lk.mobitel.msmsenterprisewebbeapi.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class NewSMSController {
    @Autowired
    NewSMSService newSMSService;
    @Autowired
    AliasService aliasService;
    @Autowired
    UserService userService;
    @Autowired
    LoggingService loggingService;

    @PostMapping("/send-messages")
    public ResponseEntity<Object> sendMessagesFromArray(@RequestBody MessageRequest messageRequest, @RequestHeader HttpHeaders headers) throws UnsupportedEncodingException {

        String sessionId = headers.getFirst("sessionId");
        Integer customer = Integer.valueOf(headers.getFirst("customer"));
        String userName = headers.getFirst("userName");
        Integer userId = Integer.valueOf(headers.getFirst("userId"));
        int userType = Integer.parseInt(headers.getFirst("userType"));
        int enableIdd = Integer.parseInt(headers.getFirst("enableIdd"));
        int enableM2o = Integer.parseInt(headers.getFirst("enableM2o"));
        int enableM2all = Integer.parseInt(headers.getFirst("enableM2all"));

        String response = "";
        int count = 0;
        int validCount = 0;
        int invalidCount = 0;
        List<String> validRecipientArray = new ArrayList<>();
        int validSentCount = 0;
        int optedOutCount = 0;
        int duplicateCount = 0;

        String recipients = messageRequest.getRecipients();
        recipients = recipients.replace("\n", ",");
        List<String> recipientList = Arrays.asList(recipients.split(","));

        String message = messageRequest.getMessage();
        String alias = messageRequest.getAlias();

        Integer esmClass = messageRequest.getEsmClass() ? 3 : 0;
        Integer promoTax = messageRequest.getNonPromoTax() ? 1 : 0;
        Integer stopAd = messageRequest.getOptOutInformation() ? 1 : 0;
        String status = "";
        String sourceIP = "";
        String log = "Message - " + message + " | Alias - " + alias;
        loggingService.logActivity(sourceIP,sessionId, customer, userName,"send_messages_from_array",log);

        Object sessionDetailsObj = userService.getCISession(sessionId);
        if (Objects.equals(sessionDetailsObj, "INVALID SESSION")) {
            status = "ACCESS DENIED2";
        } else {
            if (esmClass == 3) {
                byte[] utf16Bytes = message.getBytes(StandardCharsets.UTF_16);
                StringBuilder hexString = new StringBuilder();
                for (byte b : utf16Bytes) {
                    String hex = String.format("%02X", b);
                    hexString.append(hex);
                }
                message = hexString.toString().toUpperCase();
            }

            String aliasResult = aliasService.verifyAlias(customer, userId, alias);

            if(Objects.equals(aliasResult, "INVALID")){
                log = "Invalid Alias is used";
                loggingService.logActivity(sourceIP,sessionId, customer, userName,"send_messages_from_array",log);

                response = "INVALID_ALIAS";
                status = "FAILED";
            }else {
                for(String recep : recipientList){
                    String addOn = "";
                    boolean sendSMS = true;
                    count ++;

                    Map<String,String> array = newSMSService.getRecipientType(String.valueOf(recep));
                    System.out.println("array:" + array);
                    String recipient = array.get("recipient");
                    String type = array.get("type");

                    System.out.println("array::" + array );
                    if(type.equals("0")){
                        invalidCount ++;
                    } else if (enableIdd == 0 && type.equals("IDD")) {
                        invalidCount ++;
                    } else if (enableM2o == 0 && enableM2all == 0 && type.equals("M2O")) {
                        invalidCount ++;
                    }else {
                        validCount ++;
                        if (validRecipientArray.contains(recipient)){
                            sendSMS = false;
                        }
                        if(sendSMS){
                            if(userType == 0 || userType == 101){
                                if(customer == 9999991){
                                    String aliasEncoded = URLEncoder.encode(alias, "UTF-8");
                                    String sendMessage = newSMSService.sendMessage(recipient, type, message, aliasEncoded, esmClass, stopAd, customer, userId, userType, promoTax);
                                    if(Objects.equals(sendMessage, "OPTEDOUT")){
                                        optedOutCount ++;
                                    }else {
                                        validSentCount ++;
                                        response = "TESTS";
                                    }
                                }else {
                                    String aliasEncoded = URLEncoder.encode(alias, "UTF-8");
                                    String sendMessage = newSMSService.sendMessage(recipient, type, message, aliasEncoded, esmClass, stopAd, customer, userId, userType, promoTax);
                                    if(Objects.equals(sendMessage, "OPTEDOUT")){
                                        optedOutCount ++;
                                    }else {
                                        validSentCount ++;
                                        response = "SUCCESS";
                                    }
                                    status = "SUCCESS";
                                }
                                status = "SUCCESS";
                            } else if (userType == 10) {
                                String aliasEncoded = URLEncoder.encode(alias, "UTF-8");
                                String sendMessage = newSMSService.sendTestMessage(recipient, type, message, aliasEncoded, esmClass, stopAd, customer, userId, userType, promoTax);
                                if(Objects.equals(sendMessage, "OPTEDOUT")){
                                    optedOutCount ++;
                                }else if (Objects.equals(sendMessage, "SUCCESS")){
                                    validSentCount ++;
                                    response = "SUCCESS";
                                }
                                status = "SUCCESS";
                            }
                        }
                        validRecipientArray.add(recipient);
                    }
                }

                Set<String> uniqueValidRecipientSet = new HashSet<>(validRecipientArray);
                int uniqueValidRecipientCount = uniqueValidRecipientSet.size();

                // Calculate duplicate count
                validCount = validRecipientArray.size();
                duplicateCount = validCount - uniqueValidRecipientCount;

                if(validCount == 0){
                    status = "FAILED";
                    response = "Invalid recipient number(s)...";
                }
            }
        }
        Map<String,Object> result = new HashMap<>();
        result.put("status",status);
        result.put("response",response);
        result.put("count",count);
        result.put("validCount",validCount);
        result.put("validSentCount",validSentCount);
        result.put("invalidCount",invalidCount);
        result.put("duplicateCount",duplicateCount);
        result.put("promoTax",message);
        result.put("optedOutCount",optedOutCount);

        log = "status - " + status + "| response - " + response + "| count - " + count + "| valid_count - " + validCount + "| valid_sent_count - " + validSentCount + "| invalid_count - " + invalidCount + "| duplicate_count - " + duplicateCount + "| promoTax - " + message + "| optedout_count - " + optedOutCount;
        loggingService.logActivity(sourceIP,sessionId, customer, userName,"send_messages_from_array",log);

        System.out.println("result" + result);
        if ("SUCCESS".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(300).body(result);
        }
    }

    @PostMapping("/send-bulk-messages")
    public ResponseEntity<Map<String,Object>> send_message_selective_bulk(@RequestBody BulkMessageRequest bulkMessageRequest, @RequestHeader HttpHeaders headers){
        String sessionId = headers.getFirst("sessionId");
        Integer customer = Integer.valueOf(headers.getFirst("customer"));
        String userName = headers.getFirst("userName");
        Integer userId = Integer.valueOf(headers.getFirst("userId"));
        Integer userType = Integer.valueOf(headers.getFirst("userType"));
        Integer enableIdd = Integer.valueOf(headers.getFirst("enableIdd"));
        Integer enableM2o = Integer.valueOf(headers.getFirst("enableM2o"));
        Integer enableM2all = Integer.valueOf(headers.getFirst("enableM2all"));
        String aliasOnnet = bulkMessageRequest.getSenderAlias();
        String aliasOffnet = bulkMessageRequest.getSenderAliasNonMobitel();

        String log;
        String sourceIP = "";
        String fileName = bulkMessageRequest.getFileName();
        Map<String,Object> responseArray = new HashMap<>();

        Object sessionDetailsObj = userService.getCISession(sessionId);
        if (Objects.equals(sessionDetailsObj, "INVALID SESSION")) {
            responseArray.put("status","ACCESS DENIED2");
        } else {
            if (fileName == null || fileName.isEmpty()) {
                log = "status - FAILED | response - Invalid File!";
                loggingService.logActivity(sourceIP,sessionId, customer, userName,"upload_ExcelSMS_file",log);

                responseArray.put("status", "FAILED");
                responseArray.put("response", "Invalid File! Please upload a correct file");
            } else {
                String[] filenameParts = fileName.split("\\.");
                String filenameWithoutExtension = filenameParts[0]; // Access the filename without the extension
                String fileExtension = filenameParts[1]; // Access the file extension

                if (Objects.equals(fileExtension, "csv")) {
                    Map<String, Object> result = newSMSService.loadFromCsv(fileName, aliasOnnet, aliasOffnet, bulkMessageRequest.getEsmClass(), bulkMessageRequest.getRemoveDuplicate(), bulkMessageRequest.getOptOutInformation(), customer, userName, userId, userType, sessionId, enableIdd, enableM2o, enableM2all, bulkMessageRequest.getNonPromoTax());
                    responseArray = result;
                } else if (Objects.equals(fileExtension, "xls") || Objects.equals(fileExtension, "xlsx")) {

                } else {
                    log = "status - FAILED | response - Invalid File!";
                    loggingService.logActivity(sourceIP,sessionId, customer, userName,"upload_ExcelSMS_file",log);

                    responseArray.put("status", "FAILED");
                    responseArray.put("response", "Invalid File! Please upload a correct file");
                }
            }
        }
        if(responseArray.get("status") == "SUCCESS"){
            return ResponseEntity.ok(responseArray);
        }else {
            return ResponseEntity.status(300).body(responseArray);
        }
    }

    @PostMapping("/validate_from_csv")
    public ResponseEntity<Object> validateFromCsv(@RequestBody ValidateFromCsv validateFromCsv, @RequestHeader HttpHeaders headers){
        String sessionId = headers.getFirst("sessionId");
        String inputFileName = "C:/Users/saranjanr/Desktop/Esms_files/" + validateFromCsv.getFileName();
        String response;
        int count = 0;
        int validCount = 0;
        int invalidCount = 0;
        int validSMSCount = 0;
        int invalidSMSCount = 0;
        String log;
        String sourceIP = "";
        List<String> validRecipientList = new ArrayList<>();
        Map<String,Object> result = new HashMap<>();

        Object sessionDetailsObj = userService.getCISession(sessionId);
        if (sessionDetailsObj != "INVALID SESSION") {
            try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] row = line.split(","); // Assuming CSV format
                    if (row.length == 0) {
                        invalidCount++;
                        invalidSMSCount++;
                    } else if (row.length == 1) {
                        invalidSMSCount++;
                    } else {
                        String recipient = row[0];
                        String message = row[1];

                        if (message != null && !message.isEmpty()) {
                            validSMSCount++;
                        } else {
                            invalidSMSCount++;
                        }

                        // Replace getRecipientType method call with your logic to retrieve recipient type
                        Map<String, String> array = newSMSService.getRecipientType(recipient);
                        String recep = array.get("recipient");
                        String type = array.get("type");

                        if (type.equals("0")) {
                            invalidCount++;
                        } else if (validateFromCsv.getEnableIdd() == 0 && type.equals("IDD")) { // Assuming enableIDD is defined elsewhere
                            invalidCount++;
                        } else {
                            validCount++;
                            validRecipientList.add(recep);
                        }
                    }

                    count++;
                }

                int uniqueValidRecipientCount = (int) validRecipientList.stream().distinct().count();
                int duplicateCount = validCount - uniqueValidRecipientCount;

                if (count == 0) {
                    log = "status - FAILED | response - File is blank";
                    loggingService.logActivity(sourceIP, sessionId, validateFromCsv.getCustomer(), validateFromCsv.getUserName(), "validate_from_csv",log);

                    response = "status - FAILED | response - File is blank";
                    result.put("status","FAILED");
                } else {
                    log = "status - SUCCESS | response - " + inputFileName + " | count - " + count + " | valid_count - " + validCount + " | invalid_count - " + invalidCount + " | duplicate_count - " + duplicateCount + " | valid_SMS_count - " + validSMSCount + " | invalid_SMS_count - " + invalidSMSCount;
                    loggingService.logActivity(sourceIP, sessionId, validateFromCsv.getCustomer(), validateFromCsv.getUserName(), "validate_from_csv",log);

//                response = "status - SUCCESS | response - " + inputFileName + " | count - " + count + " | valid_count - " + validCount + " | invalid_count - " + invalidCount + " | duplicate_count - " + duplicateCount + " | valid_SMS_count - " + validSMSCount + " | invalid_SMS_count - " + invalidSMSCount;
                    result.put("status","SUCCESS");
                    result.put("validCount",validCount);
                    result.put("invalidCount",invalidCount);
                    result.put("duplicateCount",duplicateCount);
                    result.put("invalidSMSCount",invalidSMSCount);
                }

            } catch (IOException e) {
                log = "status - FAILED | response - Error loading file " + inputFileName + e.getMessage();
                loggingService.logActivity(sourceIP, sessionId, validateFromCsv.getCustomer(), validateFromCsv.getUserName(), "validate_from_csv",log);

                response = "status - FAILED | response - Error loading file " + inputFileName + " - " + e.getMessage();
                result.put("status","FAILED");
            }

            if(result.get("status") == "FAILED"){
                return ResponseEntity.status(300).body(result);
            }else {
                return ResponseEntity.ok(result);
            }
        }else {
            return ResponseEntity.status(300).body("Invalid Session");
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageRequest {
        private String recipients;
        private String message;
        private String alias;
        private Boolean esmClass;
        private Boolean optOutInformation;
        private Boolean nonPromoTax;
        private Map<String,Object> sessionData;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkMessageRequest{
        private String fileName;
        private String senderAlias;
        private String senderAliasNonMobitel;
        private Boolean esmClass;
        private Boolean removeDuplicate;
        private Boolean optOutInformation;
        private Boolean nonPromoTax;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateFromCsv{
        private String fileName;
        private Integer enableIdd;
        private Integer customer;
        private String userName;
    }
}