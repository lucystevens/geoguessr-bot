package uk.co.lukestevens.geoguessr.util;

import uk.co.lukestevens.utils.Dates;

import java.util.Date;
import java.util.function.Supplier;

public class Cached<T> {

    private final long cacheLength;
    private final Supplier<T> refreshMethod;

    private T cachedObject;
    private Date cacheExpiry;

    Cached(long cacheLength, Supplier<T> refreshMethod, T cachedObject) {
        this.cacheLength = cacheLength;
        this.refreshMethod = refreshMethod;
        this.cachedObject = cachedObject;
        this.cacheExpiry = cachedObject != null?
                new Date(Dates.millis() + cacheLength) :
                Dates.now();
    }

    public T get(){
        return get(false);
    }

    public T get(boolean forceRefresh){
        if(forceRefresh || isExpired()){
            cachedObject = refreshMethod.get();
            cacheExpiry = new Date(Dates.millis() + cacheLength);
        }
        return cachedObject;
    }

    public boolean isExpired(){
        return cacheExpiry.before(Dates.now());
    }

}
