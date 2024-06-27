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
@Entity(name = "Component")
@Table(name = "component")
public class Component {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer componentId;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "controller")
    private String controller;

    @Column(name = "icon")
    private String icon;

    @Column(name = "title")
    private String title;

    @Column(name = "tab_name")
    private String tabName;

    @Column(name = "path_name")
    private String pathName;

    @Column(name = "tab_details")
    private String tabDetails;

    @Column(name = "icon_name")
    private String iconName;
}
