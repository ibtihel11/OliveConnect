package tn.oliveconnect.m4_carte.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Collection receptions : _id, tourneeId, benneId, pressoirId, dateReception, poids, qualite (cadrage 4.2).
 * Index (cadrage 4.3) : tourneeId, pressoirId, dateReception.
 */
@Document(collection = "receptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reception {

    @Id
    private String id;

    @Indexed
    private String tourneeId;

    private String benneId;

    @Indexed
    private String pressoirId;

    @Indexed
    private LocalDateTime dateReception;

    private Double poids;

    private QualiteReception qualite;
}
