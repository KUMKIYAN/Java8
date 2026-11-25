package algo;

import java.util.*;

class LRUCache1 {

    private final int capacity;

    private final LinkedHashMap<Integer, Integer> cache;

    public LRUCache1(int capacity) {
        this.capacity  = capacity;
        cache = new LinkedHashMap<>(capacity,0.75f,true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    public int get(int key) {
        Integer i = cache.get(key);
        return i == null ? -1 : i;
    }

    public void put(int key, int value) {
        cache.put(key, value);
    }


}

public class LRUCacheHashMap {

    public static void main(String[] args) {

        LRUCache1 cache = new LRUCache1(2);

        cache.put(1, 100);
        cache.put(2, 200);

        System.out.println(cache.get(1)); // Expected: 100

        cache.put(3, 300);

        System.out.println(cache.get(2)); // Expected: -1

        cache.put(4, 400);

        System.out.println(cache.get(1)); // Expected: -1
        System.out.println(cache.get(3)); // Expected: 300
        System.out.println(cache.get(4)); // Expected: 400
    }
}
