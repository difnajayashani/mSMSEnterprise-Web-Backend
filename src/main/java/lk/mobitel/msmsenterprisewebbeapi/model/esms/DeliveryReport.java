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
@Table(name = "delivery_report")
public class DeliveryReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "message")
    private String message;

    @Column(name = "sender")
    private String sender;

    @Column(name = "time")
    private Date time;

    @Column(name = "status")
    private String status;

    @Column(name = "messageId")
    private String messageId;

    @Column(name = "submitDate")
    private Date submitDate;

    @Column(name = "deliveryDate")
    private Date deliveryDate;

    @Column(name = "msgsegments")
    private Integer msgsegments;
}
