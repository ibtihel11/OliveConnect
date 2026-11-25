package tn.oliveconnect.m3_ressources.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

/** La collection et les champs sont lus par le contrat BenneReader de M4. */
@Document(collection = "bennes")
@Getter
@Setter
@NoArgsConstructor
public class Benne extends Ressource {

    private Double capacite;

    private String immatriculation;
}
