package tn.oliveconnect.m4_carte.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Collection pressoirs : _id, nom, adresse, capaciteTraitement, responsableId (cadrage 4.2). */
@Document(collection = "pressoirs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pressoir {

    @Id
    private String id;

    private String nom;

    private String adresse;

    private Double capaciteTraitement;

    /** Relation 1 - 1 ResponsablePressoir - Pressoir ("gère"). Index unique, sparse (pressoirs importés sans responsable). */
    @Indexed(unique = true, sparse = true)
    private String responsableId;
}
