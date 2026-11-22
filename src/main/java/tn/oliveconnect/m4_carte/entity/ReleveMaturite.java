package tn.oliveconnect.m4_carte.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/** Collection releves_maturite : historique complet (les 5 derniers sont aussi imbriqués dans vergers). */
@Document(collection = "releves_maturite")
@CompoundIndex(name = "vergerId_dateReleve", def = "{'vergerId': 1, 'dateReleve': -1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleveMaturite {

    @Id
    private String id;

    private String vergerId;

    private LocalDateTime dateReleve;

    /** Niveau de maturité en pourcentage (0 à 100). */
    private Double niveauMaturite;
}
