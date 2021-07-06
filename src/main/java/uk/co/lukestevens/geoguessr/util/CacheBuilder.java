package uk.co.lukestevens.geoguessr.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CacheBuilder<T> {

    public interface ExceptionalSupplier<T> {
        T get() throws Exception;
    }

    private final Supplier<T> refreshMethod;

    private long cacheLength = 60000;
    private T cachedObject;

    private CacheBuilder(Supplier<T> refreshMethod) {
        this.refreshMethod = refreshMethod;
    }

    public static <T> CacheBuilder<T> withSource(ExceptionalSupplier<T> refreshMethod) {
        return new CacheBuilder<>(() -> {
            try {
                return refreshMethod.get();
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        });
    }

    public CacheBuilder<T> withCacheLength(long time, TimeUnit unit) {
        this.cacheLength = unit.toMillis(time);
        return this;
    }

    public CacheBuilder<T> withInitialObject(T cachedObject) {
        this.cachedObject = cachedObject;
        return this;
    }

    public CacheBuilder<T> isEager(boolean isEager){
        if(isEager) {
            this.cachedObject = refreshMethod.get();
        }
        return this;
    }

    public Cached<T> build() {
        return new Cached<>(cacheLength, refreshMethod, cachedObject);
    }

}
