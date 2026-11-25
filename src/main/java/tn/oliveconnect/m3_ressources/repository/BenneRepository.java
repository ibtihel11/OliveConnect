package tn.oliveconnect.m3_ressources.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.m3_ressources.entity.Benne;

import java.util.List;

public interface BenneRepository extends MongoRepository<Benne, String> {
    @Query(value = "{ 'immatriculation': { '$regex': ?0, '$options': 'i' } }", exists = true)
    boolean existsByImmatriculationEquivalent(String regex);

    Page<Benne> findByDisponibiliteTrue(Pageable pageable);
    List<Benne> findByDisponibiliteTrue(Sort sort);
}
