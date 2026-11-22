package tn.oliveconnect.m4_carte.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import tn.oliveconnect.m4_carte.entity.Reception;

public interface ReceptionRepository extends MongoRepository<Reception, String> {

    Page<Reception> findByTourneeId(String tourneeId, Pageable pageable);
}
