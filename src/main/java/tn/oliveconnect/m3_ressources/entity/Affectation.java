package tn.oliveconnect.m3_ressources.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "affectations")
@CompoundIndex(name = "idx_affectations_ressource_periode",
        def = "{'ressourceId': 1, 'ressourceType': 1, 'dateDebut': 1, 'dateFin': 1}")
@Getter
@Setter
@NoArgsConstructor
public class Affectation {

    @Id
    private String id;

    private String ressourceId;

    private TypeRessource ressourceType;

    /** Référence externe non vérifiée tant que le contrat M1 n'est pas disponible. */
    private String tourneeId;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    private String statut;
}
