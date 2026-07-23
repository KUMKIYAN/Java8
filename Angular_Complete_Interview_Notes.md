# Angular — Complete Interview Notes
> Quick Reference for Interview & Development

---

## 1. What is Angular?

```
Angular = TypeScript-based frontend framework by Google (2016)
→ full framework (not just library like React)
→ opinionated — specific way to do things
→ built-in: routing, HTTP, forms, DI, testing
→ TypeScript first — strong typing ✅
→ component-based architecture
→ two-way data binding
→ MVC/MVVM pattern
```

---

## 2. Angular vs React vs Vue

| | Angular | React | Vue |
|---|---|---|---|
| **Type** | Full framework | Library | Progressive framework |
| **Language** | TypeScript | JavaScript/TypeScript | JavaScript/TypeScript |
| **Data binding** | Two-way | One-way | Two-way |
| **Learning curve** | Steep | Medium | Easy |
| **By** | Google | Meta/Facebook | Community |
| **CLI** | ✅ Angular CLI | Create React App/Vite | Vue CLI |
| **Size** | Large | Small | Small |
| **Best for** | Enterprise large apps | Flexible UI | Rapid development |

---

## 3. Angular Architecture

```
Angular App
├── Modules (NgModule)      ← organize app into blocks
│   ├── Components          ← UI building blocks (view + logic)
│   ├── Services            ← business logic + data
│   ├── Directives          ← DOM manipulation
│   ├── Pipes               ← transform data in template
│   └── Guards              ← protect routes
├── Routing                 ← navigation between views
└── HTTP Client             ← API calls
```

---

## 4. Setup

```bash
# install Angular CLI
npm install -g @angular/cli

# create new project
ng new my-app
cd my-app
ng serve           # run dev server http://localhost:4200

# generate components/services
ng generate component orders/order-list   # or ng g c
ng generate service services/order        # or ng g s
ng generate module orders                 # or ng g m
ng generate pipe pipes/currency-format    # or ng g p
ng generate guard guards/auth             # or ng g g

# build for production
ng build --configuration production
```

---

## 5. Project Structure

```
my-app/
├── src/
│   ├── app/
│   │   ├── app.module.ts         ← root module
│   │   ├── app.component.ts      ← root component
│   │   ├── app.component.html    ← root template
│   │   ├── app.component.css     ← root styles
│   │   ├── app-routing.module.ts ← routing config
│   │   ├── components/           ← UI components
│   │   ├── services/             ← business logic
│   │   ├── models/               ← TypeScript interfaces
│   │   ├── pipes/                ← custom pipes
│   │   └── guards/               ← route guards
│   ├── assets/                   ← images, fonts
│   ├── environments/             ← dev/prod config
│   │   ├── environment.ts        ← dev
│   │   └── environment.prod.ts   ← prod
│   ├── index.html
│   └── main.ts                   ← bootstrap app
├── angular.json                  ← Angular CLI config
├── tsconfig.json                 ← TypeScript config
└── package.json
```

---

## 6. Modules (NgModule)

```typescript
// app.module.ts — root module
import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { HttpClientModule } from "@angular/common/http";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { AppRoutingModule } from "./app-routing.module";

@NgModule({
    declarations: [           // components, directives, pipes
        AppComponent,
        OrderListComponent,
        OrderCardComponent,
    ],
    imports: [                // other modules
        BrowserModule,
        HttpClientModule,     // for HTTP calls
        FormsModule,          // template-driven forms
        ReactiveFormsModule,  // reactive forms
        AppRoutingModule,     // routing
    ],
    providers: [              // services
        OrderService,
        AuthGuard,
    ],
    bootstrap: [AppComponent] // root component
})
export class AppModule { }
```

### Feature Module
```typescript
// orders.module.ts — feature module
@NgModule({
    declarations: [
        OrderListComponent,
        OrderDetailComponent,
        OrderFormComponent,
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        OrdersRoutingModule,
    ],
    exports: [OrderListComponent] // make available to other modules
})
export class OrdersModule { }
```

---

