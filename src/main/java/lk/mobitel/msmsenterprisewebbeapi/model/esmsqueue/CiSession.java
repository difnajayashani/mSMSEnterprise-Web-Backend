package lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ci_session")
public class CiSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "sessionId")
    private String sessionId;

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "sourceIP")
    private String sourceIP;

    @Column(name = "userAgent")
    private String userAgent;

    @Column(name = "loginAttempt")
    private Integer loginAttempt;

    @Column(name = "verifyPWResetAttempt")
    private Integer verifyPWResetAttempt;

    @Column(name = "verifyPWAttempt")
    private Integer verifyPWAttempt;

    @Column(name = "validateLoginOTPAttempt")
    private Integer validateLoginOTPAttempt;

    @Column(name = "loginOTP")
    private String loginOTP;

    @Column(name = "otpSentTime")
    private LocalDateTime otpSentTime;

    @Column(name = "validatePWResetOTPAttempt")
    private Integer validatePWResetOTPAttempt;

    @Column(name = "pwResetOTP")
    private String pwResetOTP;

    @Column(name = "pwResetOTPTime")
    private String pwResetOTPTime;

    @Column(name = "pwResetOTPSuccess")
    private Integer pwResetOTPSuccess;

    @Column(name = "customer")
    private Integer customer;

    @Column(name = "user")
    private Integer user;

    @Column(name = "username")
    private String userName;
}
