# Sorting Algorithms in Java — Complete Guide
---
Bubble Sort — swap adjacent pairs

Selection Sort — find min each pass

Insertion Sort — build sorted one by one

Merge Sort — divide + conquer + merge

Quick Sort — pivot + partition

Heap Sort — max heap extraction

Counting Sort — count occurrences

Radix Sort — digit by digit

Shell Sort — gap-based insertion

Tim Sort — Java default hybrid

---
---
## Quick Reference

| Algorithm | Best | Average | Worst | Space | Stable |
|---|---|---|---|---|---|
| Bubble Sort | O(n) | O(n²) | O(n²) | O(1) | ✅ Yes |
| Selection Sort | O(n²) | O(n²) | O(n²) | O(1) | ❌ No |
| Insertion Sort | O(n) | O(n²) | O(n²) | O(1) | ✅ Yes |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) | ✅ Yes |
| Quick Sort | O(n log n) | O(n log n) | O(n²) | O(log n) | ❌ No |
| Heap Sort | O(n log n) | O(n log n) | O(n log n) | O(1) | ❌ No |
| Counting Sort | O(n+k) | O(n+k) | O(n+k) | O(k) | ✅ Yes |
| Radix Sort | O(nk) | O(nk) | O(nk) | O(n+k) | ✅ Yes |
| Shell Sort | O(n log n) | O(n log²n) | O(n²) | O(1) | ❌ No |
| Tim Sort | O(n) | O(n log n) | O(n log n) | O(n) | ✅ Yes |

---

## 1. Bubble Sort

```
What-    repeatedly swap adjacent elements if in wrong order ✅
How-     compare pair by pair | largest bubbles to end ✅
Best-    O(n) already sorted | Worst O(n²) ✅
Use-     simple | educational | small arrays ✅
```

```java
public class BubbleSort {

    public static void bubbleSort(int[] arr) {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {

            boolean swapped = false; // optimization ✅

            for (int j = 0; j < n - i - 1; j++) {
                // swap if current > next ✅
                if (arr[j] > arr[j + 1]) {
                    int temp  = arr[j];
                    arr[j]    = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped   = true;
                }
            }
            // if no swap → already sorted → stop ✅
            if (!swapped) break;
        }
    }

    public static void main(String[] args) {
        int[] arr = {64, 34, 25, 12, 22, 11, 90};
        bubbleSort(arr);
        // Output: [11, 12, 22, 25, 34, 64, 90] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example:
[64, 34, 25, 12]
Pass 1: [34, 25, 12, 64] → 64 bubbles to end ✅
Pass 2: [25, 12, 34, 64] → 34 bubbles ✅
Pass 3: [12, 25, 34, 64] → done ✅
```

---

## 2. Selection Sort

```
What-    find minimum in unsorted part → place at beginning ✅
How-     scan entire array | pick minimum | swap with first ✅
Best-    O(n²) always same regardless ✅
Use-     simple | small arrays | minimizes swaps ✅
```

```java
public class SelectionSort {

    public static void selectionSort(int[] arr) {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            // find minimum in unsorted part ✅
            int minIdx = i;

            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            // swap minimum with first unsorted ✅
            int temp     = arr[minIdx];
            arr[minIdx]  = arr[i];
            arr[i]       = temp;
        }
    }

    public static void main(String[] args) {
        int[] arr = {64, 25, 12, 22, 11};
        selectionSort(arr);
        // Output: [11, 12, 22, 25, 64] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example:
[64, 25, 12, 22, 11]
Pass 1: min=11 → swap with 64 → [11, 25, 12, 22, 64] ✅
Pass 2: min=12 → swap with 25 → [11, 12, 25, 22, 64] ✅
Pass 3: min=22 → swap with 25 → [11, 12, 22, 25, 64] ✅
```

---

## 3. Insertion Sort

```
What-    build sorted array one element at a time ✅
How-     pick element | insert into correct position in sorted part ✅
Best-    O(n) nearly sorted | Worst O(n²) ✅
Use-     small arrays | nearly sorted | online sorting ✅
```

