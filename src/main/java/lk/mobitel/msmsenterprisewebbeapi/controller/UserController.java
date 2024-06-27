package lk.mobitel.msmsenterprisewebbeapi.controller;

import jakarta.servlet.http.HttpSession;
import lk.mobitel.msmsenterprisewebbeapi.model.SessionInfoDTO;
import lk.mobitel.msmsenterprisewebbeapi.model.UserRequestDTO;
import lk.mobitel.msmsenterprisewebbeapi.model.esms.User;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.CiSession;
import lk.mobitel.msmsenterprisewebbeapi.service.LoggingService;
import lk.mobitel.msmsenterprisewebbeapi.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONException;

import java.security.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    LoggingService loggingService;

    @PostMapping("/addUser")
    public ResponseEntity<User> addUser(@RequestBody UserRequestDTO userRequest){
        String user1 = userService.addUser(userRequest);

        if (user1 != null){
            System.out.println("user added");
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            System.out.println("failed");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUserById(id);
        return "SUCCESS";
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String,Object>> users = userService.getAllUsers();

        if (users != null) {
            return new ResponseEntity<>(users, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sessionCreation")
    public ResponseEntity<Object>sessionCreation(@RequestBody LoginRequest loginRequest, HttpSession session) throws JSONException{
        String userName = loginRequest.getUserName();
        Object userExist = userService.validateUserExist(userName);
        if(userExist != "Check the input credentials"){
            String sessionId = "ESMSWeb" + UUID.randomUUID().toString().replaceAll("-", "") + System.currentTimeMillis();
            User userExist1 = (User) userExist;

            session.setAttribute("webUI", 1);
            session.setAttribute("sessionId", sessionId);
            session.setAttribute("userName", userName); // Set the userName attribute

            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setSessionId(sessionId);
            sessionInfo.setWebUI(1);
            sessionInfo.setUserId(userExist1.getId());
            sessionInfo.setCustomer(userExist1.getCustomer());

            String userAgent = "";
            Integer loginAttempt = 0;
            Integer verifyPWAttempt = 0;
            Integer verifyPWResetAttempt = 0;
            Integer validateLoginOTPAttempt = 0;
            Integer validatePWResetOTPAttempt = 0;
            Integer customer = 0;
            Integer user = 0;
            String sourceIP = "";
            String username = "";

            Object sessionCreation = userService.sessionCreation(sessionId, sourceIP, userAgent, loginAttempt, verifyPWAttempt, verifyPWResetAttempt, validateLoginOTPAttempt, validatePWResetOTPAttempt, customer, user, username);
            loggingService.logLoginActivity(sourceIP,sessionId,customer,userName,"Index",sessionCreation);
            if(sessionCreation == "Session has been created"){
                return ResponseEntity.ok(sessionInfo);
            }else {
                return ResponseEntity.status(300).body(sessionCreation) ;
            }
        }else {
            return ResponseEntity.status(300).body("Check the input credentials");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpSession session) {
        if (session != null) {
            // Invalidate the session
            session.invalidate();

            return ResponseEntity.ok("Logged out successfully");
        } else {
            return ResponseEntity.status(300).body("No active session found");
        }
    }

    @PostMapping("/login")
    public  ResponseEntity<Object> userLogin(@RequestBody LoginRequest loginRequest, HttpSession session) throws JSONException {
            // Obtain the current session or create a new one if it doesn't exist
            Map<String, Object> response = new HashMap<>();
            String userName = loginRequest.getUserName();
            Integer webUI = loginRequest.getWebUI();
            String sessionId = loginRequest.getSessionId();
            String sourceIP = "";
            String log;

            if (webUI != 1) {
                response.put("status", "ACCESS DENIED1");
                return ResponseEntity.ok(response.get("status"));
            } else {
                String password = loginRequest.getPassword();

                if (!Objects.equals(userName, "") && userName != null && !Objects.equals(password, "") && password != null) {
                    Object sessionDetailsObj = userService.getCISession(sessionId);

                    if (Objects.equals(sessionDetailsObj, "INVALID SESSION")) {
                        log = "Invalid Session";
                        loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                        response.put("status", "ACCESS DENIED2");
                    } else {
                        CiSession sessionDetails = (CiSession) sessionDetailsObj;
                        Integer loginAttemptCISession = sessionDetails.getLoginAttempt();
                        log = "Login Attempt - " + loginAttemptCISession;
                        loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);

                        Map<String, Object> verifyUsername = userService.verifyUserName(userName);
                        if (loginAttemptCISession > 2) {
                            if (!verifyUsername.isEmpty()) {
                                String lockOut = userService.logOut(userName);
                                if (Objects.equals(lockOut, "SUCCESS")) {
                                    LocalDateTime now = LocalDateTime.now();

                                    // Format the date and time as "yyyy-MM-dd HH:mm:ss"
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                    String myDate = now.format(formatter);

                                    log = "Account locked";
                                    loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);

                                    // Convert the formatted date string to a LocalDateTime object
                                    LocalDateTime lockDate = LocalDateTime.parse(myDate, formatter);
                                    response.put("status", "ACCOUNT LOCKED");
                                    response.put("lockDate", lockDate);
                                }
                            } else {
                                LocalDateTime now = LocalDateTime.now();

                                // Format the LocalDateTime object as a string with the desired format
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String formattedDate = now.format(formatter);
                                log = "Account locked";
                                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                response.put("status", "ACCOUNT LOCKED");
                                response.put("lockDate", formattedDate);
                            }
                        } else {
                            loginAttemptCISession = loginAttemptCISession + 1;
                            userService.updateLoginAttemptCISession(sessionId, loginAttemptCISession);

                            if (verifyUsername.isEmpty()) {
                                log = "Invalid Username";
                                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                response.put("status", "INVALID CREDENTIALS");
                            } else {
                                Integer userId = (Integer) verifyUsername.get("id");
                                Integer userType = (Integer) verifyUsername.get("type");
                                Integer userActiveStatus = (Integer) verifyUsername.get("active_status");
                                Integer subUserActiveStatus = (Integer) verifyUsername.get("user_active_status");
                                Integer haltServices = (Integer) verifyUsername.get("halt_status");
                                Integer customer1 = (Integer) verifyUsername.get("customer");
                                Integer enableLoginOTP = (Integer) verifyUsername.get("enableLoginOTP");
                                Integer enableWebUI = (Integer) verifyUsername.get("enableWebUI");
                                Integer enableLock = (Integer) verifyUsername.get("enableLock");
                                LocalDateTime dateLoginLock = (LocalDateTime) verifyUsername.get("lockTime");

                                session.setAttribute("customer", customer1);

                                userService.updateUserDetailsCISession(sessionId, customer1, userId, userName);

                                if (enableWebUI == 1) {
                                    if (userType == 0 || userType == 101 || userType == 104) {
                                        if (subUserActiveStatus == 1) {
                                            if (userActiveStatus == 1) {
                                                if (haltServices == 0) {
                                                    int getLatestPasswordReset = 0;
                                                    if (enableLock == 1) {
                                                        LocalDateTime todayDate = LocalDateTime.now();
                                                        // Calculate the difference between two dates
                                                        Duration duration = Duration.between(todayDate, dateLoginLock);
                                                        long diffMinutes = Math.abs(duration.toMinutes());

                                                        if (diffMinutes < 1200) {
                                                            log = "Account is locked";
                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                            response.put("status", "ACCOUNT LOCKED");
                                                            response.put("lockDate", dateLoginLock);
                                                        } else {//login is locked for more than 20 minutes => then unlock the login page
                                                            log = "Account unlocked after 20 minutes";
                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                            enableLock = 0;
                                                            userService.unlockLogin(userName);
                                                            getLatestPasswordReset = 1;
                                                        }
//                                                    response.put("status", diffMinutes);

                                                    } else {//if login is not locked
                                                        getLatestPasswordReset = 1;
                                                    }
                                                    if (getLatestPasswordReset == 1) {
                                                        String forcePWReset = userService.forcePWReset(userName);
                                                        if (Objects.equals(forcePWReset, "FORCE PW RESET")) {
                                                            log = "Password Reset is forced";
                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                            response.put("status", "FORCE PW RESET");
                                                        } else if (Objects.equals(forcePWReset, "INVALID USERNAME")) {
                                                            log = "Invalid Username";
                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                            response.put("status", "INVALID CREDENTIALS");
                                                        } else {
                                                            Map<String, Object> verifiedUser = userService.verifyUser(userName, password);
                                                            if (verifiedUser.get("response") == "INVALID") {
                                                                log = "Invalid Username";
                                                                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                                response.put("status", "INVALID CREDENTIALSsss");
                                                            } else if (verifiedUser.get("response") == "TEST USER EXPIRED") {
                                                                log = "Test user id has been expired";
                                                                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                                response.put("status", "TEST USER EXPIRED");
                                                            } else {
                                                                String statusUser = (String) verifiedUser.get("response");
                                                                Map<String, Object> userData = (Map<String, Object>) verifiedUser.get("userData");
                                                                response.put("status", statusUser);

                                                                if (Objects.equals(statusUser, "VALID USER")) {
                                                                    if (enableLoginOTP == 1) {
                                                                        Map<String, Object> sendLoginOTP = userService.sendOTP(customer1, userId, userName, sessionId, "OTP_LOGIN");
                                                                        if (sendLoginOTP.get("status") == "INVALID") {
                                                                            log = "Test user id has been expired";
                                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                                            response.put("status", "INVALID CREDENTIALS");
                                                                        } else if (sendLoginOTP.get("status") == "NO CONTACT NUMBER") {
                                                                            log = "Customer contact number is missing";
                                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                                            response.put("status", "NO CONTACT NUMBER");
                                                                        } else if (sendLoginOTP.get("status") == "INVALID CONTACT") {
                                                                            log = "Registered customer contact is invalid";
                                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                                            response.put("status", "INVALID CONTACT");
                                                                        } else if(sendLoginOTP.get("status") == "LOGIN OTP SENT"){
                                                                            log = "User validated and Login OTP has been sent";
                                                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                                            response.put("status", "LOGIN OTP SENT");
                                                                            response.put("userName",userName);
                                                                            response.put("customer",customer1);
                                                                            response.put("sessionId",sessionId);
                                                                            response.put("userId", userId);
                                                                        }
                                                                    } else {// Go ahead without login OTP
                                                                        Map<String, Object> result = (Map<String, Object>) userData.get("response");
                                                                        SessionInfoDTO sessionInfoDTO = new SessionInfoDTO();
                                                                        sessionInfoDTO.setUserId(userId);
                                                                        sessionInfoDTO.setCustomer(customer1);
                                                                        sessionInfoDTO.setUserName(userName);
                                                                        sessionInfoDTO.setEnableAdStop((Integer) result.get("EnableAdStop"));
                                                                        sessionInfoDTO.setUserType((Integer) result.get("userType"));
                                                                        sessionInfoDTO.setEnableIdd((Integer) result.get("EnableIDD"));
                                                                        sessionInfoDTO.setEnableM2all((Integer) result.get("EnableM2ALL"));
                                                                        sessionInfoDTO.setEnableM2o((Integer) result.get("EnableM2O"));
                                                                        sessionInfoDTO.setSessionId(sessionId);
                                                                        sessionInfoDTO.setFirstName((String) result.get("firstName"));

                                                                        log = "Login OTP disabled. User validated and logging in";
                                                                        loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);

                                                                        response.put("sessionInfoDTO", sessionInfoDTO);
                                                                        response.put("status", "GO AHEAD");
                                                                    }
                                                                }
                                                            }
//                                                        response.put("status","INVALID");
                                                        }
                                                    }
                                                } else {
                                                    log = "Account Halted";
                                                    loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                    response.put("status", "ACCOUNT HALTED");
                                                }
                                            } else {
                                                log = "User Inactive";
                                                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                                response.put("status", "INACTIVE");
                                            }
                                        } else {
                                            log = "SubUser Inactive";
                                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                            response.put("status", "SUBUSER INACTIVE");
                                        }
                                    } else {
                                        log = "Invalid User type - " + userType;
                                        loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                        response.put("status", "INVALID CREDENTIALS");
                                    }
                                } else {
                                    log = "Access denied for webUI";
                                    loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                                    response.put("status", "WEBUI ACCESS DENIED");
                                }
                            }
                        }
                    }
                } else {
                    log = "Invalid Access";
                    loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"verifyLogin",log);
                    response.put("status", "ACCESS DENIED");
                }
                return ResponseEntity.ok(response);
            }
    }

    @PostMapping("/validateLoginOTP")
    public ResponseEntity<?> validateLoginOTP(@RequestBody LoginRequest loginRequest){
        String userName = loginRequest.getUserName();
        Integer webUI = loginRequest.getWebUI();
        String sessionId = loginRequest.getSessionId();
        Integer customer = loginRequest.getCustomer();
        Integer otp = loginRequest.getOtp();
        Map<String, Object> response = new HashMap<>();
        String sourceIP = "";
        String log;

        if(webUI != 1){
            response.put("status","ACCESS DENIED");
        }else {
            Object sessionDetailsObj = userService.getCISession(sessionId);
            if(sessionDetailsObj == null){
                log = "Invalid Session";
                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);
                response.put("status","ACCESS DENIED");
            }else {
                CiSession sessionDetails = (CiSession) sessionDetailsObj;
                Integer loginAttemptCISession = sessionDetails.getLoginAttempt();
                Integer validateLoginOTPAttemptCISession = sessionDetails.getValidateLoginOTPAttempt();
                String loginOTP = sessionDetails.getLoginOTP();
                LocalDateTime otpSentTime = sessionDetails.getOtpSentTime();

                log = "LoginAttempt - " + loginAttemptCISession + " and validateLoginOTPAttempt - " + validateLoginOTPAttemptCISession;
                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);

                log = "LoginOTP - " + loginOTP + " otpSentTime - " + otpSentTime + " enteredOTP - " + otp;
                loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);

                if(loginAttemptCISession > 2 || validateLoginOTPAttemptCISession > 2){
                    String lockOut = userService.logOut(userName);
                    if (Objects.equals(lockOut, "SUCCESS")) {
                        log = "Account Locked";
                        loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);
                        response.put("status", "ACCOUNT LOCKED");
                    }
                }else {
                    validateLoginOTPAttemptCISession = validateLoginOTPAttemptCISession + 1;
                    userService.updateValidateLoginOTPAttemptCISession(sessionId,validateLoginOTPAttemptCISession);
                    LocalDateTime now = LocalDateTime.now();
                    Duration duration = Duration.between(otpSentTime, now);
                    long diffMinutes = duration.toMinutes();
//                    System.out.println("otpSentTime" + otpSentTime);
//                    System.out.println("now" + now);
//                    System.out.println("diffMinutes" + diffMinutes);
                    if(diffMinutes > 2){
                        log = "OTP session expired";
                        loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);
                        response.put("status", "SESSION_EXPIRED");
                    }else {
                        if(!Objects.equals(loginOTP, otp.toString())){
                            log = "Invalid OTP";
                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);
                            response.put("status", "INVALID OTP");
                        }else {
                            log = "Login OTP Validated and user logging in";
                            loggingService.logLoginActivity(sourceIP,sessionId, loginRequest.getCustomer(), userName,"validateLoginOTP",log);

                            Map<String, Object> verifyUsername = userService.verified_user_data(userName,customer);
                            Integer userId = (Integer) verifyUsername.get("userId");
                            String firstName = (String) verifyUsername.get("firstName");
                            String lastName = (String) verifyUsername.get("lastName");
                            Integer userType = (Integer) verifyUsername.get("type");
                            Integer customer1 = (Integer) verifyUsername.get("customer");
                            Integer EnableIDD = (Integer) verifyUsername.get("EnableIDD");
                            Integer EnableM2O = (Integer) verifyUsername.get("EnableM2O");
                            Integer EnableM2ALL = (Integer) verifyUsername.get("EnableM2ALL");
                            Integer EnableAdStop = (Integer) verifyUsername.get("EnableAdStop");
//
                            Map<String, Object> result = new HashMap<>();
                            SessionInfoDTO sessionInfoDTO = new SessionInfoDTO();
                            sessionInfoDTO.setUserId(userId);
                            sessionInfoDTO.setCustomer(customer1);
                            sessionInfoDTO.setUserName(userName);
                            sessionInfoDTO.setUserType(userType);
                            sessionInfoDTO.setSessionId(sessionId);
                            sessionInfoDTO.setFirstName(firstName);
                            sessionInfoDTO.setEnableIdd(EnableIDD);
                            sessionInfoDTO.setEnableM2o(EnableM2O);
                            sessionInfoDTO.setEnableM2all(EnableM2ALL);
                            sessionInfoDTO.setEnableAdStop(EnableAdStop);
                            response.put("status", "OTP VALIDATED");
                            response.put("sessionInfoDTO", sessionInfoDTO);
                        }
                    }
                }
            }
        }
        System.out.println(response.get("status"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verifyPWReset")
    public ResponseEntity<?> verifyPWReset(@RequestBody PasswordResetRequest passwordResetRequest){
        Integer webUI = passwordResetRequest.getWebUI();
        String status = null;
        String log;
        String sourceIP = "";
        if(webUI != 1){
            status = "ACCESS DENIED";
        }else {
            String userName = passwordResetRequest.getUserName();
            String sessionId = passwordResetRequest.getSessionId();
            Integer customer = passwordResetRequest.getCustomer();
            Object sessionDetailsObj = userService.getCISession(sessionId);

            if(!Objects.equals(userName, "")){
                if (Objects.equals(sessionDetailsObj, "INVALID SESSION")) {
                    log = "Invalid Session";
                    loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);
                    status = "ACCESS DENIED2";
                }else {
                    CiSession ciSession = (CiSession) sessionDetailsObj;
                    Integer verifyPWResetAttemptCISession = ciSession.getVerifyPWResetAttempt();

                    log = "verifyPWResetAttempt - " + verifyPWResetAttemptCISession;
                    loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyPWReset",log);
                    Map<String, Object> verifyUsername = userService.verifyUserName(userName);
                    if(verifyPWResetAttemptCISession > 2){
                        if (!verifyUsername.isEmpty()) {
                            String lockOut = userService.logOut(userName);
                            if (Objects.equals(lockOut, "SUCCESS")) {
                                LocalDateTime now = LocalDateTime.now();

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String myDate = now.format(formatter);

                                log = "Account locked as attempts exceeded";
                                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyPWReset",log);

                                LocalDateTime lockDate = LocalDateTime.parse(myDate, formatter);
                                status = "ACCOUNT LOCKED" + lockDate;
                            }
                        } else {
                            LocalDateTime now = LocalDateTime.now();

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            String formattedDate = now.format(formatter);
                            log = "Username absent";
                            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyPWReset",log);
                            status = "ACCOUNT LOCKED" + formattedDate;
                        }
                    }else {
                        verifyPWResetAttemptCISession = verifyPWResetAttemptCISession + 1;
                        userService.updateVerifyPWResetAttemptCISession(sessionId,verifyPWResetAttemptCISession);
                        if (verifyUsername.isEmpty()) {
                            log = "Invalid Username";
                            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyPWReset",log);
                            status = "INVALID CREDENTIALS";
                        } else {
                            Integer userId = (Integer) verifyUsername.get("id");
                            Integer userType = (Integer) verifyUsername.get("type");
                            Integer userActiveStatus = (Integer) verifyUsername.get("active_status");
                            Integer subUserActiveStatus = (Integer) verifyUsername.get("user_active_status");
                            Integer haltServices = (Integer) verifyUsername.get("halt_status");
                            Integer customer1 = (Integer) verifyUsername.get("customer");
                            Integer enableLoginOTP = (Integer) verifyUsername.get("enableLoginOTP");
                            Integer enableWebUI = (Integer) verifyUsername.get("enableWebUI");
                            Integer enableLock = (Integer) verifyUsername.get("enableLock");
                            LocalDateTime dateLoginLock = (LocalDateTime) verifyUsername.get("lockTime");

                            userService.updateUserDetailsCISession(sessionId, customer1, userId, userName);

                            if (enableWebUI == 1) {
                                if (userType == 0 || userType == 101 || userType == 104) {
                                    if (subUserActiveStatus == 1) {
                                        if (userActiveStatus == 1) {
                                            if (haltServices == 0) {
                                                int sendPasswordResetOTP = 0;
                                                if (enableLock == 1) {
                                                    LocalDateTime todayDate = LocalDateTime.now();
                                                    // Calculate the difference between two dates
                                                    Duration duration = Duration.between(todayDate, dateLoginLock);
                                                    long diffMinutes = Math.abs(duration.toMinutes());

                                                    if (diffMinutes < 1200) {
                                                        log = "Account is locked";
                                                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyPWReset",log);
                                                        status = "ACCOUNT LOCKED" + dateLoginLock;
                                                    } else {//login is locked for more than 20 minutes => then unlock the login page
                                                        log = "Account unlocked after 20 minutes";
                                                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyPWReset",log);
                                                        enableLock = 0;
                                                        userService.unlockLogin(userName);
                                                        sendPasswordResetOTP = 1;
                                                    }
//
                                                } else {//if login is not locked
                                                    sendPasswordResetOTP = 1;
                                                }

                                                if (sendPasswordResetOTP == 1) {
                                                    Map<String, Object> sendOTP = userService.sendOTP(customer1, userId, userName, sessionId, "OTP_PWRESET");
                                                    if (sendOTP.get("status") == "INVALID") {
                                                        log = "Invalid User";
                                                        loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "verifyPWReset", log);
                                                        status = "INVALID CREDENTIALS";
                                                    } else if (sendOTP.get("status") == "NO CONTACT NUMBER") {
                                                        log = "Customer contact number is missing";
                                                        loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "verifyPWReset", log);
                                                        status = "NO CONTACT NUMBER";
                                                    } else if (sendOTP.get("status") == "INVALID CONTACT") {
                                                        log = "Registered customer contact is invalid";
                                                        loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "verifyPWReset", log);
                                                        status = "INVALID CONTACT";
                                                    } else if (sendOTP.get("status") == "LOGIN OTP SENT") {
                                                        log = "User validated and Login OTP has been sent";
                                                        loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "verifyPWReset", log);
                                                        status = "OTP SENT";
                                                    }
                                                }
                                            }else {
                                                log = "Account Halted";
                                                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyLogin",log);
                                                status = "ACCOUNT HALTED";
                                            }
                                        }else {
                                            log = "User Inactive";
                                            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyLogin",log);
                                            status = "INACTIVE";
                                        }
                                    }else {
                                        log = "SubUser Inactive";
                                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyLogin",log);
                                        status = "SUBUSER INACTIVE";
                                    }
                                }else {
                                    log = "Invalid User type - " + userType;
                                    loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyLogin",log);
                                    status = "INVALID CREDENTIALS";
                                }
                            }else {
                                log = "Access denied for webUI";
                                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyLogin",log);
                                status = "WEBUI ACCESS DENIED";
                            }
                        }

                    }
                }
            }else {
                log = "Invalid Access";
                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"verifyLogin",log);
                status = "ACCESS DENIED";
            }
        }
        System.out.println(status);
        if(Objects.equals(status, "OTP SENT")){
            return ResponseEntity.ok(status);
        }else {
            return ResponseEntity.status(300).body(status);
        }
    }

    @PostMapping("/validatePwResetOtp")
    public ResponseEntity<?> validatePwResetOtp(@RequestBody PasswordResetRequest passwordResetRequest) {
        Integer webUI = passwordResetRequest.getWebUI();
        String status = null;
        String log;
        String sourceIP = null;
        if (webUI != 1) {
            status = "ACCESS DENIED";
        } else{
            String sessionId = passwordResetRequest.getSessionId();
            Integer customer = passwordResetRequest.getCustomer();
            String userName = passwordResetRequest.getUserName();
            String otp = passwordResetRequest.getOtp();

            Object sessionDetailsObj = userService.getCISession(sessionId);
            if(sessionDetailsObj == null){
                log = "Invalid Session";
                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"validatePwResetOtp",log);
                status = "ACCESS DENIED";
            }else {
                CiSession sessionDetails = (CiSession) sessionDetailsObj;
                Integer loginAttemptCISession = sessionDetails.getLoginAttempt();
                Integer verifyPWResetAttemptCISession = sessionDetails.getVerifyPWResetAttempt();
                Integer validateLoginOTPAttemptCISession = sessionDetails.getValidateLoginOTPAttempt();
                Integer validatePWResetOTPAttemptCISession = sessionDetails.getValidatePWResetOTPAttempt();
                String pwResetOTP = sessionDetails.getPwResetOTP();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime otpSentTime = LocalDateTime.parse(sessionDetails.getPwResetOTPTime(), formatter);

                log = "verifyPWResetAttempt - " + verifyPWResetAttemptCISession + " validatePWResetOTPAttempt - " + validatePWResetOTPAttemptCISession;
                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"validatePwResetOtp",log);

                log = "PwResetOTP - " + pwResetOTP + " otpSentTime - " + otpSentTime + " enteredOTP - " + otp;
                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"validatePwResetOtp",log);

                if(verifyPWResetAttemptCISession > 2 || validatePWResetOTPAttemptCISession > 2){
                    String lockOut = userService.logOut(userName);
                    if (Objects.equals(lockOut, "SUCCESS")) {
                        log = "Account Locked";
                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"validatePwResetOtp",log);
                        status = "ACCOUNT LOCKED";
                    }
                }else {
                    validatePWResetOTPAttemptCISession = validatePWResetOTPAttemptCISession + 1;
                    userService.updateValidatePWResetOTPAttemptCISession(sessionId,validatePWResetOTPAttemptCISession);
                    LocalDateTime now = LocalDateTime.now();
                    Duration duration = Duration.between(otpSentTime, now);
                    long diffMinutes = duration.toMinutes();
                    if(diffMinutes > 2){
                        log = "PWReset OTP session expired";
                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"validatePwResetOtp",log);
                        status = "SESSION_EXPIRED";
                    }else {
                        if(!Objects.equals(pwResetOTP, otp)){
                            if(validatePWResetOTPAttemptCISession < 3){
                                log = "Invalid PWReset OTP";
                                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"validatePwResetOtp",log);
                                status = "INVALID OTP";
                            }else {
                                log = "Account locked as PWReset OTP attempts exceeded";
                                loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "validatePwResetOtp", log);
                                status = "ACCOUNT LOCKED";
                            }
                        } else if (Objects.equals(pwResetOTP, otp)) {
                            Integer pwResetOTPSuccess = 1;
                            userService.updatePwResetOTPSuccessCISession(sessionId,pwResetOTPSuccess);
                            log = "PWReset OTP validated";
                            loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "validatePwResetOtp", log);
                            status = "OTP VALIDATED";
                        }else {
                            log = "Invalid Access";
                            loggingService.logLoginActivity(sourceIP, sessionId, customer, userName, "validatePwResetOtp", log);
                            status = "ACCESS DENIED";
                        }
                    }
                }
            }
        }
        System.out.println("status" + status);
        if(Objects.equals(status, "OTP VALIDATED")){
            return ResponseEntity.ok(status);
        }else {
            return ResponseEntity.status(300).body(status);
        }
    }

    @PostMapping("/passwordReset")
    public ResponseEntity<?> passwordReset(@RequestBody PasswordResetRequest passwordResetRequest){
        Integer webUI = passwordResetRequest.getWebUI();
        String status;
        String log;
        String sourceIP = "";
        String apiSelect = "true";
        String webSelect = "true";
        if(webUI != 1){
            status = "ACCESS DENIED";
        }else {
            Integer userId = passwordResetRequest.getUserId();
            String userName = passwordResetRequest.getUserName();
            Integer customer = passwordResetRequest.getCustomer();
            String sessionId = passwordResetRequest.getSessionId();
            String currentPassword = passwordResetRequest.getCurrentPassword();
            String newPassword = passwordResetRequest.getNewPassword();

            Object sessionDetailsObj = userService.getCISession(sessionId);
            if (Objects.equals(sessionDetailsObj, "INVALID SESSION")) {
                log = "Invalid Session";
                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);
                status = "ACCESS DENIED2";
            }else {
                CiSession ciSession = (CiSession) sessionDetailsObj;
                Integer loginAttemptCISession = ciSession.getLoginAttempt();
                Integer verifyPWResetAttemptCISession = ciSession.getVerifyPWResetAttempt();
                Integer validateLoginOTPAttemptCISession = ciSession.getValidateLoginOTPAttempt();
                Integer validatePWResetOTPAttemptCISession = ciSession.getValidatePWResetOTPAttempt();
                String pwResetOTP = ciSession.getPwResetOTP();
                String otpSentTime = ciSession.getPwResetOTPTime();
                Integer pwResetOTPSuccess = ciSession.getPwResetOTPSuccess();

                log = "validatePWResetOTPAttempt - " + validatePWResetOTPAttemptCISession + " pwResetOTPSuccess - " + pwResetOTPSuccess;
                loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);

                if(pwResetOTPSuccess != 1){
                    log = "Invalid Access to the function";
                    loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);
                    status = "ACCESS DENIED";
                }else {
                    log = "PWReset requested for apiSelect - " + apiSelect + " webSelect - " + webSelect;
                    loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);

                    if(userId == null){
                        log = "Session Timeout";
                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);
                        status = "TIMEOUT";
                    }else {
                        LocalDateTime localDateTime = LocalDateTime.now();
                        String pwResetStatus = userService.password_reset(newPassword,userId,localDateTime,apiSelect,webSelect);

                        log = "PWReset Output - " + pwResetStatus;
                        loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resetPassword",log);
                        status = pwResetStatus;
                    }
                }
            }
        }
        if(Objects.equals(status, "SUCCESS")){
            return ResponseEntity.ok(status);
        }else {
            return ResponseEntity.status(300).body(status);
        }
    }

    @PostMapping("/resendCode")
    public ResponseEntity<?> resendCode(@RequestBody ResendOTPRequest resendRequest){
        String log;
        String sessionId = resendRequest.getSessionId();
        Integer customer = resendRequest.getCustomer();
        String userName = resendRequest.getUserName();
        Integer userId = resendRequest.getUserId();
        String sourceIP = "";
        String status = "";

        Map<String, Object> sendLoginOTP = userService.sendOTP(customer, userId, userName, sessionId, resendRequest.getType());

        if (sendLoginOTP.get("status") == "INVALID") {
            log = "Test user id has been expired";
            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resendCode",log);
            status = "INVALID CREDENTIALS";
        } else if (sendLoginOTP.get("status") == "NO CONTACT NUMBER") {
            log = "Customer contact number is missing";
            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resendCode",log);
            status = "NO CONTACT NUMBER";
        } else if (sendLoginOTP.get("status") == "INVALID CONTACT") {
            log = "Registered customer contact is invalid";
            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resendCode",log);
            status = "INVALID CONTACT";
        } else if(sendLoginOTP.get("status") == "LOGIN OTP SENT"){
            log = "User validated and Login OTP has been sent";
            loggingService.logLoginActivity(sourceIP,sessionId, customer, userName,"resendCode",log);
            status = "OTP SENT";
        }

        if(status.equals("OTP SENT")){
            return ResponseEntity.ok(status);
        }else {
            return ResponseEntity.status(300).body(status);
        }
    }

    @Getter
    @Setter
    public static class LoginRequest{
        private String userName;
        private String password;
        private Integer webUI;
        private String sessionId;
        private Integer otp;
        private Integer customer;
    }

    @Getter
    @Setter
    public static class SessionInfo {
        private String sessionId;
        private Integer webUI;
        private Integer customer;
        private Integer userId;
    }

    @Getter
    @Setter
    public static class PasswordResetRequest{
        private String currentPassword;
        private String newPassword;
        private Integer webUI;
        private String sessionId;
        private Integer userId;
        private String userName;
        private Integer customer;
        private String otp;
    }

    @Getter
    @Setter
    public static class ResendOTPRequest{
        private String userName;
        private String sessionId;
        private Integer customer;
        private Integer userId;
        private String type;
    }
}
