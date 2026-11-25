package tn.oliveconnect.m3_ressources.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ouvriers")
@Getter
@Setter
@NoArgsConstructor
public class Ouvrier extends Ressource {

    private String specialite;

    private Boolean saisonnier;
}
