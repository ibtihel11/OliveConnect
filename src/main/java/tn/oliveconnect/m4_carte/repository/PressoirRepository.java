package tn.oliveconnect.m4_carte.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import tn.oliveconnect.m4_carte.entity.Pressoir;

import java.util.Optional;

public interface PressoirRepository extends MongoRepository<Pressoir, String> {

    Optional<Pressoir> findByResponsableId(String responsableId);

    Page<Pressoir> findByResponsableId(String responsableId, Pageable pageable);
}
