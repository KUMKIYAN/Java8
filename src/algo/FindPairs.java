package algo;

import java.util.*;

public class FindPairs {

    public static void main(String[] args) {

        int[] input = {2, 7, 11, 15, 3, 6};
        int target = 9;

        List<List<Integer>> result = findPairs(input, target);

        System.out.println(result);
    }

    static List<List<Integer>> findPairs(int[] input, int target) {

        // Write your logic here
        List<List<Integer>> targetPair = new ArrayList<>();
        Set<String> uniquePair = new HashSet<>();
        Set<Integer> seen = new HashSet<>();
        for(Integer n : input) {
            int complement = target - n;
            if(seen.contains(complement)){
                int first = Math.min(complement, n);
                int second = Math.max(complement, n);
                String key = first + ":" + second;
                if(uniquePair.add(key)){
                    targetPair.add(Arrays.asList(first, second));
                }
            }
            seen.add(n);
        }

        return targetPair;
    }
}
