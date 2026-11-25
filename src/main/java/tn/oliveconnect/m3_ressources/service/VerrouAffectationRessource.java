package tn.oliveconnect.m3_ressources.service;

import org.springframework.stereotype.Component;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/** Verrou local à une JVM, partagé uniquement par les opérations visant le même type et la même ressource. */
@Component
public class VerrouAffectationRessource {

    private final ConcurrentMap<CleRessource, EntreeVerrou> entrees = new ConcurrentHashMap<>();

    public <T> T executer(TypeRessource type, String ressourceId, Supplier<T> operation) {
        CleRessource cle = new CleRessource(Objects.requireNonNull(type), Objects.requireNonNull(ressourceId));
        EntreeVerrou entree = entrees.compute(cle, (ignore, existante) -> {
            EntreeVerrou retenue = existante == null ? new EntreeVerrou() : existante;
            retenue.references++;
            return retenue;
        });

        entree.verrou.lock();
        try {
            return operation.get();
        } finally {
            entree.verrou.unlock();
            entrees.compute(cle, (ignore, courante) -> {
                if (courante != entree) {
                    throw new IllegalStateException("Entrée de verrou de ressource incohérente");
                }
                entree.references--;
                return entree.references == 0 ? null : entree;
            });
        }
    }

    private record CleRessource(TypeRessource type, String id) { }

    private static final class EntreeVerrou {
        private final ReentrantLock verrou = new ReentrantLock();
        /** Modifié uniquement dans les opérations atomiques de ConcurrentHashMap.compute pour cette clé. */
        private int references;
    }
}
