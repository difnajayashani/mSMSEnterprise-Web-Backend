package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.esms.ReceivedMessage;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.ReceivedMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ReceivedMessageService {
    @Autowired
    ReceivedMessageRepository receivedMessageRepository;
    public List<Map<String,Object>> getAllInboxes(int customer, int userId) {
        List<Map<String,Object>> result;
        if(customer == 111111){
            result = receivedMessageRepository.getAllInboxes();
        }else {
            result = receivedMessageRepository.getAllInboxesWithUserId(customer,userId);
        }
        return result;
    }

    public List<ReceivedMessage> getAllReceivedItems(String inbox, int customer, int userId, int userType) {
        List<ReceivedMessage> results = receivedMessageRepository.getAllReceivedItems(inbox);

        List<ReceivedMessage> convertedMessages = new ArrayList<>();

        for (ReceivedMessage receivedMessage : results) {
            String message = receivedMessage.getMessage();
            int esmClass = receivedMessage.getEsmClass();
            String convertedMessage = "";

            if (esmClass == 3) {
                for (int i = 0; i < message.length(); ) {
                    String charStr = message.substring(i, i + 4);
                    int charValue = Integer.parseInt(charStr, 16);
                    char[] utf8Char = Character.toChars(charValue);

                    convertedMessage += new String(utf8Char);
                    i += 4;
                }
            } else {
                convertedMessage = message.trim().replaceAll("\\s+", " ");
            }

            receivedMessage.setMessage(convertedMessage);
            convertedMessages.add(receivedMessage);
        }

        return convertedMessages;
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

    public List<ReceivedMessage> downloadInboxWithinDates(String currentInbox, Date fromDate, Date toDate, int customer, int userId, int userType) {
        List<ReceivedMessage> results = receivedMessageRepository.downloadInboxWithinDates(currentInbox,fromDate,toDate);

        List<ReceivedMessage> convertedMessages = new ArrayList<>();

        for (ReceivedMessage receivedMessage : results) {
            String message = receivedMessage.getMessage();
            int esmClass = receivedMessage.getEsmClass();
            String convertedMessage = "";

            if (esmClass == 3) {
                for (int i = 0; i < message.length(); ) {
                    String charStr = message.substring(i, i + 4);
                    int charValue = Integer.parseInt(charStr, 16);
                    char[] utf8Char = Character.toChars(charValue);

                    convertedMessage += new String(utf8Char);
                    i += 4;
                }
            } else {
                convertedMessage = message.trim().replaceAll("\\s+", " ");
            }

            receivedMessage.setMessage(convertedMessage);
            convertedMessages.add(receivedMessage);
        }

        return convertedMessages;
    }
}
