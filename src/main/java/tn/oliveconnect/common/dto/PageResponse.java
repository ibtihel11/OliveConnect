package tn.oliveconnect.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Réponse paginée du contrat (6.1) : {content, page, size, totalElements}. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
