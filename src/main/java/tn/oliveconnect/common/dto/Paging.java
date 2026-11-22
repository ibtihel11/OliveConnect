package tn.oliveconnect.common.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.common.exception.BadRequestException;

/** Construction d'un Pageable validé à partir de ?page=0&size=20 (cadrage 6.1). */
public final class Paging {

    public static final int TAILLE_MAX = 100;

    private Paging() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        if (page < 0) {
            throw new BadRequestException("page doit être supérieur ou égal à 0");
        }
        if (size < 1 || size > TAILLE_MAX) {
            throw new BadRequestException("size doit être compris entre 1 et " + TAILLE_MAX);
        }
        return PageRequest.of(page, size, sort);
    }
}
