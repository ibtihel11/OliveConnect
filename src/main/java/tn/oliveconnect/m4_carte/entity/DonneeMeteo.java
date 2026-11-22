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

/** Collection meteo : _id, vergerId, dateReleve, temperature (°C), humidite (%), precipitation (mm). */
@Document(collection = "meteo")
@CompoundIndex(name = "vergerId_dateReleve", def = "{'vergerId': 1, 'dateReleve': -1}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonneeMeteo {

    @Id
    private String id;

    private String vergerId;

    private LocalDateTime dateReleve;

    private Double temperature;

    private Double humidite;

    private Double precipitation;
}
