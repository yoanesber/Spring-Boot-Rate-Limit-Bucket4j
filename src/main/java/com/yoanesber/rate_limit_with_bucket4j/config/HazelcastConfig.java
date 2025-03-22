package com.yoanesber.rate_limit_with_bucket4j.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    /*
     * The following properties are used to configure the map:
     * 
     * TimeToLiveSeconds (TTL)
     * - This property is used to configure the time-to-live (TTL) for the rate limiting map.
     * - The TTL is the maximum time that an entry can remain in the map before it is automatically removed.
     * - The default value is 0, which means that the entries do not expire.
     * - The TTL is specified in seconds.
     * 
     * BackupCount
     * - This property is used to configure the number of backup copies of the rate limiting map.
     * - The backup copies are additional copies of the map entries that are stored on other nodes in the cluster.
     * - The default value is 1, which means that there is one backup copy of each entry.
     * 
     * EvictionPolicy
     * - This property is used to configure the eviction policy for the rate limiting map.
     * - The eviction policy determines how entries are removed from the map when the maximum size is reached.
     * - The available options are "NONE", "LRU" (Least Recently Used), "LFU" (Least Frequently Used), and "RANDOM".
     * - The NONE means that no eviction policy is applied. The map will grow indefinitely.
     * - The LRU policy is suitable for rate limiting, as it removes the least recently used entries first.
     * - The LFU policy is also suitable, as it removes the least frequently used entries first.
     * - The RANDOM policy is not suitable, as it removes entries randomly.
     * 
     * MaxSizePolicy
     * - This property is used to configure the max size policy for the rate limiting map.
     * - The max size policy determines how the maximum size is calculated.
     * - The available options are:
     *  - "ENTRY_COUNT" (the number of entries). It means that the maximum size is based on the number of entries. The map will grow until the maximum number of entries is reached.
     *  - "USED_HEAP_SIZE" (the memory used by the entries). It means that the maximum size is based on the memory used by the entries. The map will grow until the maximum memory is reached.
     *  - "USED_HEAP_PERCENTAGE" (the percentage of memory used by the entries). It means that the maximum size is based on the percentage of memory used by the entries. The map will grow until the maximum percentage of memory is reached.
     * 
     * EvictionSize
     * - This property is used to configure the size of the eviction policy for the rate limiting map.
     * - If MaxSizePolicy is "ENTRY_COUNT", the size is the number of entries to remove. If the size is 100, it means that 100 entries will be removed.
     * - If MaxSizePolicy is "USED_HEAP_SIZE", the size is the memory used by the entries to remove. If the size is 100, it means that 100 MB of memory used by the entries will be removed.
     * - If MaxSizePolicy is "USED_HEAP_PERCENTAGE", the size is the percentage of memory used by the entries to remove. If the size is 80, it means that 80% of the memory used by the entries will be removed.
     * 
     * MergePolicy
     * - This property is used to configure the merge policy for the rate limiting map.
     * - The merge policy determines how entries are merged when they are updated on different nodes in the cluster.
     * - The available options are:
     * - "PutIfAbsentMergePolicy" (default): Keeps the existing value if present; otherwise, takes the merging value.
     * - "HigherHitsMergePolicy": Takes the value that has been accessed more times.
     * - "LatestUpdateMergePolicy": Takes the most recently updated value.
     * - "PassThroughMergePolicy": Always takes the merging value (overwrites).
     * - "DiscardMergePolicy": Always keeps the existing value (discards merging value).
     * 
     */

    @Value("${hazelcast.map.config.rate-limit.name}")
    private String mapConfigRateLimitName;

    @Value("${hazelcast.map.config.rate-limit.ttl}")
    private int mapConfigRateLimitTtl;

    @Value("${hazelcast.map.config.rate-limit.backup.count}")
    private int mapConfigRateLimitBackupCount;

    @Value("${hazelcast.map.config.rate-limit.eviction.policy}")
    private String mapConfigRateLimitEvictionPolicy;

    @Value("${hazelcast.map.config.rate-limit.max.size.policy}")
    private String mapConfigRateLimitMaxSizePolicy;

    @Value("${hazelcast.map.config.rate-limit.eviction.size}")
    private int mapConfigRateLimitEvictionSize;

    @Value("${hazelcast.map.config.rate-limit.merge.policy}")
    private String mapConfigRateLimitMergePolicy;

    /**
     * Configures Hazelcast instance with the required maps.
     * Multiple maps can be configured with different configurations.
     *
     * @return HazelcastInstance
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();

        // Rate Limiting Map Configuration
        MapConfig rateLimitMap = new MapConfig(mapConfigRateLimitName)
            .setTimeToLiveSeconds(mapConfigRateLimitTtl)
            .setBackupCount(mapConfigRateLimitBackupCount)
            .setEvictionConfig(new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.valueOf(mapConfigRateLimitEvictionPolicy))
                .setSize(mapConfigRateLimitEvictionSize)
                .setMaxSizePolicy(MaxSizePolicy.valueOf(mapConfigRateLimitMaxSizePolicy)))
            .setMergePolicyConfig(new MergePolicyConfig()
                .setPolicy(mapConfigRateLimitMergePolicy));

        // Add the Rate Limiting Map Configuration to the Hazelcast instance
        config.addMapConfig(rateLimitMap);

        // Configuration for other maps can be added here
        // MapConfig otherMap = new MapConfig("otherMap")
        //     .setTimeToLiveSeconds(...)
        //     .setBackupCount(...)
        // config.addMapConfig(otherMap);

        return Hazelcast.newHazelcastInstance(config);
    }
}