```java
public class InsertionSort {

    public static void insertionSort(int[] arr) {
        int n = arr.length;

        for (int i = 1; i < n; i++) {
            int key = arr[i]; // element to insert ✅
            int j   = i - 1;

            // shift larger elements right ✅
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            // insert at correct position ✅
            arr[j + 1] = key;
        }
    }

    public static void main(String[] args) {
        int[] arr = {12, 11, 13, 5, 6};
        insertionSort(arr);
        // Output: [5, 6, 11, 12, 13] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example:
[12, 11, 13, 5, 6]
i=1: key=11 | 12>11 shift | [11, 12, 13, 5, 6] ✅
i=2: key=13 | no shift     | [11, 12, 13, 5, 6] ✅
i=3: key=5  | shift 3      | [5, 11, 12, 13, 6] ✅
i=4: key=6  | shift 2      | [5, 6, 11, 12, 13] ✅
```

---

## 4. Merge Sort

```
What-    divide array in half | sort each half | merge ✅
How-     recursively divide | conquer | merge sorted halves ✅
Best-    O(n log n) always | extra space O(n) ✅
Use-     large arrays | linked lists | stable sort needed ✅
```

```java
public class MergeSort {

    public static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;

            // sort left half ✅
            mergeSort(arr, left, mid);
            // sort right half ✅
            mergeSort(arr, mid + 1, right);
            // merge sorted halves ✅
            merge(arr, left, mid, right);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        // temp arrays ✅
        int[] L = new int[n1];
        int[] R = new int[n2];

        System.arraycopy(arr, left,     L, 0, n1);
        System.arraycopy(arr, mid + 1,  R, 0, n2);

        int i = 0, j = 0, k = left;

        // merge back ✅
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }
        // copy remaining ✅
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    public static void main(String[] args) {
        int[] arr = {38, 27, 43, 3, 9, 82, 10};
        mergeSort(arr, 0, arr.length - 1);
        // Output: [3, 9, 10, 27, 38, 43, 82] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example:
[38, 27, 43, 3]
divide: [38, 27] | [43, 3]
divide: [38] [27] | [43] [3]
merge:  [27, 38] | [3, 43]
merge:  [3, 27, 38, 43] ✅
```

---

## 5. Quick Sort

```
What-    pick pivot | partition around pivot | recurse ✅
How-     elements < pivot go left | > pivot go right | repeat ✅
Best-    O(n log n) | Worst O(n²) sorted array + bad pivot ✅
Use-     fastest in practice | large arrays | in-place ✅
```

```java
public class QuickSort {

    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // partition and get pivot index ✅
            int pivotIdx = partition(arr, low, high);

            // sort left of pivot ✅
            quickSort(arr, low, pivotIdx - 1);
            // sort right of pivot ✅
            quickSort(arr, pivotIdx + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high]; // last element as pivot ✅
        int i     = low - 1;  // smaller element index

        for (int j = low; j < high; j++) {
            // if current <= pivot move to left ✅
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i]   = arr[j];
                arr[j]   = temp;
            }
        }
        // place pivot in correct position ✅
        int temp    = arr[i + 1];
        arr[i + 1]  = arr[high];
        arr[high]   = temp;

        return i + 1;
    }

    public static void main(String[] args) {
        int[] arr = {10, 7, 8, 9, 1, 5};
        quickSort(arr, 0, arr.length - 1);
        // Output: [1, 5, 7, 8, 9, 10] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example:
[10, 7, 8, 9, 1, 5] pivot=5
after partition: [1, 5, 8, 9, 7, 10]
                      ↑ pivot in place ✅
recurse left [1] | recurse right [8,9,7,10]
```

---

## 6. Heap Sort

```
What-    build max heap | extract max repeatedly ✅
How-     heapify → max at root | swap root with last | reduce heap ✅
Best-    O(n log n) always | in-place O(1) space ✅
Use-     guaranteed O(n log n) | when space is concern ✅
```

```java
public class HeapSort {

    public static void heapSort(int[] arr) {
        int n = arr.length;

        // build max heap ✅
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        // extract elements from heap ✅
        for (int i = n - 1; i > 0; i--) {
            // move root (max) to end ✅
            int temp = arr[0];
            arr[0]   = arr[i];
            arr[i]   = temp;

            // heapify reduced heap ✅
            heapify(arr, i, 0);
        }
    }

    private static void heapify(int[] arr, int n, int i) {
        int largest = i;       // root
        int left    = 2*i + 1; // left child
        int right   = 2*i + 2; // right child

        if (left < n && arr[left] > arr[largest])
            largest = left;

        if (right < n && arr[right] > arr[largest])
            largest = right;

        // if largest is not root → swap + recurse ✅
        if (largest != i) {
            int temp    = arr[i];
            arr[i]      = arr[largest];
            arr[largest] = temp;

            heapify(arr, n, largest);
        }
    }

    public static void main(String[] args) {
        int[] arr = {12, 11, 13, 5, 6, 7};
        heapSort(arr);
        // Output: [5, 6, 7, 11, 12, 13] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Max Heap:
        13
       /  \
      12    7
     / \   /
    5   6  11

extract 13 → swap with last → heapify → repeat ✅
```

