package com.yoanesber.rate_limit_with_bucket4j.service.impl;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

import java.util.Map;

import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.github.bucket4j.*;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.grid.hazelcast.Bucket4jHazelcast;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.yoanesber.rate_limit_with_bucket4j.service.RateLimiterService;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "rate-limit:";
    private final IMap<String, byte[]> rateLimitMap;
    private final HazelcastProxyManager<String> proxyManager;

    /* 
     * This name should be the same as the name of the map configuration in the Hazelcast configuration file
     * This is to ensure that the map configuration is used when creating the map in the Hazelcast instance
    */
    private static final String mapConfigRateLimitName = "rate-limit-map";

    /*
     * Create a jitter for eviction
     * This is to prevent all the buckets from being evicted at the same time
     * This is to prevent a thundering herd problem
     */
    @Value("${rate.limiter.eviction-jitter}")
    private long evictionJitter;

    /*
     * Create an expiration strategy based on time for refilling the bucket up to the maximum
     * This is to prevent the bucket from being evicted before it is refilled
     */
    private final ExpirationAfterWriteStrategy expiration = ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(ofSeconds(evictionJitter));

    // This means that the bucket will have a capacity of 5 tokens
    @Value("${rate.limiter.token.limit-capacity}")
    private long TOKEN_LIMIT_CAPACITY;

    // This means that the bucket will be refilled with 5 tokens every minute
    @Value("${rate.limiter.token.refill.amount}")
    private long TOKEN_REFILL_AMOUNT;

    // This means that the bucket will be refilled every minute
    @Value("${rate.limiter.token.refill.period}")
    private long TOKEN_REFILL_PERIOD;

    public RateLimiterServiceImpl(@Qualifier("hazelcastInstance") HazelcastInstance instance) {
        // Print all the map configurations
        this.printAllMapConfigs(instance.getConfig().getMapConfigs());

        // Get the map from the Hazelcast instance
        this.rateLimitMap = instance.getMap(mapConfigRateLimitName); 

        // Create a new Hazelcast proxy manager
        this.proxyManager = Bucket4jHazelcast
            .entryProcessorBasedBuilder(rateLimitMap)
            .expirationAfterWrite(expiration)
            .build();
    }

    // Print all the map configurations
    private void printAllMapConfigs(Map<String, MapConfig> mapConfigs) {
        for (Map.Entry<String, MapConfig> entry : mapConfigs.entrySet()) {
            String mapName = entry.getKey();
            MapConfig mapConfig = entry.getValue();
            System.out.println("Map Name: " + mapName);
            this.printMapConfigs(mapConfig);
        }
    }

    // Print the map configurations
    private void printMapConfigs(MapConfig mapConfig) {
        System.out.println("TTL: " + mapConfig.getTimeToLiveSeconds() + " seconds");
        System.out.println("Backup Count: " + mapConfig.getBackupCount());
        System.out.println("Async Backup Count: " + mapConfig.getAsyncBackupCount());
        System.out.println("Eviction Policy: " + mapConfig.getEvictionConfig().getEvictionPolicy());
        System.out.println("Max Size Policy: " + mapConfig.getEvictionConfig().getMaxSizePolicy());
        System.out.println("Merge Policy: " + mapConfig.getMergePolicyConfig().getPolicy());
        System.out.println("------------------------------------------------");
    }

    @Override
    public Bucket resolveBucket(String key) {

        /*
         * Get the bucket from the proxy manager
         * If the bucket does not exist, create a new bucket with the following configuration:
         * - The bucket will have a capacity of 5 tokens
         * - The bucket will be refilled with 5 tokens every minute
         */ 
        return this.proxyManager.getProxy(RATE_LIMIT_PREFIX + key, () -> 
            BucketConfiguration.builder()
            .addLimit(limit -> limit.capacity(TOKEN_LIMIT_CAPACITY).refillGreedy(TOKEN_REFILL_AMOUNT, ofMinutes(TOKEN_REFILL_PERIOD)))
            .build());
    }
}
