import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K,V> extends LinkedHashMap<K,V> {

   private int initialCapacity;

    public LRUCache(int initialCapacity) {
        super(initialCapacity, .75f, true);
        this.initialCapacity = initialCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > initialCapacity;
    }

    public static void main(String[] args){
        LRUCache<Integer,String> lruCache = new LRUCache<>(3);
        lruCache.put(1,"kiyan");
        lruCache.put(2,"ravi");
        lruCache.put(3,"rajesh");

        lruCache.get(1);
        System.out.println(lruCache);
        lruCache.put(4,"sudha");
        System.out.println(lruCache);
    }
}