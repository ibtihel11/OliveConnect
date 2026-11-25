package tn.oliveconnect.m3_ressources.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.m3_ressources.entity.Tracteur;

import java.util.List;

public interface TracteurRepository extends MongoRepository<Tracteur, String> {
    @Query(value = "{ 'immatriculation': { '$regex': ?0, '$options': 'i' } }", exists = true)
    boolean existsByImmatriculationEquivalent(String regex);

    Page<Tracteur> findByDisponibiliteTrue(Pageable pageable);
    List<Tracteur> findByDisponibiliteTrue(Sort sort);
}
