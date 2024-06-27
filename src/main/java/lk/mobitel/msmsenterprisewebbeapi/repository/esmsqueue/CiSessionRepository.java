package lk.mobitel.msmsenterprisewebbeapi.repository.esmsqueue;

import jakarta.transaction.Transactional;
import lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue.CiSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Date;

@Repository
public interface CiSessionRepository extends JpaRepository<CiSession, Integer> {
    @Query("SELECT c FROM CiSession c WHERE c.sessionId = :sessionId")
    CiSession getDetail(@Param("sessionId") String sessionId);

    @Transactional
    @Modifying
    @Query("INSERT INTO CiSession(sessionId, sourceIP, userAgent, loginAttempt, verifyPWAttempt,verifyPWResetAttempt, validateLoginOTPAttempt, validatePWResetOTPAttempt, customer, user, userName) " +
            "VALUES (:sessionId, :sourceIP, :userAgent, :loginAttempt, :verifyPWAttempt,:verifyPWResetAttempt, :validateLoginOTPAttempt, :validatePWResetOTPAttempt, :customer, :user, :userName)")
    Integer insertIntoCiSession(@Param("sessionId") String sessionId,
                             @Param("sourceIP") String sourceIP,
                             @Param("userAgent") String userAgent,
                             @Param("loginAttempt") Integer loginAttempt,
                             @Param("verifyPWAttempt") Integer verifyPWAttempt,
                             @Param("verifyPWResetAttempt") Integer verifyPWResetAttempt,
                             @Param("validateLoginOTPAttempt") Integer validateLoginOTPAttempt,
                             @Param("validatePWResetOTPAttempt") Integer validatePWResetOTPAttempt,
                             @Param("customer") Integer customer,
                             @Param("user") Integer user,
                             @Param("userName") String userName);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.loginAttempt = :loginAttempt WHERE c.sessionId = :sessionId")
    void updateLoginAttempt(@Param("sessionId") String sessionId, @Param("loginAttempt") Integer loginAttempt);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.customer = :customer, c.user = :userId, c.userName = :userName WHERE c.sessionId = :sessionId")
    void updateUserDetails(@Param("sessionId") String sessionId, @Param("customer") Integer customer, @Param("userId") Integer userId, @Param("userName") String userName);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.loginOTP = :loginOTP, c.otpSentTime = :otpSentTime WHERE c.sessionId = :sessionId")
    Integer updateLoginOTPCISession(@Param("sessionId") String sessionId,@Param("loginOTP") String loginOTP,@Param("otpSentTime") LocalDateTime otpSentTime);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.pwResetOTP = :pwResetOTP, c.pwResetOTPTime = :otpSentTime WHERE c.sessionId = :sessionId")
    Integer updatePwResetOTPCISession(@Param("sessionId") String sessionId,@Param("pwResetOTP") String pwResetOTP,@Param("otpSentTime")String  otpSentTime);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.validateLoginOTPAttempt = :validateLoginOTPAttemptCISession WHERE c.sessionId = :sessionId")
    void updateValidateLoginOTPAttemptCISession(@Param("sessionId") String sessionId, @Param("validateLoginOTPAttemptCISession") Integer validateLoginOTPAttemptCISession);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.verifyPWResetAttempt = :verifyPWResetAttemptCISession WHERE c.sessionId = :sessionId")
    void updateVerifyPWResetAttemptCISession(@Param("sessionId") String sessionId, @Param("verifyPWResetAttemptCISession") Integer verifyPWResetAttemptCISession);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.validatePWResetOTPAttempt = :validatePWResetOTPAttemptCISession WHERE c.sessionId = :sessionId")
    void updateValidatePWResetOTPAttemptCISession(@Param("sessionId") String sessionId, @Param("validatePWResetOTPAttemptCISession") Integer validatePWResetOTPAttemptCISession);

    @Transactional
    @Modifying
    @Query("UPDATE CiSession c SET c.pwResetOTPSuccess = :pwResetOTPSuccess WHERE c.sessionId = :sessionId")
    void updatePwResetOTPSuccessCISession(@Param("sessionId") String sessionId, @Param("pwResetOTPSuccess") Integer pwResetOTPSuccess);
}
