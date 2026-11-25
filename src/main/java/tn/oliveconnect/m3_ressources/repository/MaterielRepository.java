package tn.oliveconnect.m3_ressources.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.m3_ressources.entity.Materiel;

import java.util.List;

public interface MaterielRepository extends MongoRepository<Materiel, String> {
    Page<Materiel> findByDisponibiliteTrue(Pageable pageable);
    List<Materiel> findByDisponibiliteTrue(Sort sort);
}