## 7. Components

```typescript
// order-list.component.ts
import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from "@angular/core";

@Component({
    selector:    "app-order-list",       // HTML tag: <app-order-list>
    templateUrl: "./order-list.component.html",
    styleUrls:   ["./order-list.component.css"],
    // OR inline:
    // template: `<div>{{title}}</div>`,
    // styles: [`h1 { color: red; }`]
})
export class OrderListComponent implements OnInit, OnDestroy {

    // Input — receive data from parent
    @Input() title: string = "Orders";
    @Input() pageSize: number = 10;

    // Output — send events to parent
    @Output() orderSelected = new EventEmitter<Order>();
    @Output() orderDeleted  = new EventEmitter<number>();

    orders: Order[] = [];
    loading = false;
    error: string | null = null;

    // inject service via constructor (Dependency Injection)
    constructor(private orderService: OrderService) {}

    // lifecycle hooks
    ngOnInit(): void {
        this.loadOrders();  // called once after component init ✅
    }

    ngOnDestroy(): void {
        // cleanup subscriptions ✅
    }

    loadOrders(): void {
        this.loading = true;
        this.orderService.getOrders().subscribe({
            next:  (orders) => { this.orders = orders; this.loading = false; },
            error: (err)    => { this.error = err.message; this.loading = false; }
        });
    }

    selectOrder(order: Order): void {
        this.orderSelected.emit(order); // emit event to parent ✅
    }

    deleteOrder(id: number): void {
        this.orderDeleted.emit(id);
    }
}
```

### Template
```html
<!-- order-list.component.html -->
<div class="order-list">
    <h2>{{ title }}</h2>                           <!-- interpolation -->

    <div *ngIf="loading">Loading...</div>          <!-- conditional -->
    <div *ngIf="error">{{ error }}</div>

    <ul>
        <li *ngFor="let order of orders; let i = index; trackBy: trackById">
            <!-- ngFor with index and trackBy ✅ -->
            <app-order-card
                [order]="order"                    <!-- property binding -->
                [index]="i"
                (orderSelected)="selectOrder($event)" <!-- event binding -->
            ></app-order-card>
        </li>
    </ul>
</div>
```

---

## 8. Data Binding

```html
<!-- 1. Interpolation — display data -->
<h1>{{ title }}</h1>
<p>{{ order.amount | currency }}</p>

<!-- 2. Property Binding — bind to DOM property -->
<input [value]="name" />
<img [src]="imageUrl" [alt]="title" />
<button [disabled]="isLoading">Submit</button>
<div [class.active]="isActive"></div>
<div [style.color]="textColor"></div>

<!-- 3. Event Binding — handle DOM events -->
<button (click)="handleClick()">Click</button>
<input (input)="handleInput($event)" />
<form (submit)="handleSubmit($event)">

<!-- 4. Two-Way Binding — [(ngModel)] -->
<!-- requires FormsModule -->
<input [(ngModel)]="searchTerm" />
<!-- same as: -->
<input [value]="searchTerm" (input)="searchTerm = $event.target.value" />

<!-- 5. Attribute Binding -->
<td [attr.colspan]="columnSpan">
<div [attr.aria-label]="label">
```

---

## 9. Directives

### Structural Directives (change DOM structure)
```html
<!-- *ngIf — show/hide element -->
<div *ngIf="isLoggedIn">Welcome!</div>
<div *ngIf="isLoggedIn; else loginBlock">Welcome!</div>
<ng-template #loginBlock><p>Please login</p></ng-template>

<!-- *ngFor — repeat element -->
<li *ngFor="let item of items; 
            let i = index; 
            let first = first; 
            let last = last;
            trackBy: trackById">
    {{ i }}: {{ item.name }}
</li>

<!-- *ngSwitch -->
<div [ngSwitch]="status">
    <p *ngSwitchCase="'PAID'">Paid ✅</p>
    <p *ngSwitchCase="'PENDING'">Pending ⏳</p>
    <p *ngSwitchDefault>Unknown</p>
</div>
```

