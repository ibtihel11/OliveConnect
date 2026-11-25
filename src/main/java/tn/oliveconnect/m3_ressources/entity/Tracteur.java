package tn.oliveconnect.m3_ressources.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tracteurs")
@Getter
@Setter
@NoArgsConstructor
public class Tracteur extends Ressource {

    private String immatriculation;

    private Double puissance;
}
