import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        List<String> stringList = new ArrayList<String>();
        stringList.add("kiyan");
        stringList.add("kumar");
        stringList.add("ravi");
        stringList.add("ravi");
        stringList.add("Kiyandoor");
        stringList.add("kk");

        stringList.stream()
                    .distinct()
                    .toList();

        stringList.stream()
                    .sorted(Comparator.comparingInt(String::length))
                    .toList();

        stringList.stream()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .toList();

        stringList.stream()
                    .collect(Collectors.joining(","));
        

       stringList.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet().stream().filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .toList();

        stringList.stream()
                    .sorted()
                    .toList();

        stringList.stream()
                    .sorted()
                    .limit(3)
                    .toList();

        stringList.stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(3)
                    .toList();

        stringList.stream()
                    .map(String::toUpperCase)
                    .toList();

        Map<String, List<String>> collect4 = stringList.stream()
                .collect(Collectors.groupingBy(k -> k.substring(0, 1)));

        
        String name = "Kiyandoor";
        Map<Character, Long> collect = name
                                        .chars()
                                        .mapToObj(c -> (char) c)
                                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));


        List<Integer> allNumber = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        
            allNumber.stream()
            .filter(x -> x % 2 == 0)
            .toList();

        String forNonRepeatedChar = "kkiyiandoor";
        
        String s =
                forNonRepeatedChar.chars()
                        .mapToObj(c -> (char) c)
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                LinkedHashMap::new,        // preserves order ← key optimization
                                Collectors.counting()))
                        .entrySet().stream()
                        .filter(e -> e.getValue() == 1)
                        .map(e -> String.valueOf(e.getKey()))
                        .findFirst()
                        .orElse("");

        // true and false Map
        allNumber.stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0));
        

        // even and add Map
        allNumber.stream()
                .collect(Collectors.groupingBy(x -> x % 2 == 0 ? "even" : "odd"));

        //list of list
        allNumber.stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0))
                .values()
                .stream()
                .toList();

        //stats
        IntSummaryStatistics stats = allNumber.stream()
                    .mapToInt(Integer::intValue)
                    .summaryStatistics();


        System.out.println(stats.getMin());
        System.out.println(stats.getMax());
        System.out.println(stats.getAverage());

        

        // list to map
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee(1, "kiyan", "it", 100));
        employeeList.add(new Employee(2, "sudha", "hardware", 200));
        employeeList.add(new Employee(3, "ravi", "software", 300));
        employeeList.add(new Employee(4, "rajesh", "hardware", 400));
        

        Map<Integer, Employee> collect1 = employeeList.stream()
                .collect(Collectors.toMap(Employee::id, Function.identity()));

        // convert to DTO
        List<Employee> list4 = employeeList.stream()
                .map(e -> new Employee(e.id(), e.name(), e.department(), e.salary()))
                .toList();

        List<Employee> list6 = employeeList.stream()
                .sorted(Comparator.comparing(Employee::name)).toList();

        Map<Integer, Map<String, List<Employee>>> collect6 = employeeList.stream()
                .collect(Collectors.groupingBy(Employee::id, Collectors.groupingBy(Employee::department)));

        // count words in a sentence
        String countWords = "i am a sentence";
        System.out.println(countWords.split(" ").length);

        //FlatMap Example
        List<Integer> list7 = result.stream().flatMap(Collection::stream).toList();
        System.out.println(list7);

        boolean b = list7.stream().anyMatch(x -> x > 10);

        boolean a = list7.stream().anyMatch(x -> x > 8);

        boolean b1 = list7.stream().noneMatch(x -> x > 8);

        boolean b2 = list7.stream().allMatch(x -> x > 8);

        boolean b3 = list7.stream().allMatch(x -> x < 10);

        list7.stream().reduce(0, Integer::sum);

        OptionalDouble average = list7.stream()
                .mapToInt(Integer::intValue)
                .average();

        list7.stream().sorted(Comparator.reverseOrder()).skip(1).findFirst()
                .ifPresent(System.out::println);


        // not null
        List<Integer> mylist = Arrays.asList(1, null, 2, 3);
        List<Integer> list10 = mylist.stream().filter(Objects::nonNull).toList();


        // Option 1 - Virtual Thread with lot of bailor plate code

        List<Employee> employeeDTOList = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(employeeList.size());

        employeeList.forEach(emp -> {
            Thread t = Thread.ofVirtual().start(() -> {
                try {
                    employeeDTOList.add(new Employee(emp.id(), emp.name(), emp.department(), emp.salary() + 10));
                } finally {
                    latch.countDown(); // decrement count after each thread finishes
                }
            });
        });

        latch.await(); // main thread waits here until count reaches 0
        employeeDTOList.forEach(System.out::println);


        // Option 2 -> Virtual Thread and best
        List<Employee> employeeDTOList2 = new CopyOnWriteArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            employeeList.forEach(emp ->
                    executor.submit(() -> employeeDTOList2.add(new Employee(emp.id(), emp.name(), emp.department(), emp.salary() + 20)))
            );
        } // auto waits for all tasks to complete
        employeeDTOList2.forEach(System.out::println);
     }
    }

    record Employee (int id, String name, String department, int salary){}