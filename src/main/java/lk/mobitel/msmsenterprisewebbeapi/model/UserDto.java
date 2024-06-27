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
public class UserDto {
    private Integer userId;
    private String password;
    private String userName;
    private String firstName;
    private String email;
    private String contactPhone;
    private String roleName;
    private Integer roleId;
    private Integer m2mBalance;
    private Integer m2oBalance;
    private Integer iddBalance;
    private Date start;
    private Date end;
    private Integer m2m;
    private Integer m2o;
    private Integer idd;
}
