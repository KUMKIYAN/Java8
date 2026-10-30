import java.util.concurrent.CompletableFuture;

public class CompletableFutureExample {

    public static void main(String[] args)
            throws Exception {

        // ── 1. Simple async task ✅ ───────────────────────────
        CompletableFuture<Integer> future =
            CompletableFuture.supplyAsync(() -> {
                System.out.println("Task running: "
                    + Thread.currentThread().getName());
                return 10; // ✅
            });

        // block and get result ✅
        Integer result = future.get();
        System.out.println("Result: " + result); // 10

        // ── 2. Chain operations ✅ ────────────────────────────
        CompletableFuture<Integer> chained =
            CompletableFuture.supplyAsync(() -> 10)
                .thenApply(n -> n * 2)   // 20 ✅
                .thenApply(n -> n + 5);  // 25 ✅

        System.out.println("Chained: " + chained.get()); // 25

        // ── 3. Two tasks parallel ✅ ──────────────────────────
        CompletableFuture<Integer> task1 =
            CompletableFuture.supplyAsync(() -> 10);

        CompletableFuture<Integer> task2 =
            CompletableFuture.supplyAsync(() -> 20);

        // combine both results ✅
        CompletableFuture<Integer> combined =
            task1.thenCombine(task2,
                (t1, t2) -> t1 + t2); // 30 ✅

        System.out.println("Combined: " + combined.get()); // 30

         //       or

        CompletableFuture<Integer> hdfcAccountBalance = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("HDFC account balance");
            return 100;
        });

        CompletableFuture<Integer> sbiAccountBalance = CompletableFuture.supplyAsync(() -> {
            System.out.println("SBI account balance");
            return 500;
        });

        hdfcAccountBalance.thenCombine(sbiAccountBalance, (hdfc, sbi) -> {
            System.out.println("total balance " + hdfc + " + " + sbi);
            return hdfc + sbi;
        }).thenAccept(System.out::println);


    /* 
    output
    SBI account balance
    HDFC account balance
    total balance 100 + 500
    600
    */

        // ── 4. Exception handling ✅ ──────────────────────────
        CompletableFuture<String> makePayment = CompletableFuture.supplyAsync(() -> {
            System.out.println("payment processing started ....");
            if (true) {
                throw new RuntimeException("error while processing payment ....");
            }
            return "PAYMENT_SUCCESS";
        }).exceptionally(ex ->
                {
                    System.out.println(ex.getMessage());
                    return "PAYMENT_PENDING";
                }
        );
        System.out.println(makePayment.get());

        /*
            payment processing started ....
            java.lang.RuntimeException: error while processing payment ....
            PAYMENT_PENDING
        */

        // ── 5. Wait for ALL tasks ✅ ──────────────────────────
        CompletableFuture<Integer> f1 =
            CompletableFuture.supplyAsync(() -> 1);
        CompletableFuture<Integer> f2 =
            CompletableFuture.supplyAsync(() -> 2);
        CompletableFuture<Integer> f3 =
            CompletableFuture.supplyAsync(() -> 3);

        CompletableFuture.allOf(f1, f2, f3).join(); // wait all ✅

        int sum = f1.get() + f2.get() + f3.get();
        System.out.println("Sum: " + sum); // 6 ✅
    }

    // --- 6th chaining Completable Future ( when we have dependancy then we will go for this.

    CompletableFuture<String> result =
                CompletableFuture.supplyAsync(() -> {
                            return "101";  // Get user ID
                        })
                        .thenCompose(userId -> {
                            return CompletableFuture.supplyAsync(() -> {
                                return "User Name for ID: " + userId;
                            });
                        });

    System.out.println(result.join());  // User Name for ID: 101

/*
    thenApply()	                                                thenCompose()
    Transforms the result	                                    Chains another asynchronous task
    Returns a normal value	                                    Returns a CompletableFuture
    Can create nested futures if the function returns a future	Flattens nested futures

*/



}