### Attribute Directives (change appearance/behavior)
```html
<!-- ngClass — conditional classes -->
<div [ngClass]="{ 'active': isActive, 'error': hasError }">
<div [ngClass]="getClasses()">

<!-- ngStyle — conditional styles -->
<div [ngStyle]="{ 'color': textColor, 'font-size': fontSize + 'px' }">
```

### Custom Directive
```typescript
@Directive({
    selector: "[appHighlight]"  // attribute selector
})
export class HighlightDirective {

    @Input() appHighlight = "yellow";

    constructor(private el: ElementRef,
                private renderer: Renderer2) {}

    @HostListener("mouseenter")
    onMouseEnter() {
        this.renderer.setStyle(
            this.el.nativeElement, "backgroundColor", this.appHighlight
        );
    }

    @HostListener("mouseleave")
    onMouseLeave() {
        this.renderer.removeStyle(this.el.nativeElement, "backgroundColor");
    }
}

// usage: <p appHighlight="lightblue">Hover me</p>
```

---

## 10. Pipes

```html
<!-- Built-in pipes -->
{{ name | uppercase }}                    <!-- JOHN -->
{{ name | lowercase }}                    <!-- john -->
{{ amount | currency:"USD" }}             <!-- $100.00 -->
{{ amount | currency:"GBP":"symbol" }}    <!-- £100.00 -->
{{ 3.14159 | number:"1.2-2" }}           <!-- 3.14 -->
{{ date | date:"dd/MM/yyyy" }}            <!-- 15/01/2024 -->
{{ date | date:"medium" }}               <!-- Jan 15, 2024, 10:30:00 AM -->
{{ longText | slice:0:50 }}              <!-- first 50 chars -->
{{ items | json }}                        <!-- JSON string (debug) -->
{{ items | async }}                       <!-- unwrap Observable/Promise -->
{{ 0.25 | percent }}                      <!-- 25% -->
```

### Custom Pipe
```typescript
@Pipe({ name: "truncate" })
export class TruncatePipe implements PipeTransform {

    transform(value: string, limit: number = 50, suffix: string = "..."): string {
        if (!value) return "";
        return value.length > limit
            ? value.substring(0, limit) + suffix
            : value;
    }
}

// usage: {{ description | truncate:100:"..." }}
```

---

## 11. Services and Dependency Injection

```typescript
// order.service.ts
import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError, map, tap } from "rxjs/operators";

@Injectable({
    providedIn: "root"  // singleton — available throughout app ✅
})
export class OrderService {

    private apiUrl = "https://api.example.com/orders";

    constructor(private http: HttpClient) {}

    // GET all orders
    getOrders(): Observable<Order[]> {
        return this.http.get<Order[]>(this.apiUrl).pipe(
            tap(orders => console.log("Fetched orders:", orders.length)),
            catchError(this.handleError)
        );
    }

    // GET with query params
    searchOrders(status: string, page: number): Observable<Order[]> {
        const params = new HttpParams()
            .set("status", status)
            .set("page", page.toString());

        return this.http.get<Order[]>(this.apiUrl, { params });
    }

    // GET single order
    getOrder(id: number): Observable<Order> {
        return this.http.get<Order>(`${this.apiUrl}/${id}`);
    }

    // POST — create
    createOrder(order: Partial<Order>): Observable<Order> {
        return this.http.post<Order>(this.apiUrl, order);
    }

    // PUT — full update
    updateOrder(id: number, order: Order): Observable<Order> {
        return this.http.put<Order>(`${this.apiUrl}/${id}`, order);
    }

    // PATCH — partial update
    updateStatus(id: number, status: string): Observable<Order> {
        return this.http.patch<Order>(`${this.apiUrl}/${id}`, { status });
    }

    // DELETE
    deleteOrder(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    private handleError(error: any): Observable<never> {
        console.error("API Error:", error);
        return throwError(() => new Error(error.message));
    }
}
```

---

## 12. HTTP Interceptors

