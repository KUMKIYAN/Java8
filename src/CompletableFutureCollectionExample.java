import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CompletableFutureCollectionExample {

    public static void main(String[] args)
            throws Exception {

        // ── 1. Fetch list of orders async ✅ ──────────────────
        CompletableFuture<List<String>> ordersFuture =
            CompletableFuture.supplyAsync(() -> {
                // simulate DB call ✅
                return List.of("ORD001", "ORD002", "ORD003");
            });

        List<String> orders = ordersFuture.get();
        System.out.println("Orders: " + orders);
        // [ORD001, ORD002, ORD003] ✅

        // ── 2. Process each order in parallel ✅ ──────────────
        List<String> orderIds =
                List.of("ORD001", "ORD002", "ORD003",
                        "ORD004", "ORD005");

        // create one future per order ✅
        List<CompletableFuture<String>> futures =
            orderIds.stream()
                    .map(id -> CompletableFuture
                        .supplyAsync(() -> processOrder(id))) // ✅
                    .collect(Collectors.toList());

        // wait for ALL futures ✅
        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

        System.out.println("Futures size:" + futures.size());

        // collect all results into List ✅
        List<String> results = allDone
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join) // ✅
                        .collect(Collectors.toList()))
                .get();

        System.out.println("Results: " + results);
        // [PROCESSED-ORD001, PROCESSED-ORD002...] ✅

        // ── 3. Fetch from multiple services parallel ✅ ───────
        CompletableFuture<List<String>> pendingOrders =
            CompletableFuture.supplyAsync(() ->
                List.of("ORD001", "ORD002")); // DB call ✅

        CompletableFuture<List<String>> completedOrders =
            CompletableFuture.supplyAsync(() ->
                List.of("ORD003", "ORD004")); // DB call ✅

        CompletableFuture<List<String>> cancelledOrders =
            CompletableFuture.supplyAsync(() ->
                List.of("ORD005")); // DB call ✅

        // combine all three lists ✅
        CompletableFuture<List<String>> allOrders =
            pendingOrders.thenCombine(completedOrders,
                (pending, completed) -> {
                    // merge two lists ✅
                    return new java.util.ArrayList<String>() {{
                        addAll(pending);
                        addAll(completed);
                    }};
                })
            .thenCombine(cancelledOrders,
                (merged, cancelled) -> {
                    merged.addAll(cancelled);
                    return merged; // final combined list ✅
                });

        System.out.println("All orders: " + allOrders.get());
        // [ORD001, ORD002, ORD003, ORD004, ORD005] ✅

        // ── 4. Return Map from async ✅ ────────────────────────
        CompletableFuture<Map<String, String>> orderMap =
            CompletableFuture.supplyAsync(() ->
                List.of("ORD001", "ORD002", "ORD003")
                    .stream()
                    .collect(Collectors.toMap(
                        id -> id,                         // key ✅
                        id -> "STATUS-" + id)));          // value ✅

        Map<String, String> statusMap = orderMap.get();
        System.out.println("Status map: " + statusMap);
        // {ORD001=STATUS-ORD001, ORD002=STATUS-ORD002} ✅

        // ── 5. Error handling with collection ✅ ──────────────
        CompletableFuture<Object> withError =
            CompletableFuture.supplyAsync(() -> {
                throw new RuntimeException("DB down!");
            })
            .exceptionally(ex -> {
                System.out.println("Error: " + ex.getMessage());
                return List.of(); // return empty list ✅
            });

        System.out.println("Safe result: " + withError.get());
        // [] ✅
    }

    // simulate order processing ✅
    private static String processOrder(String orderId) {
        return "PROCESSED-" + orderId; // ✅
    }
}

/*
 * Orders: [ORD001, ORD002, ORD003]
 * Results: [PROCESSED-ORD001, PROCESSED-ORD002,
 *           PROCESSED-ORD003, PROCESSED-ORD004,
 *           PROCESSED-ORD005]
 * All orders: [ORD001, ORD002, ORD003, ORD004, ORD005]
 * Status map: {ORD001=STATUS-ORD001 ...}
 * Safe result: []
 */

/*
List return-   supplyAsync(() -> List.of(...)) ✅
Parallel list- stream().map(supplyAsync).collect() ✅
allOf+join-    wait all + collect results ✅
thenCombine-   merge two lists ✅
Map return-    collect(Collectors.toMap()) ✅
Error list-    exceptionally → return empty list ✅
 */