package algo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class MyCalendar  {
    Map<Integer, Integer> dayCalender = new HashMap<>();
    public boolean book(int start, int end) {
    //write code here
        boolean conflict =  dayCalender
                .entrySet().stream()
                .anyMatch(a -> (a.getKey() <= start) && (a.getValue() > start));

        if(conflict){
            return false;
        } else {
            dayCalender.put(start, end);
            return true;
        }
    }

public static void main(String[] args){

MyCalendar calendar = new MyCalendar();

    System.out.println("Expected: true  Actual: " + calendar.book(12, 14));
    System.out.println("Expected: false Actual: " + calendar.book(13, 15));
    System.out.println("Expected: true  Actual: " + calendar.book(14, 17));
    System.out.println("Expected: false Actual: " + calendar.book(16, 18));
    System.out.println("Expected: true  Actual: " + calendar.book(17, 20));
    System.out.println("Expected: false Actual: " + calendar.book(19, 22));
    System.out.println("Expected: true  Actual: " + calendar.book(20, 23));
}
}