```typescript
// auth.interceptor.ts — add JWT to every request
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private authService: AuthService) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.authService.getToken();

        if (token) {
            // clone request and add Authorization header
            const authReq = req.clone({
                headers: req.headers.set("Authorization", `Bearer ${token}`)
            });
            return next.handle(authReq);
        }
        return next.handle(req);
    }
}

// loading.interceptor.ts — show/hide loading spinner
@Injectable()
export class LoadingInterceptor implements HttpInterceptor {

    constructor(private loadingService: LoadingService) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        this.loadingService.show();
        return next.handle(req).pipe(
            finalize(() => this.loadingService.hide())
        );
    }
}

// register in app.module.ts
providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor,    multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true }
]
```

---

## 13. Reactive Forms

```typescript
// order-form.component.ts
import { FormBuilder, FormGroup, Validators, AbstractControl } from "@angular/forms";

@Component({
    selector: "app-order-form",
    templateUrl: "./order-form.component.html"
})
export class OrderFormComponent implements OnInit {

    orderForm!: FormGroup;

    constructor(private fb: FormBuilder,
                private orderService: OrderService) {}

    ngOnInit(): void {
        this.orderForm = this.fb.group({
            customerId: ["", [Validators.required]],
            amount:     [0,  [Validators.required, Validators.min(1)]],
            status:     ["PENDING", Validators.required],
            email:      ["", [Validators.required, Validators.email]],
            address: this.fb.group({           // nested form group
                street: ["", Validators.required],
                city:   ["", Validators.required],
                zip:    ["", [Validators.required, Validators.pattern(/^\d{5}$/)]]
            }),
            items: this.fb.array([])           // form array
        });
    }

    // getters for easy template access
    get customerId() { return this.orderForm.get("customerId")!; }
    get amount()     { return this.orderForm.get("amount")!; }
    get email()      { return this.orderForm.get("email")!; }
    get items()      { return this.orderForm.get("items") as FormArray; }

    addItem(): void {
        this.items.push(this.fb.group({
            name:     ["", Validators.required],
            quantity: [1,  Validators.min(1)]
        }));
    }

    removeItem(index: number): void {
        this.items.removeAt(index);
    }

    onSubmit(): void {
        if (this.orderForm.invalid) {
            this.orderForm.markAllAsTouched(); // show all errors ✅
            return;
        }
        this.orderService.createOrder(this.orderForm.value).subscribe({
            next:  () => console.log("Order created"),
            error: (err) => console.error(err)
        });
    }

    onReset(): void {
        this.orderForm.reset();
    }
}
```

```html
<!-- order-form.component.html -->
<form [formGroup]="orderForm" (ngSubmit)="onSubmit()">

    <input formControlName="customerId" type="text" />
    <div *ngIf="customerId.invalid && customerId.touched">
        <span *ngIf="customerId.errors?.['required']">Required</span>
    </div>

    <input formControlName="amount" type="number" />
    <div *ngIf="amount.invalid && amount.touched">
        <span *ngIf="amount.errors?.['min']">Must be > 0</span>
    </div>

    <input formControlName="email" type="email" />
    <div *ngIf="email.invalid && email.touched">
        <span *ngIf="email.errors?.['email']">Invalid email</span>
    </div>

    <!-- nested group -->
    <div formGroupName="address">
        <input formControlName="street" />
        <input formControlName="city" />
        <input formControlName="zip" />
    </div>

    <!-- form array -->
    <div formArrayName="items">
        <div *ngFor="let item of items.controls; let i = index"
             [formGroupIndex]="i">
            <input formControlName="name" />
            <input formControlName="quantity" type="number" />
            <button type="button" (click)="removeItem(i)">Remove</button>
        </div>
    </div>
    <button type="button" (click)="addItem()">Add Item</button>

    <button type="submit" [disabled]="orderForm.invalid">Submit</button>
    <button type="button" (click)="onReset()">Reset</button>
</form>
```

---

## 14. Routing

