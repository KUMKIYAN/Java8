package algo;

public class ReverseWords {

    public static String reverseWords(String input) {

        String[] split = input.split(" ");
        StringBuffer sb = new StringBuffer();
        for(int i = split.length-1 ; i >=0; i--){
            sb.append(split[i] + " ");
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) {

        System.out.println(reverseWords("Java is awesome"));
    }
}