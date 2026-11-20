import java.util.*;
import java.util.stream.Collectors;

public class LongestConsecutive {

    public static void main(String[] args) {
        int[] input = {100, 4, 200, 1, 3, 2};
        int result = longestConsecutive(input);
        System.out.println(result);
    }

    static int longestConsecutive(int[] input) {

        Set<Integer> inputSet = Arrays.stream(input).boxed().collect(Collectors.toSet());
        int longestSequence = 0;
        for(Integer num : inputSet){
            if(!inputSet.contains(num-1)){
                int currentNumber=num;
                int currentLongSequence = 1;
                while(inputSet.contains(currentNumber+1)){
                    currentNumber++;
                    currentLongSequence++;
                }
                longestSequence = Math.max(currentLongSequence, longestSequence);
            }
        }
        return longestSequence;
    }
}
