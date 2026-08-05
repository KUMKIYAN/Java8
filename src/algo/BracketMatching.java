package algo;

import java.util.Stack;

public class BracketMatching {

    public static boolean isValid(String s){
        Stack<Character> stack = new Stack<>();

        for(Character ch : s.toCharArray()){
            if(ch == '(' || ch == '{' || ch == '['){
                stack.push(ch);
            }
            else if(ch == ')' || ch == ']' || ch == '}' ){
                if(stack.isEmpty()){
                    return false;
                }
                char top = stack.pop();
                if(ch ==  ')' && top != '(' ) return false;
                if(ch ==  '}' && top != '{' ) return false;
                if(ch ==  ']' && top != '[' ) return false;
            }
        }
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        System.out.println(isValid("(abc)[k]{}"));
    }
}