---

## 7. Counting Sort

```
What-    count occurrences | calculate positions | place elements ✅
How-     count[] | cumulative count | build output ✅
Best-    O(n+k) k=range | NOT comparison based ✅
Use-     integers | small range | no negatives ✅
```

```java
public class CountingSort {

    public static void countingSort(int[] arr) {
        int n   = arr.length;
        int max = java.util.Arrays.stream(arr).max().getAsInt();

        // count occurrences ✅
        int[] count = new int[max + 1];
        for (int num : arr) count[num]++;

        // cumulative count ✅
        for (int i = 1; i <= max; i++) {
            count[i] += count[i - 1];
        }

        // build output ✅
        int[] output = new int[n];
        for (int i = n - 1; i >= 0; i--) {
            output[count[arr[i]] - 1] = arr[i];
            count[arr[i]]--;
        }

        System.arraycopy(output, 0, arr, 0, n);
    }

    public static void main(String[] args) {
        int[] arr = {4, 2, 2, 8, 3, 3, 1};
        countingSort(arr);
        // Output: [1, 2, 2, 3, 3, 4, 8] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example: [4, 2, 2, 8, 3, 3, 1]
count:  [0, 1, 2, 2, 1, 0, 0, 0, 1]
         0  1  2  3  4  5  6  7  8
cumulative: [0,1,3,5,6,6,6,6,7]
place in output ✅
```

---

## 8. Radix Sort

```
What-    sort digit by digit from least to most significant ✅
How-     use counting sort for each digit position ✅
Best-    O(nk) k=number of digits | stable ✅
Use-     large integers | fixed length strings ✅
```

```java
public class RadixSort {

    public static void radixSort(int[] arr) {
        int max = java.util.Arrays.stream(arr).max().getAsInt();

        // sort by each digit position ✅
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countSortByDigit(arr, exp);
        }
    }

    private static void countSortByDigit(int[] arr, int exp) {
        int n      = arr.length;
        int[] output = new int[n];
        int[] count  = new int[10];

        // count digits ✅
        for (int num : arr) {
            count[(num / exp) % 10]++;
        }

        // cumulative ✅
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        // build output right to left ✅
        for (int i = n - 1; i >= 0; i--) {
            int digit = (arr[i] / exp) % 10;
            output[count[digit] - 1] = arr[i];
            count[digit]--;
        }

        System.arraycopy(output, 0, arr, 0, n);
    }

    public static void main(String[] args) {
        int[] arr = {170, 45, 75, 90, 802, 24, 2, 66};
        radixSort(arr);
        // Output: [2, 24, 45, 66, 75, 90, 170, 802] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example: [170, 45, 75, 90, 802, 24, 2, 66]
Sort by 1s:  [170, 90, 802, 2, 24, 45, 75, 66]
Sort by 10s: [802, 2, 24, 45, 66, 170, 75, 90]
Sort by 100s:[2, 24, 45, 66, 75, 90, 170, 802] ✅
```

---

## 9. Shell Sort

```
What-    improved insertion sort | sort elements far apart first ✅
How-     use gap sequence | reduce gap | final pass=insertion sort ✅
Best-    O(n log n) | depends on gap sequence ✅
Use-     medium arrays | better than insertion sort ✅
```

```java
public class ShellSort {

    public static void shellSort(int[] arr) {
        int n = arr.length;

        // start with large gap → reduce to 1 ✅
        for (int gap = n / 2; gap > 0; gap /= 2) {

            // insertion sort with gap ✅
            for (int i = gap; i < n; i++) {
                int temp = arr[i];
                int j    = i;

                while (j >= gap && arr[j - gap] > temp) {
                    arr[j] = arr[j - gap];
                    j -= gap;
                }
                arr[j] = temp;
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {12, 34, 54, 2, 3};
        shellSort(arr);
        // Output: [2, 3, 12, 34, 54] ✅
        System.out.println(java.util.Arrays.toString(arr));
    }
}
```

