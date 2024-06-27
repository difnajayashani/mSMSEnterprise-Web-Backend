package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueue;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueueTest;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.SMSRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.BlacklistRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class NewSMSService {
    @Autowired
    SMSRepository smsRepository;
    @Autowired
    BlacklistRepository blacklistRepository;
    @Autowired
    SendQueueRepository sendQueueRepository;
    @Autowired
    SendQueueTestRepository sendQueueTestRepository;
    @Autowired
    AliasService aliasService;
    @Autowired
    LoggingService loggingService;

    public Map<String, String> getRecipientType(String recipient) {
        recipient = recipient.replaceAll("[^0-9]", "");
        recipient = recipient.replaceFirst("^0+(?!$)", ""); // Remove leading zeros

        String type;

        if (recipient.length() < 9) {
            type = "0";
            recipient = recipient;
        } else if (recipient.matches("^(71)[0-9]{7}$")) {
            recipient = "94" + recipient;
            type = "M2M";
        } else if (recipient.matches("^(9471)[0-9]{7}$")) {
            type = "M2M";
            recipient = recipient;
        } else if (recipient.matches("^(70)[0-9]{7}$")) {
            recipient = "94" + recipient;
            type = "M2M";
        } else if (recipient.matches("^(9470)[0-9]{7}$")) {
            type = "M2M";
            recipient = recipient;
        } else if (recipient.matches("^(7)[3][0-9]{7}$") || recipient.matches("^(7)[9][0-9]{7}$") || recipient.matches("^(947)[3][0-9]{7}$") || recipient.matches("^(947)[9][0-9]{7}$")) {
            type = "0";
            recipient = recipient;
        } else if (recipient.matches("^(7)[2-9][0-9]{7}$")) {
            recipient = "94" + recipient;
            type = "M2O";
        } else if (recipient.matches("^(947)[2-9][0-9]{7}$")) {
            type = "M2O";
            recipient = recipient;
        } else if (recipient.matches("^(94)[0-9]{9}$") && !recipient.startsWith("947")) {
            type = "M2O";
            recipient = recipient;
        } else if (!recipient.startsWith("94")) {
            Map<String, Integer> SL_AreaCode = new HashMap<>();
            SL_AreaCode.put("Ampara", 63);
            SL_AreaCode.put("Anuradhapura", 25);
            SL_AreaCode.put("Avissawella", 36);
            SL_AreaCode.put("Badulla", 55);
            SL_AreaCode.put("Bandarawela", 57);
            SL_AreaCode.put("Batticaloa", 65);
            SL_AreaCode.put("Chilaw", 32);
            SL_AreaCode.put("Colombo", 11);
            SL_AreaCode.put("Galle", 91);
            SL_AreaCode.put("Gampaha", 33);
            SL_AreaCode.put("Hambantota", 47);
            SL_AreaCode.put("Hatton", 51);
            SL_AreaCode.put("Jaffna", 21);
            SL_AreaCode.put("Kalmunai", 67);
            SL_AreaCode.put("Kalutara", 34);
            SL_AreaCode.put("Kandy", 81);
            SL_AreaCode.put("Kegalle", 35);
            SL_AreaCode.put("Kurunegala", 37);
            SL_AreaCode.put("Mannar", 23);
            SL_AreaCode.put("Matale", 66);
            SL_AreaCode.put("Matara", 41);
            SL_AreaCode.put("Nawalapitiya", 54);
            SL_AreaCode.put("Negombo", 31);
            SL_AreaCode.put("Nuwara Eliya", 52);
            SL_AreaCode.put("Panadura", 38);
            SL_AreaCode.put("Polonnaruwa", 27);
            SL_AreaCode.put("Ratnapura", 45);
            SL_AreaCode.put("Trincomalee", 26);
            SL_AreaCode.put("Vavuniya", 24);

            if (recipient.length() == 9 && SL_AreaCode.containsKey(recipient.substring(0, 2))) {
                recipient = "94" + recipient;
                type = "M2O";
            } else {
                recipient = "00" + recipient;
                type = "IDD";
            }
        } else {
            recipient = recipient;
            type = "0";
        }

        Map<String, String> array = new HashMap<>();
        array.put("recipient", recipient);
        array.put("type", type);
        return array;
    }

    public String sendMessage(String recipient, String type, String message, String aliasEncoded, Integer esmClass, Integer stopAd, Integer customer, Integer userId, Integer userType, Integer promoTax) {
        String appType = "WEB";
        boolean sendSMS = true;

        String alias = URLDecoder.decode(aliasEncoded);

        if (promoTax == 1) {
            List<Map<String,Object>> blacklistAliasList = blacklistRepository.findAliasAndMsisdn(recipient);

            if (!blacklistAliasList.isEmpty()) {
                String blacklistAliasString = (String) blacklistAliasList.get(0).get("alias");
                String[] blacklistAlias = blacklistAliasString.split("\\|");

//                if (Arrays.asList(blacklistAlias).contains(alias) || Arrays.asList(blacklistAlias).contains("ALL")) {
//                    sendSMS = false;
//                    return "OPTEDOUT";
//                }
//
//                if (blacklistAliasString.equals("ALL")) {
//                    sendSMS = false;
//                    return "OPTEDOUT";
//                }
            }
        }
        String dbTable = getQdBTable(customer);

        if(sendSMS && dbTable.equals("SendQueue")){
            LocalDateTime timeSubmitted = LocalDateTime.now();
            sendQueueRepository.insertIntoEsmTxQueue(message, recipient, alias, customer, userId, appType, esmClass, 1, 1, promoTax,timeSubmitted);
        }else if(sendSMS && dbTable.equals("SendQueueTest")){
            Date date = new Date();
            sendQueueTestRepository.insertIntoSendQueueTest(message, recipient, alias, customer, userId, date, appType, esmClass, 1, 1, promoTax);
        }
        return "SUCCESS";

    }

    public String getQdBTable(Integer customer) {
        String dBTable;

        if (customer == 42682340) {
            dBTable = "send_queue_ceb";
        } else if (customer == 24794895) {
            dBTable = "send_queue_ceylinco";
        } else if (customer == 80808080) {
            dBTable = "send_queue_madv";
        } else if (customer == 9999991) {
            dBTable = "SendQueueTest";
        } else {
            dBTable = "SendQueue";
        }

        return dBTable;
    }

    public String sendTestMessage(String recipient, String type, String message, String aliasEncode, Integer esmClass, Integer stopAd, Integer customer, Integer userId, int userType, Integer promoTax) {
        List<Map<String, Object>> userBalances = smsRepository.getUserBalances(userId);

        String appType = "WEB";
        boolean sendSMS = true;
        String alias = URLDecoder.decode(aliasEncode);
        String status = "";
        if (promoTax == 1) {
            List<Map<String,Object>> blacklists = blacklistRepository.findAliasAndMsisdn(recipient);
            if (!blacklists.isEmpty()) {
                Map<String, Object> firstBlacklistEntry = blacklists.get(0);
                String blacklistAliasString = (String) firstBlacklistEntry.get("alias");
                String[] blacklistAlias = blacklistAliasString.split("\\|");

                if (Arrays.asList(blacklistAlias).contains(alias) || Arrays.asList(blacklistAlias).contains("ALL")) {
                    sendSMS = false;
                    status = "OPTEDOUT";
                }
            }
        }

        if (sendSMS) {
            String dbTable = getQdBTable(customer);
            if(Objects.equals(dbTable, "SendQueue")){
                LocalDateTime timeSubmitted = LocalDateTime.now();
                if ("M2M".equals(type) && (int) userBalances.get(0).get("m2m_balance") > 0) {
                    sendQueueRepository.insertIntoEsmTxQueue(message, recipient, alias, customer, userId, appType, esmClass, 1, 1, promoTax, timeSubmitted);
                    smsRepository.updateM2MBalance(userId);
                    status = "SUCCESS";
                } else if ("M2O".equals(type) && (int) userBalances.get(0).get("m2o_balance") > 0 && recipient.length() == 11) {
                    sendQueueRepository.insertIntoEsmTxQueue(message, recipient, alias, customer, userId, appType, esmClass, 1, 1, promoTax, timeSubmitted);
                    smsRepository.updateM2OBalance(userId);
                    status = "SUCCESS";
                } else if ("IDD".equals(type) && (int) userBalances.get(0).get("idd_balance") > 0) {
                    sendQueueRepository.insertIntoEsmTxQueue(message, recipient, alias, customer, userId, appType, esmClass, 1, 1, promoTax, timeSubmitted);
                    smsRepository.updateIDDBalance(userId);
                    status = "SUCCESS";
                } else {
                    status = "FAILED";
                }
            } else if (Objects.equals(dbTable, "SendQueueTest")) {
                Date date = new Date();
                if ("M2M".equals(type) && (int) userBalances.get(0).get("m2m_balance") > 0) {
                    sendQueueTestRepository.insertIntoSendQueueTest(message, recipient, alias, customer, userId,date, appType, esmClass, 1, 1, promoTax);
                    smsRepository.updateM2MBalance(userId);
                    status = "SUCCESS";
                } else if ("M2O".equals(type) && (int) userBalances.get(0).get("m2o_balance") > 0 && recipient.length() == 11) {
                    sendQueueTestRepository.insertIntoSendQueueTest(message, recipient, alias, customer, userId,date, appType, esmClass, 1, 1, promoTax);
                    smsRepository.updateM2OBalance(userId);
                    status = "SUCCESS";
                } else if ("IDD".equals(type) && (int) userBalances.get(0).get("idd_balance") > 0) {
                    sendQueueTestRepository.insertIntoSendQueueTest(message, recipient, alias, customer, userId,date, appType, esmClass, 1, 1, promoTax);
                    smsRepository.updateIDDBalance(userId);
                    status = "SUCCESS";
                } else {
                    status = "FAILED";
                }
            }
        }
        return status;
    }

    public Map<String,Object> loadFromCsv(String fileName,String aliasOnnet, String aliasOffnet, Boolean esm_lass, Boolean remove_duplicate, Boolean stop_ad, Integer customer, String userName, Integer userId, Integer userType, String sessionId, Integer enableIdd, Integer enableM2o, Integer enableM2all,Boolean non_promoTax) {
        String inputFileName = "C:/Users/saranjanr/Desktop/Esms_files/" + fileName;
        String response = "";
        String status = "";
        int count = 0;
        int validCount = 0;
        int invalidCount = 0;
        int validSentCount = 0;
        int duplicateCount = 0;
        int optedoutCount = 0;
        List<String> validRecipientList = new ArrayList<>();
        Map<String,Object> result = new HashMap<>();
        String sourceIP = "";
        String alias;
        String log;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            Integer esmClass = esm_lass ? 3 : 0;
            int removeDuplicate = remove_duplicate ? 1 : 0;
            Integer stopAd = stop_ad ? 1 : 0;
            int promoTax = non_promoTax ? 1 : 0;

            String verifiedAlias1 = aliasService.verifyAlias(customer,userId,aliasOnnet);
            String verifiedAlias2 = aliasService.verifyAlias(customer,userId,aliasOffnet);
            if(Objects.equals(verifiedAlias1, "INVALID") || Objects.equals(verifiedAlias2, "INVALID")){
                log = "Invalid Alias is used";
                loggingService.logActivity(sourceIP,sessionId, customer, userName,"load_from_csv",log);

                result.put("response","INVALID_ALIAS");
                result.put("status","FAILED");
            }else {
                String line;
                while ((line = br.readLine()) != null) {
                    boolean sendSMS = true;
                    String[] row = line.split(",");
                    if (row.length != 2) {
                        // Handle invalid row format
                        continue;
                    }
                    String recep = row[0];
                    String message = row[1];

                    Map<String, String> array = getRecipientType(recep);
                    String recipient = array.get("recipient");
                    String type = array.get("type");

                    if (type.equals("0")) {
                        invalidCount++;
                    } else if (enableIdd == 0 && type.equals("IDD")) {
                        invalidCount++;
                    } else if (enableM2o == 0 && enableM2all == 0 && type.equals("M2O")) {
                        invalidCount++;
                    }else {
                        if (type.equals("M2M")) {
                            alias = aliasOnnet;
                        } else {
                            alias = aliasOffnet;
                        }

                        if (promoTax == 1 || (promoTax == 0 && stopAd == 1)) {
                            message = message + " *StopAd? SMS NO " + alias + " to 717781111";
                        }

                        if (esmClass == 3) {
                            byte[] utf16Bytes = message.getBytes(StandardCharsets.UTF_16);
                            StringBuilder hexString = new StringBuilder();
                            for (byte b : utf16Bytes) {
                                String hex = String.format("%02X", b);
                                hexString.append(hex);
                            }
                            message = hexString.toString().toUpperCase();
                        }

                        validCount++;

                        if (removeDuplicate == 1) {
                            if (validRecipientList.contains(recipient)) {
                                sendSMS = false;
                            }
                        }

                        if (sendSMS) {
                            if (userType == 0 || userType == 101) {
                                String res = send_message_selective_bulk(message, recipient, type, alias, esmClass, stopAd, customer, userId, userType, promoTax);
                                if (res.equals("OPTEDOUT")) {
                                    optedoutCount++;
                                } else {
                                    validSentCount++;
                                    response = "SUCCESS";
                                }
                                status = "SUCCESS";
                            } else if (userType == 10) {
                                String res = send_test_message_selective_bulk(message, recipient, type, alias, esmClass, stopAd, customer, userId, userType, promoTax);
                                if (res.equals("OPTEDOUT")) {
                                    optedoutCount++;
                                } else {
                                    validSentCount++;
                                    response = "SUCCESS";
                                }
                                status = "SUCCESS";
                            }
                        }
                        validRecipientList.add(recipient);
                    }
                    count++;
                }
                HashSet<String> uniqueValidRecipients = new HashSet<>(validRecipientList);
                int uniqueValidRecipientCount = uniqueValidRecipients.size();

                duplicateCount = validCount - uniqueValidRecipientCount;
            }
            log = "status - " + status + "| response - " + response + "| count - " + count + "| valid_count - " + validCount + "| valid_sent_count - " + validSentCount + "| invalid_count - " + invalidCount + "| duplicate_count - " + duplicateCount + "| optedout_count - " + optedoutCount;
            loggingService.logActivity(sourceIP,sessionId, customer, userName,"load_from_csv",log);

            result.put("status",status);
            result.put("response",response);
            result.put("count",count);
            result.put("validCount",validCount);
            result.put("validSentCount",validSentCount);
            result.put("invalidCount",invalidCount);
            result.put("duplicateCount",duplicateCount);
            result.put("optedoutCount",optedoutCount);
        } catch (IOException e) {
            log = "status - FAILED | response - Error loading file " + inputFileName + " - " + e.getMessage();
            loggingService.logActivity(sourceIP,sessionId, customer, userName,"load_from_csv",log);

            result.put("status","FAILED");
            result.put("response","Error loading file" + inputFileName + ":" + e.getMessage());
        }
        return result;
    }

    private String send_test_message_selective_bulk(String message, String recipient, String type, String aliasEncoded, Integer esmClass, Integer stopAd, Integer customer, Integer userId, Integer userType, Integer promoTax) {
        String appType = "WEB";
        boolean sendSMS = true;
        String status = "";
        String alias = URLDecoder.decode(aliasEncoded);

        if (promoTax == 1) {
            List<Map<String,Object>> blacklistAliasList = blacklistRepository.findAliasAndMsisdn(recipient);

            if (!blacklistAliasList.isEmpty()) {
                String blacklistAliasString = (String) blacklistAliasList.get(0).get("alias");
                String[] blacklistAlias = blacklistAliasString.split("\\|");

//                if (Arrays.asList(blacklistAlias).contains(alias) || Arrays.asList(blacklistAlias).contains("ALL")) {
//                    sendSMS = false;
//                    return "OPTEDOUT";
//                }
//
//                if (blacklistAliasString.equals("ALL")) {
//                    sendSMS = false;
//                    return "OPTEDOUT";
//                }
            }
        }
        String dbTable = getQdBTable(customer);

        if(sendSMS && dbTable.equals("SendQueue")){
            LocalDateTime timeSubmitted = LocalDateTime.now();
            sendQueueRepository.insertIntoEsmTxQueue(message, recipient, alias, customer, userId, appType, esmClass, 1, 1, promoTax, timeSubmitted);
            status = "SUCCESS";
        }else if (sendSMS && dbTable.equals("SendQueueTest")) {
            Date date = new Date();
            sendQueueTestRepository.insertIntoSendQueueTest(message, recipient, alias, customer, userId,date, appType, esmClass, 1, 1, promoTax);
            status = "SUCCESS";
        }
        return status;
    }

    private String send_message_selective_bulk(String message, String recipient, String type, String aliasEncoded, Integer esmClass, Integer stopAd, Integer customer, Integer userId, Integer userType, Integer promoTax) {
        String appType = "WEB";
        boolean sendSMS = true;
        String status = "";

        String alias = URLDecoder.decode(aliasEncoded);

        if (promoTax == 1) {
            List<Map<String,Object>> blacklistAliasList = blacklistRepository.findAliasAndMsisdn(recipient);

            if (!blacklistAliasList.isEmpty()) {
                String blacklistAliasString = (String) blacklistAliasList.get(0).get("alias");
                String[] blacklistAlias = blacklistAliasString.split("\\|");

//                if (Arrays.asList(blacklistAlias).contains(alias) || Arrays.asList(blacklistAlias).contains("ALL")) {
//                    sendSMS = false;
//                    return "OPTEDOUT";
//                }
//
//                if (blacklistAliasString.equals("ALL")) {
//                    sendSMS = false;
//                    return "OPTEDOUT";
//                }
            }
        }
        String dbTable = getQdBTable(customer);

        if(sendSMS && dbTable.equals("SendQueue")){
            LocalDateTime timeSubmitted = LocalDateTime.now();
            sendQueueRepository.insertIntoEsmTxQueue(message, recipient, alias, customer, userId, appType, esmClass, 1, 1, promoTax,timeSubmitted);
            status = "SUCCESS";
        } else if (sendSMS && dbTable.equals("SendQueueTest")) {
            Date date = new Date();
            sendQueueTestRepository.insertIntoSendQueueTest(message, recipient, alias, customer, userId,date, appType, esmClass, 1, 1, promoTax);
            status = "SUCCESS";
        }
        return status;
    }
}