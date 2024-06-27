package lk.mobitel.msmsenterprisewebbeapi.model.esms;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "componentrole")
@Entity(name = "ComponentRole")
public class ComponentRole {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer componentRoleId;

    @Column(name = "componentid")
    private Integer componentId;

    @Column(name = "roleid")
    private Integer roleId;
}
