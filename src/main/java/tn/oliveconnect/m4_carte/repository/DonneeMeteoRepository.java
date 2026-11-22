package tn.oliveconnect.m4_carte.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.oliveconnect.m4_carte.entity.DonneeMeteo;

public interface DonneeMeteoRepository extends MongoRepository<DonneeMeteo, String> {
}
