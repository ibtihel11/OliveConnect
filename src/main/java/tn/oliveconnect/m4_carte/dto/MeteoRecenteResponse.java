package tn.oliveconnect.m4_carte.dto;

import java.time.LocalDateTime;

public record MeteoRecenteResponse(LocalDateTime dateReleve, Double temperature, Double humidite, Double precipitation) {
}
