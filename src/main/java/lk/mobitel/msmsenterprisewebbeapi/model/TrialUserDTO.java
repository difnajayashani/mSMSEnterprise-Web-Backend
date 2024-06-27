package lk.mobitel.msmsenterprisewebbeapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrialUserDTO {
    private Integer id;
    private Integer customer;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String email;
    private String contactPhone;
    private Integer type;
    private Date end;
    private Integer m2mAllowance;
    private Integer m2oAllowance;
    private Integer iddAllowance;
}
