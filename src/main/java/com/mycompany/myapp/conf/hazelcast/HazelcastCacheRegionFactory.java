package com.mycompany.myapp.conf.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.HazelcastTimestamper;
import com.hazelcast.hibernate.local.CleanupService;
import com.hazelcast.hibernate.local.LocalRegionCache;
import com.hazelcast.hibernate.local.TimestampsRegionCache;
import com.hazelcast.hibernate.region.*;
import com.mycompany.myapp.conf.CacheConfiguration;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.*;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HazelcastCacheRegionFactory implements RegionFactory {

    private static Logger logger = LoggerFactory.getLogger(HazelcastCacheRegionFactory.class);

    private HazelcastInstance hazelcastInstance;

    private CleanupService cleanupService;

    public HazelcastCacheRegionFactory() {
        super();
        hazelcastInstance = CacheConfiguration.getHazelcastInstance();
    }

    /**
     * @return true - for a large cluster, unnecessary puts will most likely slow things down.
     */
    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }

    public final QueryResultsRegion buildQueryResultsRegion(final String regionName, final Properties properties)
            throws CacheException {
                
        return new HazelcastQueryResultsRegion(hazelcastInstance, regionName, properties);
    }

    public NaturalIdRegion buildNaturalIdRegion(final String regionName, final Properties properties, final CacheDataDescription metadata)
            throws CacheException {

        return new HazelcastNaturalIdRegion(hazelcastInstance, regionName, properties, metadata);
    }

    public CollectionRegion buildCollectionRegion(final String regionName, final Properties properties,
                                                  final CacheDataDescription metadata) throws CacheException {

        HazelcastCollectionRegion<LocalRegionCache> region = new HazelcastCollectionRegion<LocalRegionCache>(hazelcastInstance,
                regionName, properties, metadata, new LocalRegionCache(regionName, hazelcastInstance, metadata));

        cleanupService.registerCache(region.getCache());
        return region;
    }

    public EntityRegion buildEntityRegion(final String regionName, final Properties properties,
                                          final CacheDataDescription metadata) throws CacheException {

        HazelcastEntityRegion<LocalRegionCache> region = new HazelcastEntityRegion<LocalRegionCache>(hazelcastInstance,
                regionName, properties, metadata, new LocalRegionCache(regionName, hazelcastInstance, metadata));

        cleanupService.registerCache(region.getCache());
        return region;
    }

    public TimestampsRegion buildTimestampsRegion(final String regionName, final Properties properties)
            throws CacheException {
        return new HazelcastTimestampsRegion<LocalRegionCache>(hazelcastInstance, regionName, properties,
                new TimestampsRegionCache(regionName, hazelcastInstance));
    }

    public void start(final Settings settings, final Properties properties) throws CacheException {
        // Do nothing the hazelcalst hazelcastInstance is injected
        logger.info("Starting up " + getClass().getSimpleName());

        if (hazelcastInstance == null) {
            throw new IllegalArgumentException("Hazelcast hazelcastInstance must not be null");
        }
        cleanupService = new CleanupService(hazelcastInstance.getName());
    }

    public void stop() {
        // Do nothing the hazelcalst instance is managed globally
        logger.info("Shutting down " + getClass().getSimpleName());
        cleanupService.stop();
    }

    public AccessType getDefaultAccessType() {
        return AccessType.READ_WRITE;
    }

    @Override
    public long nextTimestamp() {
        return HazelcastTimestamper.nextTimestamp(hazelcastInstance);
    }
}
