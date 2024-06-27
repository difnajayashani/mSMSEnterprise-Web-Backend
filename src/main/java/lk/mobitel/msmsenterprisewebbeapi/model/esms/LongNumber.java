package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "long_number")
public class LongNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "long_number")
    private String longNumber;

    @Column(name = "customer")
    private Integer customer;

    @Column(name = "userid")
    private Integer userId;

    @Column(name = "date_created")
    private Date dateCreated;
}
