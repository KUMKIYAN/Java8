package algo;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;

public class MissingNumber {

    public static int findMissingNumber(int[] arr, int n) {

        Set<Integer> collect = Arrays.stream(arr).boxed().collect(Collectors.toSet());
        return IntStream.rangeClosed(1,n).boxed().filter( i -> !collect.contains(i)).findFirst().orElse(-1);
    }

    public static void main(String[] args) {

        System.out.println("Expected: 3, Actual: " +
                findMissingNumber(new int[]{1, 2, 4, 5}, 5));

        System.out.println("Expected: 6, Actual: " +
                findMissingNumber(new int[]{1, 2, 3, 4, 5, 7}, 7));

        System.out.println("Expected: 1, Actual: " +
                findMissingNumber(new int[]{2, 3, 4, 5}, 5));

        System.out.println("Expected: 5, Actual: " +
                findMissingNumber(new int[]{1, 2, 3, 4}, 5));

        System.out.println("Expected: 4, Actual: " +
                findMissingNumber(new int[]{1, 2, 3, 5, 6, 7}, 7));
    }
}