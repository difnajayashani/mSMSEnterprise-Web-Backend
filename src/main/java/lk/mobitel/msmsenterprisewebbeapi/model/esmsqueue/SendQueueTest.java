package lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "send_queue_test")
public class SendQueueTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "messageId")
    private Long messageId;

    @Column(name = "type")
    private Type type;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "status")
    private Integer status;

    @Column(name = "time_sent")
    private LocalDateTime timeSent;

    @Column(name = "time_submitted")
    private LocalDateTime timeSubmitted;

    @Column(name = "alias")
    private String alias;

    @Column(name = "customer")
    private Long customer;

    @Column(name = "user")
    private Long user;

    @Column(name = "esmClass")
    private Integer esmClass;

    @Column(name = "campaignId")
    private Integer campaignId;

    @Column(name = "campaign_status")
    private Integer campaignStatus;

    @Column(name = "messageType")
    private Integer messageType;

    @Column(name = "applicationType")
    private String applicationType;
}

