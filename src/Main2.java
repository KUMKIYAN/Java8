import java.util.*;
import java.util.stream.Collectors;
 record Account(String accountNumber, int customerId, double balance) {}
record Customer(int customerId, String customerName) {}
public class Main2 {
    static List<Customer> customers =
            List.of(
                    new Customer(1, "John"),
                    new Customer(2, "Mary"),
                    new Customer(3, "David"),
                    new Customer(4, "Sam"));
    static List<Account> accounts =
            List.of(
                    new Account("A101", 1, 1000),
                    new Account("A102", 1, 1500),
                    new Account("A103", 2, 2000),
                    new Account("A104", 2, 500),
                    new Account("A105", 2, 700),
                    new Account("A106", 3, 3000),
                    new Account("A107", 4, 1200),
                    new Account("A108", 4, 1200));

    // Task 1: Find total balance per customer.
    public static Map<Integer, Double> totalBalancePerCustomer(List<Account> accounts) {
        return accounts.stream()
                .collect(Collectors.groupingBy(Account::customerId, Collectors.summingDouble(Account::balance)));
    }

    // Task 6:   Find customer who has the highest balance.
    public static String highestBalanceCustomer(List<Customer> customers, List<Account> accounts) {
        Set<Map.Entry<Integer, Double>> allSalary = totalBalancePerCustomer(accounts).entrySet();
        int hightBalCustomer = 0;
        double balance = 0.0;
        for(Map.Entry<Integer, Double> e : allSalary){
                if(e.getValue() > balance){
                    balance = e.getValue();
                    hightBalCustomer = e.getKey();
                }
        }
        final int finalHighBalCustomer = hightBalCustomer;
        Customer highValueCustomer = customers
                .stream().filter(a -> a.customerId() == finalHighBalCustomer).findFirst().get();
        return highValueCustomer.customerName();
    }

    public static void main(String[] args) {
        System.out.println("========== TASK 1 ==========");
        Map<Integer, Double> balances = totalBalancePerCustomer(accounts);
        System.out.println(balances);
        String highBalanceCustomer = highestBalanceCustomer(customers, accounts);
        System.out.println(highBalanceCustomer);
        System.out.println("\n========== EXPECTED RESULTS ==========");
        System.out.println(
                """            
                    Task 1;
                    {1=2500.0, 2=3200.0, 3=3000.0, 4=2400.0}
                    Task 2:
                    [Mary, David]
                    Task 3:
                    A106
                    A103
                    A102
                    A107
                    A108
                    A101
                    A105
                    A104
                    Task 4:
                    8 unique account numbers
                    Task 5:
                    11100.0
                    Task 6:
                    Mary
                    """);
    }
 }