import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        // eliminate duplicate strings from list

        List<String> stringList = new ArrayList<String>();
        stringList.add("kiyan");
        stringList.add("kumar");
        stringList.add("ravi");
        stringList.add("ravi");
        stringList.add("Kiyandoor");
        stringList.add("kk");

        System.out.println("Given List :" + stringList);

        System.out.println("Distinct element : " + stringList.stream().distinct().toList());

        // sort a list of String by length
        List<String> list = stringList.stream().sorted(Comparator.comparingInt(String::length)).toList();
        System.out.println("Sorted By length from lowest to highest : " + list);

        // sort a list of String by length - by descending order
        List<String> list1 = stringList.stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
        System.out.println("Sorted By length from  highest to lowest : " + list1);

        // join list with comma / csv
        String collect5 = stringList.stream().collect(Collectors.joining(","));
        System.out.println("Joining list with comma : " + collect5);

        // or

        String join = String.join(",", stringList);

        // duplicate elements in string.
        List<String> list5 = stringList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList();
        System.out.println("non-unique elements from list : " + list5);


        List<String> list11 = stringList.stream().sorted().toList();
        System.out.println("sorted List : " + list11);

        // sort and get 3
        List<String> list9 = stringList.stream().sorted().limit(3).toList();
        System.out.println("First 3 highest from list : " + list9);

        // sort by reverse and get 3
        List<String> list8 = stringList.stream().sorted(Comparator.reverseOrder()).limit(3).toList();
        System.out.println("Last 3 highest from list : " + list8);



        // char frequency in a String
        String name = "Kiyandoor";
        Map<Character, Long> collect = name.chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting()
        ));
        System.out.println("Frequency of char in String : " + collect);


        // convert to upper case
        List<String> list3 = stringList.stream().map(String::toUpperCase).toList();
        System.out.println("uppercase list : " +list3);

        //extract even numbers from string
        List<Integer> allNumber = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Integer> list2 = allNumber.stream().filter(x -> x % 2 == 0).toList();
        System.out.println("Event numbers from list :" + list2);


        String forNonRepeatedChar = "kkiyiandoor";
        // Optimized — LinkedHashMap preserves insertion order
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


        System.out.println("first Non Repeated Char from string \"kkiyiandoor\" : " + s);

        // true and false
        Map<Boolean, List<Integer>> collect2 = allNumber.stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0));
        System.out.println(collect2);

        // even and add
        Map<String, List<Integer>> collect3 = allNumber.stream()
                .collect(Collectors.groupingBy(x -> x % 2 == 0 ? "even" : "odd"));

        System.out.println(collect3);

        //list of list
        List<List<Integer>> result = allNumber.stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0))
                .values()
                .stream()
                .toList();

        System.out.println(result);

        //stats
        IntSummaryStatistics stats = allNumber.stream().mapToInt(Integer::intValue).summaryStatistics();
        System.out.println(stats.getMin());
        System.out.println(stats.getMax());
        System.out.println(stats.getAverage());

        // list of strings - group by first letter
        Map<String, List<String>> collect4 = stringList.stream().collect(Collectors.groupingBy(k -> k.substring(0, 1)));
        System.out.println(collect4);

        // list to map
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee(1, "kiyan", "it", 100));
        employeeList.add(new Employee(2, "sudha", "hardware", 200));
        employeeList.add(new Employee(3, "ravi", "software", 300));
        employeeList.add(new Employee(4, "rajesh", "hardware", 400));
        Map<Integer, Employee> collect1 = employeeList.stream()
                .collect(Collectors.toMap(Employee::id, Function.identity()));
        System.out.println(collect1);

        // convert to DTO
        List<Employee> list4 = employeeList.stream()
                .map(e -> new Employee(e.id(), e.name(), e.department(), e.salary()))
                .toList();
        System.out.println(list4);




        List<Employee> list6 = employeeList.stream().sorted(Comparator.comparing(Employee::name)).toList();
        System.out.println(list6);

        // count words in a sentence
        String countWords = "i am a sentence";
        System.out.println(countWords.split(" ").length);


        //FlatMap Example
        List<Integer> list7 = result.stream().flatMap(Collection::stream).toList();
        System.out.println(list7);



        boolean b = list7.stream().anyMatch(x -> x > 10);
        System.out.println(b);
        boolean a = list7.stream().anyMatch(x -> x > 8);
        System.out.println(a);

        boolean b1 = list7.stream().noneMatch(x -> x > 8);
        System.out.println(b1);

        boolean b2 = list7.stream().allMatch(x -> x > 8);
        System.out.println(b2);

        boolean b3 = list7.stream().allMatch(x -> x < 10);
        System.out.println(b3);


        Integer reduceSum = list7.stream().reduce(0, Integer::sum);
        System.out.println(reduceSum);
        System.out.println("sum : " + reduceSum);

        OptionalDouble average = list7.stream()
                .mapToInt(Integer::intValue)
                .average();
        System.out.println("Average : " + average);

        Map<String, Map<Integer, List<Employee>>> collect6 = employeeList.stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.groupingBy(Employee::id)));

        System.out.println(collect6);

        // find second largest
        list7.stream().sorted(Comparator.reverseOrder()).skip(1).findFirst().ifPresent(System.out::println);

        // not null
        List<Integer> mylist = Arrays.asList(1, null, 2, 3);
        List<Integer> list10 = mylist.stream().filter(Objects::nonNull).toList();
        System.out.println(list10);

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

        List<Employee> employeeDTOList2 = new CopyOnWriteArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            employeeList.forEach(emp ->
                    executor.submit(() -> employeeDTOList2.add(new Employee(emp.id(), emp.name(), emp.department(), emp.salary() + 20)))
            );
        } // auto waits for all tasks to complete
        employeeDTOList2.forEach(System.out::println);


        Map<String, List<Employee>> collect7 = employeeList.stream().filter(k -> k.salary() > 199).collect(Collectors.groupingBy(Employee::department));

        System.out.println(collect7);

        Map<String, Long> collect8 = employeeList.stream().filter(k -> k.salary() > 199).collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
        System.out.println(collect8);

        Map<String, List<Employee>> collect9 = employeeList.stream().collect(Collectors.groupingBy(Employee::name));
        System.out.println(collect9);


        IntStream.rangeClosed(2, 100)
                .filter(n -> IntStream.rangeClosed(2, (int) Math.sqrt(n)).noneMatch(i -> n % i == 0)).boxed().toList().forEach(System.out::println);



        IntStream.rangeClosed(2, 100)
                .filter(n -> IntStream.rangeClosed(2, (int) Math.sqrt(n))
                        .noneMatch(i -> n % i == 0))
                .forEach(System.out::println);

        // carrier Threads
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(Main::getEvenNumber);
        executor.submit(Main::getOddNumbers);
        executor.shutdown();


        // virtual Threads
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            executorService.submit(Main::getEvenNumber);
            executorService.submit(Main::getOddNumbers);
            executorService.shutdown();
        }

        CompletableFuture.runAsync(Main::getEvenNumber).thenRun(Main::getOddNumbers).join();

    }

    private static void getOddNumbers() {
        for (int i = 1; i <= 100; i++) {
            if (i % 2 != 0) {
              //  System.out.println(i + "=>" + Thread.currentThread().getName());
            }
        }
    }

    private static void getEvenNumber() {
        for (int i = 1; i <= 100; i++) {
            if (i % 2 == 0) {
            //    System.out.println(i + "=>" + Thread.currentThread().getName());
            }
        }
    }
}


    record Employee (int id, String name, String department, int salary){}
