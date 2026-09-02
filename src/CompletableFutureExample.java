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

        // ── 4. Exception handling ✅ ──────────────────────────
        CompletableFuture<Object> withError =
            CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("Failed!");
            })
            .exceptionally(ex -> {
                System.out.println("Error: " + ex.getMessage());
                return -1; // fallback ✅
            });

        System.out.println("Error result: " + withError.get()); // -1

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
}


