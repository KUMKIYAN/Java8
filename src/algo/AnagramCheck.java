package algo;

import java.util.List;
import java.util.stream.*;

public class AnagramCheck {

    public static boolean isAnagram(String str1, String str2) {
        if(null == str1 || null == str2){
            return false;
        }
        if(str1.length() != str2.length()) {
            return false;
        }

        String collect1 = str1.chars().sorted().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
        String collect2 = str2.chars().sorted().mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());

return collect1.equals(collect2);

    }

    public static void main(String[] args) {

        System.out.println("Expected: true, Actual: " +
                isAnagram("listen", "silent"));

        System.out.println("Expected: true, Actual: " +
                isAnagram("race", "care"));

        System.out.println("Expected: false, Actual: " +
                isAnagram("hello", "world"));

        System.out.println("Expected: false, Actual: " +
                isAnagram("java", "jav"));

        System.out.println("Expected: true, Actual: " +
                isAnagram("aabbcc", "abcabc"));

        System.out.println("Expected: true, Actual: " +
                isAnagram("Dormitory", "Dirtyroom"));
    }
}