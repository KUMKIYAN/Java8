package algo;

import java.util.*;
import java.util.stream.*;

public class LongestConsecutive1 {

    public static int longestConsecutive(int[] nums) {
        Set<Integer> numbers = Arrays.stream(nums).boxed().collect(Collectors.toSet());
        int longestLength = 0;
        for(Integer num : numbers){
            if(!numbers.contains(num-1)){
                int currentNumber = num+1;
                int currentLength = 1;
                while(numbers.contains(currentNumber)){
                    currentLength++;
                    currentNumber++;
                }
                longestLength = Math.max(currentLength, longestLength);
            }
        }
        return longestLength;
    }

    public static void main(String[] args) {

        System.out.println("Expected: 4 Actual: " +
            longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}));

        System.out.println("Expected: 4 Actual: " +
            longestConsecutive(new int[]{10, 5, 12, 3, 55, 4, 11, 2}));

        System.out.println("Expected: 3 Actual: " +
            longestConsecutive(new int[]{1, 2, 0, 1}));

        System.out.println("Expected: 1 Actual: " +
            longestConsecutive(new int[]{5}));

        System.out.println("Expected: 0 Actual: " +
            longestConsecutive(new int[]{}));
    }
}