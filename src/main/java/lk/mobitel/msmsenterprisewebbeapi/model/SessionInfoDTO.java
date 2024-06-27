package lk.mobitel.msmsenterprisewebbeapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDTO {
    private String sessionId;
    private Integer userId;
    private String userName;
    private String firstName;
    private Integer customer;
    private Integer userType;
    private Integer enableIdd;
    private Integer enableM2o;
    private Integer enableM2all;
    private Integer enableAdStop;
}
