package lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "send_queue")
public class SendQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message", length = 5000)
    private String message;

    @Column(name = "messageId")
    private Long messageId;

    @Column(name = "type")
    private Integer type;

    @Column(name = "recipient", length = 20)
    private String recipients;

    @Column(name = "status")
    private Integer status;

    @Column(name = "time_sent")
    private Date timeSent;

    @Column(name = "time_submitted")
    private Date timeSubmitted;

    @Column(name = "alias", length = 11)
    private String alias;

    @Column(name = "customer")
    private Integer customer;

    @Column(name = "user")
    private Integer user;

    @Column(name = "esmClass")
    private Integer esmClass;

    @Column(name = "campaignId")
    private Integer campaignId;

    @Column(name = "campaign_status")
    private Integer campaignStatus;

    @Column(name = "messageType")
    private Integer messageType;

    @Column(name = "applicationType", length = 45)
    private String applicationType;
}
