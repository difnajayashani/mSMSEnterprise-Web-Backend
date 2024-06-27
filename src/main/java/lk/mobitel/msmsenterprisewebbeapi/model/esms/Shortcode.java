package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shortcode")
public class Shortcode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "shortcode")
    private String shortcode;

    @Column(name = "customer")
    private int customer;

    @Column(name = "userid")
    private int userId;

    @Column(name = "date_created")
    private Timestamp dateCreated;
}