```
Example: [12, 34, 54, 2, 3] n=5
gap=2: compare positions 0-2, 1-3, 2-4
       [12, 2, 3, 34, 54] ✅
gap=1: insertion sort → [2, 3, 12, 34, 54] ✅
```

---

## 10. Tim Sort (Java built-in)

```
What-    hybrid merge sort + insertion sort ✅
How-     divide into runs | sort each with insertion | merge runs ✅
Best-    O(n) already sorted | Worst O(n log n) ✅
Use-     Java default Arrays.sort() for objects ✅
        Collections.sort() uses TimSort ✅
```

```java
public class TimSort {

    static final int RUN = 32;

    // insertion sort for small runs ✅
    public static void insertionSort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int temp = arr[i];
            int j    = i - 1;
            while (j >= left && arr[j] > temp) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = temp;
        }
    }

    // merge two sorted runs ✅
    public static void merge(int[] arr, int l, int m, int r) {
        int len1 = m - l + 1;
        int len2 = r - m;
        int[] left  = new int[len1];
        int[] right = new int[len2];

        System.arraycopy(arr, l,     left,  0, len1);
        System.arraycopy(arr, m + 1, right, 0, len2);

        int i = 0, j = 0, k = l;
        while (i < len1 && j < len2) {
            if (left[i] <= right[j]) arr[k++] = left[i++];
            else                      arr[k++] = right[j++];
        }
        while (i < len1) arr[k++] = left[i++];
        while (j < len2) arr[k++] = right[j++];
    }

    public static void timSort(int[] arr) {
        int n = arr.length;

        // sort individual runs ✅
        for (int i = 0; i < n; i += RUN) {
            insertionSort(arr, i, Math.min(i + RUN - 1, n - 1));
        }

        // merge runs ✅
        for (int size = RUN; size < n; size = 2 * size) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid   = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);
                if (mid < right) merge(arr, left, mid, right);
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {5, 21, 7, 23, 19, 1, 3, 12};
        timSort(arr);
        // Output: [1, 3, 5, 7, 12, 19, 21, 23] ✅
        System.out.println(java.util.Arrays.toString(arr));

        // Java built-in uses TimSort ✅
        int[] arr2 = {5, 21, 7, 23, 19};
        java.util.Arrays.sort(arr2); // TimSort internally ✅
        System.out.println(java.util.Arrays.toString(arr2));
    }
}
```

---

## Java Built-in Sorting

```java
// primitive arrays — Dual-Pivot QuickSort ✅
int[] arr = {5, 3, 1, 4, 2};
Arrays.sort(arr);

// object arrays — TimSort ✅
Integer[] arr2 = {5, 3, 1, 4, 2};
Arrays.sort(arr2);

// partial sort ✅
Arrays.sort(arr, 1, 4); // sort index 1 to 3

// Collections ✅
List<Integer> list = new ArrayList<>(List.of(5,3,1,4,2));
Collections.sort(list); // TimSort ✅

// custom comparator ✅
Arrays.sort(arr2, Comparator.reverseOrder());
```

---

## When to use which

```
Bubble Sort-    educational only | never production ❌
Selection Sort- minimizes swaps | small arrays ✅
Insertion Sort- small arrays | nearly sorted | online ✅
Merge Sort-     large arrays | stable | linked lists ✅
Quick Sort-     fastest average | large arrays | in-place ✅
Heap Sort-      guaranteed O(n log n) | in-place ✅
Counting Sort-  integers | small range only ✅
Radix Sort-     large integers | fixed length ✅
Shell Sort-     medium arrays | better than insertion ✅
TimSort-        Java default | general purpose ✅
```

---

## Interview Quick Notes

```
Stable sort-    preserves relative order of equal elements ✅
               Bubble | Insertion | Merge | Counting | Radix | TimSort ✅
In-place-       O(1) extra space ✅
               Bubble | Selection | Insertion | Heap | Shell ✅
Divide+conquer- Merge Sort | Quick Sort ✅
Comparison-     most algorithms ✅
Non-comparison- Counting | Radix | Bucket (O(n) possible) ✅
Java default-   primitives=QuickSort | objects=TimSort ✅
Best for interview- Merge Sort + Quick Sort most asked ✅
```
