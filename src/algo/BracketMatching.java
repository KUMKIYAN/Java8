package algo;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class BracketMatching {

    public static boolean isValid(String s){
        ArrayDeque<Character> stack = new ArrayDeque<>();

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


    public static boolean isValid8(String s){

        Map<Character,Character> map = Map.of('(', ')', '[', ']', '{', '}');
        Stack<Character> stack = new Stack<>();
        for(Character ch : s.toCharArray()){
            if(Map.of().containsValue(ch)){
                stack.push(ch);
            } else if(!stack.isEmpty() && stack.peek().equals(map.get(ch))){
                return false;
            }

        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isValid("(abc[k]{}"));
    }
}
