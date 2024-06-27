package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessage;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageCeb;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageCeylinco;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageInternal;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.SentMessageMadv;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.SentMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SentMessageService {
    @Autowired
    SentMessageRepository sentMessageRepository;

    public List<?> getAllSentItems(int customer, int userid, int userType) {
        String sentSMSdBTable = getSentSMSdBTable(customer);
        List<SentMessage> sentMessages = new ArrayList<>();
        List<SentMessageCeb> sentMessageCebs = new ArrayList<>();
        List<SentMessageCeylinco> sentMessageCeylincos = new ArrayList<>();
        List<SentMessageInternal> sentMessageInternals = new ArrayList<>();
        List<SentMessageMadv> sentMessageMadvs = new ArrayList<>();

        if (userType == 0) {
            switch (sentSMSdBTable) {
                case "SentMessageCeb":
                    sentMessageCebs = sentMessageRepository.getSentMessagesCeb(customer);
                case "SentMessageCeylinco":
                    sentMessageCeylincos = sentMessageRepository.getSentMessagesCeylinco(customer);
                case "SentMessageInternal":
                    sentMessageInternals = sentMessageRepository.getSentMessagesInternal(customer);
                case "SentMessageMadv":
                    sentMessageMadvs = sentMessageRepository.getSentMessagesMadv(customer);
                default:
                    sentMessages =  sentMessageRepository.getSentMessages(customer);
            }
        } else if (userType == 101) {
            switch (sentSMSdBTable) {
                case "SentMessageCeb":
                    sentMessageCebs = sentMessageRepository.getSentMessagesCebWithUserId(customer, userid);
                case "SentMessageCeylinco":
                    sentMessageCeylincos = sentMessageRepository.getSentMessagesCeylincoWithUserId(customer, userid);
                case "SentMessageInternal":
                    sentMessageInternals = sentMessageRepository.getSentMessagesInternalWithUserId(customer, userid);
                case "SentMessageMadv":
                    sentMessageMadvs = sentMessageRepository.getSentMessagesMadvWithUserId(customer, userid);
                default:
                    sentMessages = sentMessageRepository.getSentMessagesWithUserId(customer, userid);
            }
        }

        for (SentMessage sentMessage : sentMessages) {
            String message = sentMessage.getMessage();
            Integer esmClass = sentMessage.getEsmClass();
            String convertedMessage;
            if (esmClass == 3) {
                convertedMessage = convertUCS4LEToUTF8(message);
            } else {
                convertedMessage = message.trim().replaceAll("\\s+", " ");
            }
            sentMessage.setMessage(convertedMessage);
            int messageType = sentMessage.getMessageType();

            if (messageType == 0) {
                sentMessage.setMessageType(2);
            } else {
                sentMessage.setMessageType(3);
            }
        }

        return sentMessages;
    }

    private String getSentSMSdBTable(int customer) {
        String dBTable;

        if (customer == 42682340) {
            dBTable = "SentMessageCeb";
        } else if (customer == 999999) {
            dBTable = "SentMessageInternal";
        } else if (customer == 24794895) {
            dBTable = "SentMessageCeylinco";
        } else if (customer == 80808080) {
            dBTable = "SentMessageMadv";
        } else {
            dBTable = "SentMessage";
        }

        return dBTable;
    }

    public Integer download_sent_items_count(LocalDateTime fromDate, LocalDateTime toDate, Integer customer, Integer userType, Integer userId) {
        String dbTable = getSentSMSdBTable(customer);
        Integer result = null;
        if(userType == 0){
            if(Objects.equals(dbTable, "SentMessage")){
                result = sentMessageRepository.download_sent_items_count_for_zeroUserType(fromDate,toDate,customer);
            }
        } else if (userType == 101 || userType == 10) {
            if(Objects.equals(dbTable, "SentMessage")){
                result = sentMessageRepository.download_sent_items_count(fromDate,toDate,customer,userId);
            }
        }
        return result;
    }

    public List<Map<String, Object>> download_sent_items(LocalDateTime fromDate, LocalDateTime toDate, Integer customer, Integer userType, Integer userId) {
        String dbTable = getSentSMSdBTable(customer);
        List<Map<String, Object>> modifiedResults = new ArrayList<>();
        List<Map<String, Object>> results = new ArrayList<>();
        if(userType == 101){
            if(Objects.equals(dbTable, "SentMessage")){
                results  = sentMessageRepository.download_sent_items(fromDate,toDate,customer,userId);
            }
        } else if (userType == 0) {
            if(Objects.equals(dbTable, "SentMessage")){
                results  = sentMessageRepository.download_sent_items_forZeroUserType(fromDate,toDate,customer);
            }
        }
        for(Map<String,Object> result : results){
            String message = (String) result.get("msg");
            Integer esmClass = (Integer) result.get("esmClass");
            String convertedMessage;

            if (esmClass == 3) {
                convertedMessage = convertUCS4LEToUTF8(message);
            } else {
                convertedMessage = message.trim().replaceAll("\\s+", " ");
            }
            Map<String, Object> modifiedResult = new HashMap<>();
            modifiedResult.put("message",convertedMessage);
            modifiedResult.put("type",result.get("type"));
            modifiedResult.put("recipient",result.get("recipient"));
            modifiedResult.put("alias",result.get("alias"));
            modifiedResult.put("timeSent",result.get("timeSent"));

            modifiedResults.add(modifiedResult);
        }
        return modifiedResults;
    }

    private String convertUCS4LEToUTF8(String ucs4le) {
        StringBuilder utf8Builder = new StringBuilder();

        for (int i = 0; i < ucs4le.length(); i += 4) {
            // Ensure that there are enough characters left in the string
            if (i + 4 <= ucs4le.length()) {
                String hex = ucs4le.substring(i, i + 4);
//                System.out.println("Hex substring: " + hex); // Log the hex substring

                try {
                    int codePoint = Integer.parseInt(hex, 16);
//                    System.out.println("Code point: " + codePoint); // Log the parsed code point

                    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    byteBuffer.putInt(codePoint);
                    byteBuffer.rewind();

                    CharBuffer charBuffer = Charset.forName("UTF-32LE").decode(byteBuffer);
                    utf8Builder.append(charBuffer.toString());
                } catch (NumberFormatException e) {
                    // Handle invalid hexadecimal string
                    e.printStackTrace();
                }
            }
        }

        return utf8Builder.toString().trim().replaceAll("\\s+", " ");
    }
}
