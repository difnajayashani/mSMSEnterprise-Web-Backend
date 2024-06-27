package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_profile")
public class CustomerProfile {
    @Id
    @Column(name = "accountNo")
    private Integer accountNo;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "account_mgr_email")
    private String accountMgrEmail;

    @Column(name = "account_mgr_contact")
    private String accountMgrContact;

    @Column(name = "date_created")
    private Date dateCreated;

    @Column(name = "enable_m2o")
    private Integer enableM2o;

    @Column(name = "enable_m2all")
    private Integer enableM2all;

    @Column(name = "enable_idd")
    private Integer enableIdd;

    @Column(name = "m2m_allowance")
    private Integer m2mAllowance;

    @Column(name = "m2o_allowance")
    private Integer m2oAllowance;

    @Column(name = "m2all_allowance")
    private Integer m2allAllowance;

    @Column(name = "enable_AdStop")
    private Integer enableAdStop;

    @Column(name = "type")
    private Integer type;

    @Column(name = "status")
    private Integer status;

    @Column(name = "enable_charging")
    private Integer enableCharging;

    @Column(name = "isInternalAccount")
    private Integer isInternalAccount;

    @Column(name = "halt_services")
    private Integer haltServices;

    @Column(name = "date_halted")
    private Date dateHalted;

    @Column(name = "date_deactivated")
    private Date dateDeactivated;

    @Column(name = "date_reactivated")
    private Date dateReactivated;
}