```typescript
// app-routing.module.ts
const routes: Routes = [
    { path: "",           redirectTo: "/orders", pathMatch: "full" },
    { path: "orders",     component: OrderListComponent },
    { path: "orders/:id", component: OrderDetailComponent },
    { path: "admin",      component: AdminComponent,
                          canActivate: [AuthGuard] },       // protected route ✅
    // lazy loading
    { path: "settings",
      loadChildren: () => import("./settings/settings.module")
                          .then(m => m.SettingsModule) },   // lazy ✅
    { path: "**",         component: NotFoundComponent }    // wildcard
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
```

```html
<!-- navigation in template -->
<nav>
    <a routerLink="/">Home</a>
    <a routerLink="/orders" routerLinkActive="active">Orders</a>
    <a [routerLink]="['/orders', order.id]">Order Detail</a>
</nav>

<!-- router outlet — where components render -->
<router-outlet></router-outlet>
```

```typescript
// programmatic navigation
@Component({...})
export class OrderComponent {

    constructor(private router: Router,
                private route: ActivatedRoute) {}

    goToOrder(id: number): void {
        this.router.navigate(["/orders", id]);
        this.router.navigate(["../detail"], { relativeTo: this.route });
        this.router.navigateByUrl("/orders?status=PAID");
    }

    // get route params
    ngOnInit(): void {
        // snapshot (one time)
        const id = this.route.snapshot.paramMap.get("id");

        // observable (updates on change)
        this.route.paramMap.subscribe(params => {
            const id = params.get("id");
            this.loadOrder(+id!);
        });

        // query params
        this.route.queryParams.subscribe(params => {
            const status = params["status"];
        });
    }
}
```

---

## 15. Route Guards

```typescript
// auth.guard.ts
@Injectable({ providedIn: "root" })
export class AuthGuard implements CanActivate {

    constructor(private authService: AuthService,
                private router: Router) {}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): boolean {
        if (this.authService.isLoggedIn()) {
            return true; // allow navigation ✅
        }
        this.router.navigate(["/login"]);
        return false; // block navigation ❌
    }
}

// role guard
@Injectable({ providedIn: "root" })
export class RoleGuard implements CanActivate {

    canActivate(route: ActivatedRouteSnapshot): boolean {
        const requiredRole = route.data["role"];
        return this.authService.hasRole(requiredRole);
    }
}

// usage in routes
{ path: "admin",   canActivate: [AuthGuard, RoleGuard],
                   data: { role: "ADMIN" },
                   component: AdminComponent }
```

---

## 16. RxJS — Observables

```typescript
import { Observable, Subject, BehaviorSubject,
         combineLatest, forkJoin, of, from } from "rxjs";
import { map, filter, switchMap, mergeMap, catchError,
         debounceTime, distinctUntilChanged, takeUntil,
         tap, finalize } from "rxjs/operators";

// Observable basics
const obs$ = new Observable(observer => {
    observer.next("value1");
    observer.next("value2");
    observer.complete();
});

obs$.subscribe({
    next:     (val) => console.log(val),
    error:    (err) => console.error(err),
    complete: ()    => console.log("done")
});

// Subject — multicast Observable
const subject$ = new Subject<string>();
subject$.subscribe(val => console.log("Sub1:", val));
subject$.subscribe(val => console.log("Sub2:", val));
subject$.next("Hello"); // both subscribers receive ✅

// BehaviorSubject — holds current value
const state$ = new BehaviorSubject<string>("initial");
state$.getValue(); // get current value ✅
state$.next("updated");

// Common operators
this.searchTerm$.pipe(
    debounceTime(500),              // wait 500ms after last event
    distinctUntilChanged(),         // ignore if same value
    switchMap(term =>               // cancel previous, use latest
        this.orderService.search(term)
    ),
    catchError(err => of([]))       // handle error gracefully
).subscribe(orders => this.orders = orders);

// forkJoin — parallel HTTP calls (like Promise.all)
forkJoin({
    orders:   this.orderService.getOrders(),
    customers: this.customerService.getCustomers()
}).subscribe(({ orders, customers }) => {
    this.orders    = orders;
    this.customers = customers;
});

// combineLatest — combine latest values from multiple streams
combineLatest([
    this.filterStatus$,
    this.filterDate$
]).pipe(
    switchMap(([status, date]) =>
        this.orderService.getOrders(status, date)
    )
).subscribe(orders => this.orders = orders);

// takeUntil — unsubscribe on component destroy
private destroy$ = new Subject<void>();

this.orderService.getOrders().pipe(
    takeUntil(this.destroy$)  // auto-unsubscribe ✅
).subscribe(orders => this.orders = orders);

ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
}
```

