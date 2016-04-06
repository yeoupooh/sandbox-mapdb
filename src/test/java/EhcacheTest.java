import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by Thomas on 4/6/2016.
 */
public class EhcacheTest {
    private static final String PERSON_CACHE = "PersonCache";
    private static final String PERSON_DATA = "PersonData";

    @Test
    public void managedCache() throws Exception {
        CacheManager cacheManager
                = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class))
                .build();
        cacheManager.init();

        assertSimpleData(cacheManager);

        cacheManager.close();
    }

    @Test
    public void diskPersistence() throws Exception {
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(getStoragePath() + File.separator + "myData"))
                .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class)
                        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(10, EntryUnit.ENTRIES)
                                .disk(10L, MemoryUnit.MB, true))
                )
                .build(true);

        assertSimpleData(persistentCacheManager);

        persistentCacheManager.close();
    }

    @Ignore
    @Test
    public void putMassiveDataToDisk() throws Exception {
        PersistentCacheManager persistentCacheManager = createPersistentCacheManager();

        putMassiveData(persistentCacheManager);

        persistentCacheManager.close();
    }

    private PersistentCacheManager createPersistentCacheManager() {
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(getStoragePath() + File.separator + PERSON_DATA))
                .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, PersonPOJO.class)
                        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(10, EntryUnit.ENTRIES)
                                .disk(10L, MemoryUnit.MB, true))
                )
                .build(true);
        return persistentCacheManager;
    }

    @Test
    public void getDataFromDisk() throws Exception {
        PersistentCacheManager persistentCacheManager =getPersistentCacheManager();
//                PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
//                .with(CacheManagerBuilder.persistence(getStoragePath() + File.separator + PERSON_DATA))
//                .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, PersonPOJO.class)
//                        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
//                                .heap(10, EntryUnit.ENTRIES)
//                                .disk(10L, MemoryUnit.MB, true))
//                )
//                .build(true);

        Cache<String, PersonPOJO> myCache = getCache(persistentCacheManager);
//        Cache<String, PersonPOJO> preConfigured =
//                cacheManager.getCache("preConfigured", String.class, PersonPOJO.class);
        assertEquals(new PersonPOJO("name1234567", 1234567), myCache.get("person1234567"));

        putMassiveData(persistentCacheManager);

        persistentCacheManager.close();
    }

    private PersistentCacheManager getPersistentCacheManager() {
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(getStoragePath() + File.separator + PERSON_DATA))
                .withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, PersonPOJO.class)
                        .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                .heap(10, EntryUnit.ENTRIES)
                                .disk(10L, MemoryUnit.MB, true))
                )
                .build(true);
        return persistentCacheManager;
    }

    private Cache<String, PersonPOJO> getCache(CacheManager cacheManager) {
        Cache<String, PersonPOJO> myCache = cacheManager.getCache(PERSON_CACHE, String.class, PersonPOJO.class);
        return myCache;
    }

    private Cache<String, PersonPOJO> createCache(CacheManager cacheManager) {
        Cache<String, PersonPOJO> myCache = cacheManager.createCache(PERSON_CACHE,
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, PersonPOJO.class).build());
        return myCache;
    }

    private void putMassiveData(CacheManager cacheManager) {
//        Cache<String, PersonPOJO> preConfigured =
//                cacheManager.getCache("preConfigured", String.class, PersonPOJO.class);
//        Cache<String, PersonPOJO> myCache = cacheManager.createCache("myCache",
//                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, PersonPOJO.class).build());
        Cache<String, PersonPOJO> myCache = createCache(cacheManager);

        for (int i = 0; i < 500 * 10000; i++) {
            if (i % 10 * 10000 == 0) {
                String key = "person" + i;
                PersonPOJO value = new PersonPOJO("name" + i, i);
                System.out.println(i + ": " + key);
                myCache.put(key, value);
            }
        }
    }

    private void assertSimpleData(CacheManager cacheManager) {
        Cache<Long, String> preConfigured =
                cacheManager.getCache("preConfigured", Long.class, String.class);

        Cache<Long, String> myCache = cacheManager.createCache("myCache",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class).build());

        myCache.put(1L, "da one!");
        String value = myCache.get(1L);
        assertEquals("da one!", value);

        cacheManager.removeCache("preConfigured");
    }

    private String getStoragePath() {
        return ".";
    }
}
