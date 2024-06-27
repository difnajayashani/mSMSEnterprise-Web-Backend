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
@Table(name = "sent_messages_ceb")
public class SentMessageCeb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "alias")
    private String alias;

    @Column(name = "customer")
    private Integer customer;

    @Column(name = "user")
    private Integer user;

    @Column(name = "message")
    private String message;

    @Column(name = "msgsegments")
    private Integer msgSegments;

    @Column(name = "sendQId")
    private Long sendQId;

    @Column(name = "messageId")
    private Long messageId;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    private Integer status;

    @Column(name = "time_sent")
    private LocalDateTime timeSent;

    @Column(name = "time_submitted")
    private LocalDateTime timeSubmitted;

    @Column(name = "esmClass")
    private Integer esmClass;

    @Column(name = "campaignId")
    private Integer campaignId;

    @Column(name = "messageType")
    private Integer messageType;

    @Column(name = "applicationType")
    private String applicationType;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "cebBranch")
    private String cebBranch;

    @Column(name = "cebApp")
    private String cebApp;

    @Column(name = "deliveryStatus")
    private String deliveryStatus;

    @Column(name = "userRefId")
    private String userRefId;
}
