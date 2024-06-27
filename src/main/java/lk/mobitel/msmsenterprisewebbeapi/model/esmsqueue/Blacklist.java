package lk.mobitel.msmsenterprisewebbeapi.model.esmsqueue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklist")
public class Blacklist {
    @Id
    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "alias")
    private String alias;
}
