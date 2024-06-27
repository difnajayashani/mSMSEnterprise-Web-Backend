package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.*;
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
@Table(name = "test_user")
public class TestUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user")
    private Integer user;

    @Column(name = "start")
    private Date start;

    @Column(name = "end")
    private Date end;

    @Column(name = "m2m")
    private Integer m2m;

    @Column(name = "m2o")
    private Integer m2o;

    @Column(name = "idd")
    private Integer idd;

    @Column(name = "m2m_balance")
    private Integer m2mBalance;

    @Column(name = "m2o_balance")
    private Integer m2oBalance;

    @Column(name = "idd_balance")
    private Integer iddBalance;
}
