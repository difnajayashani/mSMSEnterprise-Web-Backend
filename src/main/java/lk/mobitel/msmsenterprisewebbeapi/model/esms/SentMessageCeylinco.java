package lk.mobitel.msmsenterprisewebbeapi.model.esms;

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
@Table(name = "sent_messages_ceylinco")
public class SentMessageCeylinco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "alias")
    private String alias;

    @Column(name = "customer")
    private int customer;

    @Column(name = "user")
    private int user;

    @Column(name = "message")
    private String message;

    @Column(name = "msgsegments")
    private int msgSegments;

    @Column(name = "sendQId")
    private long sendQId;

    @Column(name = "messageId")
    private long messageId;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    private int status;

    @Column(name = "time_sent")
    private LocalDateTime timeSent;

    @Column(name = "time_submitted")
    private LocalDateTime timeSubmitted;

    @Column(name = "esmClass")
    private int esmClass;

    @Column(name = "campaignId")
    private int campaignId;

    @Column(name = "messageType")
    private int messageType;

    @Column(name = "applicationType")
    private String applicationType;
}