---

## 17. Lifecycle Hooks

```typescript
@Component({...})
export class OrderComponent implements
    OnInit, OnChanges, DoCheck, AfterViewInit,
    AfterContentInit, OnDestroy {

    ngOnChanges(changes: SimpleChanges): void {
        // called when @Input() properties change
        console.log("Input changed:", changes);
    }

    ngOnInit(): void {
        // called once after first ngOnChanges
        // best place for API calls ✅
        this.loadData();
    }

    ngDoCheck(): void {
        // called on every change detection cycle
        // use sparingly — performance impact ⚠️
    }

    ngAfterContentInit(): void {
        // called after ng-content projected
    }

    ngAfterViewInit(): void {
        // called after view and child views initialized
        // safe to access DOM ✅
    }

    ngOnDestroy(): void {
        // called before component destroyed
        // unsubscribe, clear timers ✅
        this.destroy$.next();
        this.destroy$.complete();
    }
}
```

---

## 18. Component Communication

```typescript
// Parent → Child: @Input()
// Child → Parent: @Output() EventEmitter
// Sibling → Sibling: Shared Service with Subject/BehaviorSubject
// Any → Any: Service with BehaviorSubject (state management)

// ViewChild — access child component/element from parent
@ViewChild(OrderFormComponent) orderForm!: OrderFormComponent;
@ViewChild("myInput") myInput!: ElementRef;

ngAfterViewInit(): void {
    this.myInput.nativeElement.focus();
    this.orderForm.reset(); // call child method ✅
}

// ContentChild — access projected content
@ContentChild("title") titleRef!: ElementRef;
```

---

## 19. State Management

### Service with BehaviorSubject (simple)
```typescript
@Injectable({ providedIn: "root" })
export class OrderStateService {

    private ordersSubject = new BehaviorSubject<Order[]>([]);
    orders$ = this.ordersSubject.asObservable(); // expose as Observable ✅

    private loadingSubject = new BehaviorSubject<boolean>(false);
    loading$ = this.loadingSubject.asObservable();

    constructor(private orderService: OrderService) {}

    loadOrders(): void {
        this.loadingSubject.next(true);
        this.orderService.getOrders().subscribe({
            next:     (orders) => { this.ordersSubject.next(orders); },
            error:    (err)    => console.error(err),
            complete: ()       => this.loadingSubject.next(false)
        });
    }

    addOrder(order: Order): void {
        const current = this.ordersSubject.getValue();
        this.ordersSubject.next([...current, order]);
    }

    removeOrder(id: number): void {
        const current = this.ordersSubject.getValue();
        this.ordersSubject.next(current.filter(o => o.id !== id));
    }
}

// use in component
@Component({...})
export class OrderListComponent {
    orders$ = this.orderState.orders$;
    loading$ = this.orderState.loading$;

    constructor(private orderState: OrderStateService) {}

    ngOnInit(): void {
        this.orderState.loadOrders();
    }
}
```

```html
<!-- use async pipe — auto subscribes and unsubscribes ✅ -->
<div *ngIf="loading$ | async">Loading...</div>
<ul>
    <li *ngFor="let order of orders$ | async">{{ order.id }}</li>
</ul>
```

---

## 20. Angular Signals (Angular 16+)

