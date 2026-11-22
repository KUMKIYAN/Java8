package algo;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;



class Student {

    public int id;
    public String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Student student = (Student) obj;

        return Objects.equals(this.name, student.name);
    }
}

public class HashSetChallenge {

    public static void main(String[] args) {

        Set<Student> students = new HashSet<>();
        Student s1 = new Student(1, "John");
        Student s2 = new Student(2, "Ravi");
        Student s3 = new Student(3,"John");

        students.add(s1);
        students.add(s2);
        students.add(s3);
        System.out.println("Set size: " + students.size());
        s1.id = 3;
        System.out.println("Set size: " + students.size());

        System.out.println(students);

        Set<Student> studentee = new HashSet<>();
        Student s4 = new Student(1, "John");
        studentee.add(s1);
        studentee.add(s4);
        System.out.println(studentee.size());

        String s = """
                ****************************************************************************************************** 
                                                HashSet Rules
                                                  
                HashSet checks for duplicates only when you insert an object (add()).
                
                After insertion:
                        - If you modify an object's fields, HashSet does not automatically check for duplicates.
                        - It does not automatically recalculate the object's bucket location.
                        - It does not automatically remove duplicate objects.
               ********************************************************************************************************           
                """;
        System.out.println(s);

    }
}
