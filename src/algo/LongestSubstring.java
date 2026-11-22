package algo;

import java.util.*;
import java.util.stream.Collectors;

public class LongestSubstring {

    public static void main(String[] args) {

        String input = "afabcdeabcbb";
        int result = longestSubstring(input);
        System.out.println(result);
    }

    static int longestSubstring(String input) {

        // Write your logic here
        Set<Character> charSet = new HashSet<>();
        int maxLength= 0;
        int left = 0;
        int startIndex = 0;
        for(int right = 0; right< input.length(); right++){
            char ch = input.charAt(right);
            while(charSet.contains(ch)){
                charSet.remove(input.charAt(left));
                left++;
            }
            charSet.add(ch);
            if(right - left + 1 > maxLength) {
                startIndex = left;
                maxLength = Math.max(maxLength, right - left + 1);

            }

        }
        System.out.println(input.substring(startIndex, startIndex+maxLength));
        return maxLength;
    }
}