```typescript
import { signal, computed, effect } from "@angular/core";

@Component({...})
export class OrderComponent {

    // signal — reactive state
    count    = signal(0);
    orders   = signal<Order[]>([]);
    isLoading = signal(false);

    // computed — derived signal (like useMemo in React)
    total = computed(() =>
        this.orders().reduce((sum, o) => sum + o.amount, 0)
    );

    // effect — side effect (like useEffect in React)
    constructor() {
        effect(() => {
            console.log("Orders changed:", this.orders());
        });
    }

    increment(): void {
        this.count.update(v => v + 1);   // update based on previous ✅
        this.count.set(10);               // set absolute value
    }

    addOrder(order: Order): void {
        this.orders.update(orders => [...orders, order]);
    }
}
```

```html
<!-- use signals in template — no async pipe needed -->
<p>Count: {{ count() }}</p>
<p>Total: {{ total() }}</p>
<div *ngIf="isLoading()">Loading...</div>
```

---

## 21. Change Detection

```typescript
// Default — checks entire component tree on every event
@Component({
    changeDetection: ChangeDetectionStrategy.Default
})

// OnPush — only checks when:
// → @Input() reference changes
// → event triggered in component
// → async pipe emits
// → ChangeDetectorRef.markForCheck() called
// → BETTER PERFORMANCE ✅
@Component({
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderCardComponent {
    @Input() order!: Order;

    constructor(private cdr: ChangeDetectorRef) {}

    // manually trigger change detection
    refreshData(): void {
        this.cdr.markForCheck();    // mark for check on next cycle
        this.cdr.detectChanges();   // immediately run change detection
    }
}
```

---

## 22. Standalone Components (Angular 14+)

```typescript
// no NgModule needed ✅
@Component({
    standalone: true,
    selector: "app-order-card",
    templateUrl: "./order-card.component.html",
    imports: [CommonModule, RouterModule] // import directly
})
export class OrderCardComponent {
    @Input() order!: Order;
}

// bootstrap standalone app
// main.ts
bootstrapApplication(AppComponent, {
    providers: [
        provideRouter(routes),
        provideHttpClient(withInterceptors([authInterceptor]))
    ]
});
```

---

## 23. Common Interview Questions

| Question | Answer |
|---|---|
| **Angular vs React** | Angular = full framework, opinionated, TypeScript. React = library, flexible |
| **What is NgModule** | Container organizing components, services, pipes into cohesive block |
| **Lazy loading** | Load feature module only when route is visited — reduces initial bundle |
| **Two-way binding** | `[(ngModel)]` = `[ngModel]` + `(ngModelChange)` combined |
| **Directive vs Component** | Component = directive with template. Directive = behavior without template |
| **Subject vs BehaviorSubject** | Subject = no initial value, BehaviorSubject = holds current value |
| **switchMap vs mergeMap** | switchMap = cancel previous (search), mergeMap = run all parallel |
| **takeUntil** | Unsubscribe from Observable when destroy$ emits — prevent memory leak |
| **ChangeDetection OnPush** | Only check component when Input changes or event fires — better perf |
| **Guard types** | canActivate, canDeactivate, canLoad, canActivateChild, resolve |
| **Interceptor** | Middleware for HTTP requests — add headers, handle errors, show loading |
| **trackBy in ngFor** | Helps Angular identify items — prevents full list re-render |
| **Signals** | New reactive primitive in Angular 16+ — simpler than RxJS for state |
| **Standalone components** | Angular 14+ — no NgModule needed, import directly in component |
| **async pipe** | Auto subscribes to Observable in template, auto-unsubscribes on destroy |

---

## 24. Best Practices

```
✅ Use OnPush change detection for better performance
✅ Unsubscribe from Observables (takeUntil + destroy$)
✅ Use async pipe in templates — auto unsubscribes
✅ Lazy load feature modules
✅ Use reactive forms over template-driven for complex forms
✅ Use trackBy in *ngFor for large lists
✅ Use environment files for API URLs
✅ Use interceptors for auth token and error handling
✅ Create feature modules for large apps
✅ Use standalone components (Angular 14+)
✅ Use Signals for simple state (Angular 16+)
❌ Never manipulate DOM directly — use Renderer2
❌ Never subscribe without unsubscribing (memory leak)
❌ Never use any type — TypeScript strict mode ✅
❌ Never put business logic in components — use services
```
