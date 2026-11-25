package tn.oliveconnect.m3_ressources.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/** Attributs communs aux ressources persistées dans des collections distinctes. */
@Getter
@Setter
@NoArgsConstructor
public abstract class Ressource {

    @Id
    private String id;

    private String nom;

    private Boolean disponibilite;
}
