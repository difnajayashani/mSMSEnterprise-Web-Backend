package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cg_monthly_sms_count")
public class CgMonthlySmsCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "customer")
    private String customer;

    @Column(name = "m2m")
    private String m2m;

    @Column(name = "m2o")
    private String m2o;

    @Column(name = "IDD")
    private String IDD;
}
