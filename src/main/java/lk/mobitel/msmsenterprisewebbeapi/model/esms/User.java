package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "password")
    private String password;

    @Column(name = "webpassword")
    private String webPassword;

    @Column(name = "username")
    private String userName;

    @Column(name = "customer")
    private Integer customer;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "type")
    private Integer type;

    @Column(name = "enableAPI")
    private Integer enableAPI;

    @Column(name = "enableWebUI")
    private Integer enableWebUI;

    @Column(name = "enableLoginOTP")
    private Integer enableLoginOTP;

    @Column(name = "enableLock")
    private Integer enableLock;

    @Column(name = "status")
    private Integer status;

    @Column(name = "date_login_lock")
    private LocalDateTime dateLoginLock;

    @Column(name = "date_created")
    private Date dateCreated;

    @Column(name = "date_deactivated")
    private Date dateDeactivated;

    @Column(name = "date_pw_reset_web")
    private Date datePwResetWeb;

    @Column(name = "date_pw_reset_api")
    private Date datePwResetApi;

    @Column(name = "forcePWReset")
    private Integer forcePWReset;
}
