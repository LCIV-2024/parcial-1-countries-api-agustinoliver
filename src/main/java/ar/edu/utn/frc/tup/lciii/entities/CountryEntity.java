package ar.edu.utn.frc.tup.lciii.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "country")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String code;

    @Column
    private String name;
}
