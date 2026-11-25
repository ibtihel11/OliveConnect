package tn.oliveconnect.m3_ressources.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "materiels")
@Getter
@Setter
@NoArgsConstructor
public class Materiel extends Ressource {

    private String type;

    private Integer quantiteStock;

    private Integer seuilMinimum;
}
