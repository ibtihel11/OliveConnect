package tn.oliveconnect.m3_ressources.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;

import java.util.List;

public interface OuvrierRepository extends MongoRepository<Ouvrier, String> {
    Page<Ouvrier> findByDisponibiliteTrue(Pageable pageable);
    List<Ouvrier> findByDisponibiliteTrue(Sort sort);
}
