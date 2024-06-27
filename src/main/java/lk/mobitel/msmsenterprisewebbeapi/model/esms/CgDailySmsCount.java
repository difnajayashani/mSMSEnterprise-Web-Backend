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
@Table(name = "cg_daily_sms_count")
public class CgDailySmsCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date")
    private Date date;

    @Column(name = "customer")
    private String customer;

    @Column(name = "m2m")
    private String m2m;

    @Column(name = "m2o")
    private String m2o;

    @Column(name = "IDD")
    private String IDD;

    @Column(name = "Total")
    private String total;
}
