package algo;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class MainFirstNonRepeating {

    public static Character firstNonRepeating(String input) {

        return input.chars().mapToObj(c -> (char) c)
        .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
        .entrySet().stream().filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey).findFirst().orElse(null);


    }

    public static void main(String[] args) {

        String input1 = "swiss";
        System.out.println("Expected: w | Actual: " + firstNonRepeating(input1));

        String input2 = "aabbcde";
        System.out.println("Expected: c | Actual: " + firstNonRepeating(input2));

        String input3 = "aabb";
        System.out.println("Expected: null | Actual: " + firstNonRepeating(input3));

        String input4 = "programming";
        System.out.println("Expected: p | Actual: " + firstNonRepeating(input4));

        String input5 = "aabbccxyz";
        System.out.println("Expected: x | Actual: " + firstNonRepeating(input5));
    }
}
