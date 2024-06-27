package lk.mobitel.msmsenterprisewebbeapi.service;

import lk.mobitel.msmsenterprisewebbeapi.model.UserRequestDTO;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.User;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.CiSession;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.SendQueue;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.TestUserRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esms.UserRoleRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.CiSessionRepository;
import lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue.SendQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private CiSessionRepository sessionRepository;
    @Autowired
    private TestUserRepository testUserRepository;
    @Autowired
    SendQueueRepository sendQueueRepository;

    public String addUser(UserRequestDTO userRequest) {
        String hashedPassword = hashPassword(userRequest.getPassword());
        userRepository.addToUser(userRequest.getFirstName(),userRequest.getLastName(),userRequest.getUserName(),hashedPassword,userRequest.getEmail());
        User fetchedUser = userRepository.getUserByUserName(userRequest.getUserName());
        Integer userId = fetchedUser.getId();
        userRoleRepository.addToUserRole(userRequest.getRoleId(),userId);
        if(fetchedUser != null){
            return "User Added successfully";
        }
        else {
            return null;
        }
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
        userRoleRepository.deleteById(id);
    }

    public List<Map<String,Object>> getAllUsers() {
       return userRepository.getAllUsers();
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convert byte array to a string
            String hashedPassword = Base64.getEncoder().encodeToString(hash);
            return hashedPassword;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    public User authenticateUser(String userName, String password) {
        User user = userRepository.findByUserName(userName);

        if (user != null) {
            String hashedPassword = hashPassword(password);

            if (hashedPassword != null && hashedPassword.equals(user.getPassword())) {
                return user;
            }else {
                return null;
            }
        }else {
            return null;
        }

    }

    public Map<String, Object> verifyUserName(String userName) {
        return userRepository.verifyUserName(userName);
    }

    public void unlockLogin(String userName) {
        userRepository.unlockLogin(userName);
    }

    public String forcePWReset(String userName) {
        Map<String, Object> response = userRepository.getLatestPwReset(userName);
        if (response == null) {
            return "INVALID USERNAME";
        } else if ((Integer) response.get("forcePWReset") != 0) {
            return "FORCE PW RESET";
        } else {
            return "NO FORCE PW RESET";
        }
    }

    public Map<String,Object> verifyUser(String userName, String password) {
        Map<String,Object> returnData = verify_user(userName,password);
        Map<String, Object> verifyResponse = new HashMap<>();
        if(returnData.get("response").equals(0)){
            verifyResponse.put("response","INVALID");
        } else if (returnData.get("response").equals("TEST USER EXPIRED")) {
            verifyResponse.put("response","TEST USER EXPIRED");
        }else {
            Object userData = returnData.get("response");
            if(userData != null){
                verifyResponse.put("response","VALID USER");
                verifyResponse.put("userData",returnData);
            }
        }
        return verifyResponse;
    }

    public Map<String,Object> verify_user(String userName, String password){
        String hashedPassword = hashPassword(password);
        System.out.println("hashedPassword" + hashedPassword);
        Map<String, Object> response = userRepository.verifyUser(userName, hashedPassword);
        Map<String, Object> returnData = new HashMap<>();
        if (!response.isEmpty()) {
            if((Integer) response.get("userType") == 0 || (Integer) response.get("userType") == 101 || (Integer) response.get("userType") == 104){
                returnData.put("response",response);
            } else if ((Integer) response.get("userType") == 10) {
                List<Map<String,Object>> result = testUserRepository.isTestUserExpired((Integer) response.get("userId"));
                if(result == null){
                    returnData.put("response",response);
                } else if (result.get(0).get("expired").equals(true)) {
                    returnData.put("response","TEST USER EXPIRED");
                }else {
                    returnData.put("response",response);
                }
            }
            return returnData;
        }else {
            returnData.put("response",0);
            return returnData;
        }
    }

    public Integer findUserId(String userName) {
        User user = userRepository.findByUserName(userName);
        return user.getId();
    }

    public String sessionCreation(String sessionId, String sourceIP, String userAgent, Integer loginAttempt, Integer verifyPWAttempt, Integer verifyPWResetAttempt, Integer validateLoginOTPAttempt, Integer validatePWResetOTPAttempt, Integer customer, Integer user, String userName) {
        Integer response = sessionRepository.insertIntoCiSession(sessionId, sourceIP, userAgent, loginAttempt, verifyPWAttempt,verifyPWResetAttempt, validateLoginOTPAttempt, validatePWResetOTPAttempt, customer, user, userName);
        if(response == 1){
            return "Session has been created";
        }else {
            return "Session creation failed";
        }
    }

    public Object getCISession(String sessionId) {
        CiSession sessionDetails = sessionRepository.getDetail(sessionId);
        return Objects.requireNonNullElse(sessionDetails, "INVALID SESSION");
    }

    public String logOut(String userName) {
        Integer response = userRepository.lockLogin(userName);
        if(response == 1){
            return "SUCCESS";
        }
        return "UNSUCCESSFUL";

    }

    public void updateLoginAttemptCISession(String sessionId, Integer loginAttemptCISession) {
        sessionRepository.updateLoginAttempt(sessionId,loginAttemptCISession);
    }

    public void updateUserDetailsCISession(String sessionId, Integer customer, Integer userId, String userName) {
        sessionRepository.updateUserDetails(sessionId,customer,userId,userName);
    }

    public Map<String,Object> sendOTP(Integer customer, Integer userId, String userName,String sessionId, String otpLogin) {
        Map<String,Object> customerContact = getCustomerContact(customer,userId);
        Map<String, Object> response = new HashMap<>();
        if(customerContact.isEmpty()){
            response.put("status","NO CONTACT NUMBER");
        } else if (customerContact.get("status") == "FAILED") {
            response.put("status","INVALID");
        } else {
            Map<String, Object> recipientDetails = (Map<String, Object>) customerContact.get("status");
            String recipient = (String) recipientDetails.get("recipient");
            String type = (String) recipientDetails.get("type");

            if(type.equals("0")){
                response.put("status","INVALID CONTACT");
            }else {
                String alias = "ESMS";
                Integer OTP_SMS_customer = 999999;
                Integer user = 1;

                LocalDateTime otpSentTime = LocalDateTime.now();
                String message = "";

                if(Objects.equals(otpLogin, "OTP_LOGIN")){
                    String alphabet = "0123456789";
                    int passwordLength = alphabet.length()-1;
                    Random random = new Random();
                    StringBuilder otp = new StringBuilder();

                    for (int i = 0; i < passwordLength; i++) {
                        int index = random.nextInt(alphabet.length());
                        otp.append(alphabet.charAt(index));
                    }
                    String loginOTP = otp.toString();
                    Integer updateLoginOTP = sessionRepository.updateLoginOTPCISession(sessionId,loginOTP,otpSentTime);
                    message = "Please enter the following OTP to continue in web login: " + loginOTP + "OTP is valid only for next 2 minutes.";
                    response.put("status",updateLoginOTP);
                } else if (Objects.equals(otpLogin, "OTP_PWRESET")) {
                    String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ1234567890!@#$%&*";
                    int passwordLength = alphabet.length()-1;
                    Random random = new Random();
                    StringBuilder otp = new StringBuilder();

                    for (int i = 0; i < passwordLength; i++) {
                        int index = random.nextInt(alphabet.length());
                        otp.append(alphabet.charAt(index));
                    }
                    String pwResetOTP = otp.toString();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    // Format the date and time
                    String formattedSendOTPTime = otpSentTime.format(formatter);

                    Integer updatePwResetOTP = sessionRepository.updatePwResetOTPCISession(sessionId,pwResetOTP,formattedSendOTPTime);
                    message = "Please enter the following activation code to continue in password reset service:" + pwResetOTP + "OTP is valid only for next 2 minutes.";
                    response.put("status",updatePwResetOTP);
                }
                String sendOTPOutput = sendOTPMessage(recipient,type,alias,OTP_SMS_customer,user,message);
                if(sendOTPOutput.equals("SUCCESS")){
                    response.put("status","LOGIN OTP SENT");
                }
            }
            response.put("recipient",recipient);
            response.put("type",type);
        }
        return response;
    }

    private String sendOTPMessage(String recipient, String type, String alias, Integer otpSmsCustomer, Integer user, String message) {
        String appType = "WEB";
        boolean sendSMS = true;
        int promoTax = 0;
        int esmClass = 0;
        Integer campaignId = 1;
        Integer campaignStatus = 1;

        String getQdBTable = getQdBTable(otpSmsCustomer.toString());
        String response = "";
        if(Objects.equals(getQdBTable, "SendQueue")) {
            Integer sendQueue = sendQueueRepository.insertIntoEsmTxQueue(message,recipient,alias,otpSmsCustomer,user,appType,esmClass,campaignId,campaignStatus,1, LocalDateTime.now());
            if (sendQueue == 1) {
                response = "SUCCESS";
            }else {
                response = "Send OTP Message Failed";
            }
        }
        return response;
    }

    private String getQdBTable(String otpSmsCustomer) {
        return switch (otpSmsCustomer) {
            case "42682340" -> "send_queue_ceb";
            case "24794895" -> "send_queue_ceylinco";
            case "80808080" -> "send_queue_madv";
//            case "9999991" -> "SendQueue";
            default -> "SendQueue";
        };
    }

    private Map<String, Object> getCustomerContact(Integer customer, Integer userId) {
        String phoneNumber = userRepository.getCustomerContact(customer,userId);
        Map<String, Object> response = new HashMap<>();
        if(phoneNumber == null){
            response.put("status","FAILED");
        }else {
            Map<String, Object> result = getRecipientType(phoneNumber);
            response.put("status",result);
        }
        return response;
    }

    private Map<String, Object> getRecipientType(String phoneNumber) {
        // Initialize the result map
        Map<String, Object> resultMap = new HashMap<>();

        // Remove non-numeric characters
        String recipient = phoneNumber.replaceAll("[^0-9]", "");

        // Remove leading zeros
        recipient = recipient.replaceFirst("^0+(?!$)", "");

        String type = "0"; // Default type

        // Check the length of the recipient
        if (recipient.length() < 9) {
            // Number with alphanumeric values
            type = "0";
        } else if (Pattern.matches("^(71)[0-9]{7}$", recipient)) {
            recipient = "94" + recipient;
            type = "M2M";
        } else if (Pattern.matches("^(9471)[0-9]{7}$", recipient)) {
            type = "M2M";
        } else if (Pattern.matches("^(70)[0-9]{7}$", recipient)) {
            recipient = "94" + recipient;
            type = "M2M";
        } else if (Pattern.matches("^(9470)[0-9]{7}$", recipient)) {
            type = "M2M";
        } else if (Pattern.matches("^(7)[3][0-9]{7}$", recipient) || Pattern.matches("^(7)[9][0-9]{7}$", recipient)
                || Pattern.matches("^(947)[3][0-9]{7}$", recipient) || Pattern.matches("^(947)[9][0-9]{7}$", recipient)) {
            type = "0";
        } else if (Pattern.matches("^(7)[2-9][0-9]{7}$", recipient)) {
            recipient = "94" + recipient;
            type = "M2O";
        } else if (Pattern.matches("^(947)[2-9][0-9]{7}$", recipient)) {
            type = "M2O";
        } else if (Pattern.matches("^(94)[0-9]{9}$", recipient) && recipient.charAt(2) != '7') {
            type = "M2O";
        } else if (!recipient.startsWith("94")) {
            Map<String, Integer> SL_AreaCode = new HashMap<>();
            SL_AreaCode.put("Ampara", 63);
            SL_AreaCode.put("Anuradhapura", 25);
            // Add other area codes

            if (recipient.length() == 9 && SL_AreaCode.containsKey(recipient.substring(0, 2))) {
                recipient = "94" + recipient;
                type = "M2O";
            } else {
                recipient = "00" + recipient;
                type = "IDD";
            }
        } else {
            type = "0";
        }

        // Populate the result map
        resultMap.put("recipient", recipient);
        resultMap.put("type", type);

        return resultMap;
    }

    public Object validateUserExist(String userName) {
        User user = userRepository.getUserByUserName(userName);
        if(user != null){
            return user;
        }else {
            return "Check the input credentials";
        }
    }

    public void updateValidateLoginOTPAttemptCISession(String sessionId, Integer validateLoginOTPAttemptCISession) {
        sessionRepository.updateValidateLoginOTPAttemptCISession(sessionId,validateLoginOTPAttemptCISession);
    }

    public Map<String, Object> verified_user_data(String userName, Integer customer) {
        return userRepository.verified_user_data(userName,customer);
    }

    public String password_reset(String newPassword, Integer userId, LocalDateTime localDateTime, String apiSelect, String webSelect) {
        String output = "";
        Integer result = null;

        if(Objects.equals(apiSelect, "true") && Objects.equals(webSelect, "false")){
            result = userRepository.updatePasswordResetApi(hashPassword(newPassword),localDateTime,userId);
        }else if(Objects.equals(apiSelect, "false") && Objects.equals(webSelect, "true")){
            result = userRepository.updatePasswordResetWeb(hashPassword(newPassword),localDateTime,userId);
        } else if (Objects.equals(apiSelect, "true") && Objects.equals(webSelect, "true")) {
            result = userRepository.updatePasswordResetApiAndWeb(hashPassword(newPassword),localDateTime,userId);
        }

        if (result > 0){
            output= "SUCCESS";
        }else {
            output= "FAILED";
        }

        return output;
    }

    public void updateVerifyPWResetAttemptCISession(String sessionId, Integer verifyPWResetAttemptCISession) {
        sessionRepository.updateVerifyPWResetAttemptCISession(sessionId,verifyPWResetAttemptCISession);
    }

    public void updateValidatePWResetOTPAttemptCISession(String sessionId, Integer validatePWResetOTPAttemptCISession) {
        sessionRepository.updateValidatePWResetOTPAttemptCISession(sessionId,validatePWResetOTPAttemptCISession);
    }

    public void updatePwResetOTPSuccessCISession(String sessionId, Integer pwResetOTPSuccess) {
        sessionRepository.updatePwResetOTPSuccessCISession(sessionId,pwResetOTPSuccess);
    }
}