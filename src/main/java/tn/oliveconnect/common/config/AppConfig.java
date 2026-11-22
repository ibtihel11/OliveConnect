package tn.oliveconnect.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableCaching
@EnableScheduling
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** Caches en mémoire des cartes (vidés périodiquement par CarteCacheEvictor). */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("cartePublique", "carteComplete");
    }
}
