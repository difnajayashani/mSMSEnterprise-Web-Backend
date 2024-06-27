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
@Table(name = "campaign_archive")
public class CampaignArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "alias")
    private String alias;

    @Column(name = "numberlistId")
    private int numberListId;

    @Column(name = "message", length = 5000)
    private String message;

    @Column(name = "schedule_date")
    private Date scheduleDate;

    @Column(name = "status")
    private int status;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "customer")
    private int customer;

    @Column(name = "user")
    private int user;

    @Column(name = "esmClass")
    private int esmClass;

    @Column(name = "stopAd")
    private int stopAd;
}
