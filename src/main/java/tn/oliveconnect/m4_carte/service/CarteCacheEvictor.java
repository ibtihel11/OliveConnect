package tn.oliveconnect.m4_carte.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Vide périodiquement les caches de la carte (durée de vie = oliveconnect.carte.cache-ttl-ms). */
@Component
public class CarteCacheEvictor {

    private final CacheManager cacheManager;

    public CarteCacheEvictor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRateString = "${oliveconnect.carte.cache-ttl-ms:30000}")
    public void vider() {
        for (String nom : new String[]{CarteService.CACHE_PUBLIQUE, CarteService.CACHE_COMPLETE}) {
            Cache cache = cacheManager.getCache(nom);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
