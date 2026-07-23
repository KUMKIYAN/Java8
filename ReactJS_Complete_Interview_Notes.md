# React JS — Complete Interview Notes
> Quick Reference for Interview & Development

---

## 1. What is React?

```
React = JavaScript library for building UIs
→ created by Facebook (2013)
→ component-based architecture
→ Virtual DOM for efficient updates
→ declarative — describe WHAT to render, React handles HOW
→ one-way data flow (parent → child via props)
```

---

## 2. Virtual DOM

```
Problem with Real DOM:
→ every change → entire DOM re-rendered ❌ (slow)

Virtual DOM solution:
→ lightweight in-memory copy of Real DOM
→ change detected → update Virtual DOM first
→ diff Virtual DOM with Real DOM (reconciliation)
→ update ONLY changed nodes in Real DOM ✅ (fast)

Flow:
State changes → Virtual DOM updated → Diff (reconciliation)
→ only changed Real DOM nodes updated ✅
```

---

## 3. JSX

```
JSX = JavaScript XML
→ write HTML-like syntax inside JavaScript
→ Babel compiles to React.createElement()
```

```jsx
// JSX
const element = <h1 className="title">Hello World</h1>;

// compiles to:
const element = React.createElement(
    "h1",
    { className: "title" },
    "Hello World"
);

// JSX rules:
// → className not class (class is reserved JS keyword)
// → self-close empty tags: <img />, <br />
// → one root element (or use Fragment)
// → JavaScript expressions in {}
```

---

## 4. Components

### Functional Component (modern — recommended)
```jsx
// arrow function
const OrderCard = ({ order }) => {
    return (
        <div className="card">
            <h2>{order.title}</h2>
            <p>{order.amount}</p>
        </div>
    );
};

export default OrderCard;

// with TypeScript
interface Props {
    order: Order;
    onDelete: (id: number) => void;
}

const OrderCard: React.FC<Props> = ({ order, onDelete }) => {
    return <div>{order.title}</div>;
};
```

### Class Component (legacy)
```jsx
class OrderCard extends React.Component {
    render() {
        return <div>{this.props.order.title}</div>;
    }
}
```

---

## 5. Props

```jsx
// parent passes props
<OrderCard
    title="Order 001"
    amount={100}
    isActive={true}
    onDelete={handleDelete}    // function as prop
    items={["a", "b"]}         // array as prop
    style={{ color: "red" }}   // object as prop
/>

// child receives props
const OrderCard = ({ title, amount, isActive, onDelete }) => {
    return (
        <div>
            <h2>{title}</h2>
            <p>{amount}</p>
            {isActive && <span>Active</span>}
            <button onClick={() => onDelete(id)}>Delete</button>
        </div>
    );
};

// default props
const OrderCard = ({ title = "Default Title", amount = 0 }) => {};

// children prop
const Card = ({ children }) => (
    <div className="card">{children}</div>
);
// usage: <Card><p>Hello</p></Card>

// spread props
const movie = { title: "Inception", rating: 9.0 };
<MovieCard {...movie} />  // same as title="Inception" rating={9.0}
```

---

## 6. State — useState

```jsx
import { useState } from "react";

const Counter = () => {
    // [currentValue, setter] = useState(initialValue)
    const [count, setCount] = useState(0);
    const [name, setName] = useState("");
    const [isOpen, setIsOpen] = useState(false);
    const [items, setItems] = useState([]);
    const [user, setUser] = useState(null);

    // update state — triggers re-render
    const increment = () => setCount(count + 1);
    const toggle    = () => setIsOpen(prev => !prev);  // functional update ✅

    // update object state — spread to keep other fields
    const [form, setForm] = useState({ name: "", email: "" });
    const updateName = (e) =>
        setForm(prev => ({ ...prev, name: e.target.value }));

    // update array state
    const addItem = (item) =>
        setItems(prev => [...prev, item]);
    const removeItem = (id) =>
        setItems(prev => prev.filter(i => i.id !== id));

    return (
        <div>
            <p>{count}</p>
            <button onClick={increment}>+</button>
        </div>
    );
};
```

---

## 7. useEffect

```jsx
import { useEffect } from "react";

const MovieList = ({ searchTerm }) => {
    const [movies, setMovies] = useState([]);

    // Runs ONCE on mount (empty dependency array)
    useEffect(() => {
        fetchMovies();
    }, []);

    // Runs when searchTerm changes
    useEffect(() => {
        fetchMovies(searchTerm);
    }, [searchTerm]);

    // Runs on EVERY render (no dependency array)
    useEffect(() => {
        console.log("rendered");
    });

    // With cleanup — prevent memory leaks
    useEffect(() => {
        const timer = setTimeout(() => fetchMovies(searchTerm), 500);
        return () => clearTimeout(timer); // cleanup on unmount or before next effect ✅
    }, [searchTerm]);

    // Event listener cleanup
    useEffect(() => {
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);
};
```

---

## 8. useRef

```jsx
import { useRef } from "react";

const InputForm = () => {
    // DOM reference
    const inputRef = useRef(null);

    const focusInput = () => {
        inputRef.current.focus(); // direct DOM access ✅
    };

    // persist value without re-render
    const renderCount = useRef(0);
    renderCount.current += 1;
    console.log("Renders:", renderCount.current); // no re-render ✅

    return (
        <div>
            <input ref={inputRef} type="text" />
            <button onClick={focusInput}>Focus</button>
        </div>
    );
};
```

---

## 9. useCallback and useMemo

```jsx
import { useCallback, useMemo } from "react";

const OrderList = ({ orders, userId }) => {

    // useCallback — memoize FUNCTION
    // prevents function recreation on every render
    // use when passing function to child component
    const handleDelete = useCallback((id) => {
        deleteOrder(id);
    }, []); // recreate only when dependencies change

    // useMemo — memoize VALUE
    // prevents expensive recalculation on every render
    const expensiveTotal = useMemo(() => {
        return orders.reduce((sum, o) => sum + o.amount, 0);
    }, [orders]); // recalculate only when orders change

    // useMemo for filtered list
    const userOrders = useMemo(() =>
        orders.filter(o => o.userId === userId),
    [orders, userId]);

    return (
        <div>
            <p>Total: {expensiveTotal}</p>
            {userOrders.map(o => (
                <OrderCard key={o.id} order={o} onDelete={handleDelete} />
            ))}
        </div>
    );
};
```

---

## 10. useContext — Global State

```jsx
import { createContext, useContext, useState } from "react";

// Step 1 — create context
const AuthContext = createContext(null);

// Step 2 — create provider
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);

    const login = (userData, token) => {
        setUser(userData);
        setToken(token);
    };

    const logout = () => {
        setUser(null);
        setToken(null);
    };

    return (
        <AuthContext.Provider value={{ user, token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

// Step 3 — custom hook
export const useAuth = () => useContext(AuthContext);

// Step 4 — wrap app with provider
const App = () => (
    <AuthProvider>
        <Router>
            <Routes />
        </Router>
    </AuthProvider>
);

// Step 5 — consume in any component
const Header = () => {
    const { user, logout } = useAuth();
    return (
        <nav>
            <span>Hello, {user?.name}</span>
            <button onClick={logout}>Logout</button>
        </nav>
    );
};
```

---

## 11. useReducer — Complex State

```jsx
import { useReducer } from "react";

// reducer function
const orderReducer = (state, action) => {
    switch (action.type) {
        case "ADD_ORDER":
            return { ...state, orders: [...state.orders, action.payload] };
        case "REMOVE_ORDER":
            return {
                ...state,
                orders: state.orders.filter(o => o.id !== action.payload)
            };
        case "SET_LOADING":
            return { ...state, loading: action.payload };
        default:
            return state;
    }
};

const initialState = { orders: [], loading: false, error: null };

const OrderManager = () => {
    const [state, dispatch] = useReducer(orderReducer, initialState);

    const addOrder = (order) =>
        dispatch({ type: "ADD_ORDER", payload: order });

    const removeOrder = (id) =>
        dispatch({ type: "REMOVE_ORDER", payload: id });

    return (
        <div>
            {state.loading && <p>Loading...</p>}
            {state.orders.map(o => <p key={o.id}>{o.title}</p>)}
        </div>
    );
};
```

---

## 12. Custom Hooks

```jsx
// useFetch — reusable data fetching hook
const useFetch = (url) => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const response = await fetch(url);
                const result = await response.json();
                setData(result);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [url]);

    return { data, loading, error };
};

// usage
const MovieList = () => {
    const { data: movies, loading, error } =
            useFetch("https://api.example.com/movies");

    if (loading) return <Spinner />;
    if (error)   return <Error message={error} />;
    return <div>{movies?.map(m => <MovieCard key={m.id} movie={m} />)}</div>;
};

// useDebounce — debounce search input
const useDebounce = (value, delay = 500) => {
    const [debouncedValue, setDebouncedValue] = useState(value);

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedValue(value), delay);
        return () => clearTimeout(timer);
    }, [value, delay]);

    return debouncedValue;
};

// usage
const Search = () => {
    const [searchTerm, setSearchTerm] = useState("");
    const debouncedSearch = useDebounce(searchTerm, 500);

    useEffect(() => {
        fetchMovies(debouncedSearch); // only fires 500ms after typing stops
    }, [debouncedSearch]);
};

// useLocalStorage
const useLocalStorage = (key, initialValue) => {
    const [value, setValue] = useState(() => {
        const stored = localStorage.getItem(key);
        return stored ? JSON.parse(stored) : initialValue;
    });

    const setStoredValue = (newValue) => {
        setValue(newValue);
        localStorage.setItem(key, JSON.stringify(newValue));
    };

    return [value, setStoredValue];
};
```

---

## 13. React.memo — Prevent Re-renders

```jsx
import { memo } from "react";

// without memo — re-renders every time parent re-renders
const MovieCard = ({ movie }) => {
    console.log("MovieCard rendered");
    return <div>{movie.title}</div>;
};

// with memo — only re-renders when props change
const MovieCard = memo(({ movie }) => {
    console.log("MovieCard rendered");
    return <div>{movie.title}</div>;
});

// memo with custom comparison
const MovieCard = memo(({ movie }) => {
    return <div>{movie.title}</div>;
}, (prevProps, nextProps) => {
    return prevProps.movie.id === nextProps.movie.id; // true = skip re-render
});
```

---

## 14. Event Handling

```jsx
const Form = () => {
    // click event
    const handleClick = () => console.log("clicked");
    const handleClickWithParam = (id) => console.log(id);

    // input change
    const handleChange = (e) => console.log(e.target.value);

    // form submit
    const handleSubmit = (e) => {
        e.preventDefault(); // prevent page reload ✅
        console.log("submitted");
    };

    // keyboard events
    const handleKeyDown = (e) => {
        if (e.key === "Enter") submitForm();
    };

    return (
        <form onSubmit={handleSubmit}>
            <input
                type="text"
                onChange={handleChange}
                onKeyDown={handleKeyDown}
            />
            <button onClick={handleClick}>Submit</button>
            <button onClick={() => handleClickWithParam(123)}>
                Delete
            </button>
        </form>
    );
};
```

---

## 15. Conditional Rendering

```jsx
const OrderStatus = ({ order }) => {
    return (
        <div>
            {/* if/else with ternary */}
            {order.isPaid
                ? <span className="paid">Paid ✅</span>
                : <span className="pending">Pending ⏳</span>
            }

            {/* short circuit — show only if true */}
            {order.hasDiscount && <span>10% OFF</span>}

            {/* nullish coalescing */}
            <p>{order.note ?? "No note"}</p>

            {/* optional chaining */}
            <p>{order.customer?.name}</p>

            {/* early return */}
            {!order && <p>No order found</p>}
        </div>
    );
};
```

---

## 16. Lists and Keys

```jsx
const MovieList = ({ movies }) => {
    return (
        <ul>
            {movies.map((movie) => (
                // key must be unique and stable ✅
                // never use index as key if list can reorder ❌
                <li key={movie.id}>
                    <MovieCard movie={movie} />
                </li>
            ))}
        </ul>
    );
};
```

---

## 17. Forms — Controlled vs Uncontrolled

```jsx
// Controlled — value from state ✅ (recommended)
const ControlledForm = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        login(email, password);
    };

    return (
        <form onSubmit={handleSubmit}>
            <input
                value={email}                        // value from state
                onChange={(e) => setEmail(e.target.value)}
            />
            <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            <button type="submit">Login</button>
        </form>
    );
};

// Uncontrolled — value from DOM ref
const UncontrolledForm = () => {
    const emailRef = useRef(null);

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(emailRef.current.value); // read from DOM directly
    };

    return (
        <form onSubmit={handleSubmit}>
            <input ref={emailRef} type="email" />
            <button type="submit">Submit</button>
        </form>
    );
};
```

---

## 18. React Router (v6)

```jsx
import { BrowserRouter, Routes, Route, Link,
         useNavigate, useParams, useLocation } from "react-router-dom";

// Setup
const App = () => (
    <BrowserRouter>
        <nav>
            <Link to="/">Home</Link>
            <Link to="/movies">Movies</Link>
        </nav>
        <Routes>
            <Route path="/"           element={<Home />} />
            <Route path="/movies"     element={<MovieList />} />
            <Route path="/movies/:id" element={<MovieDetail />} />
            <Route path="*"           element={<NotFound />} />
        </Routes>
    </BrowserRouter>
);

// useParams — get URL params
const MovieDetail = () => {
    const { id } = useParams();  // /movies/123 → id = "123"
    return <div>Movie {id}</div>;
};

// useNavigate — programmatic navigation
const LoginForm = () => {
    const navigate = useNavigate();
    const handleLogin = () => {
        login();
        navigate("/dashboard");           // go to dashboard
        navigate(-1);                     // go back
        navigate("/login", { replace: true }); // replace history entry
    };
};

// useLocation — current location
const Breadcrumb = () => {
    const location = useLocation();
    return <p>Current: {location.pathname}</p>;
};

// Protected Route
const ProtectedRoute = ({ children }) => {
    const { user } = useAuth();
    return user ? children : <Navigate to="/login" />;
};

// usage
<Route path="/dashboard" element={
    <ProtectedRoute><Dashboard /></ProtectedRoute>
} />
```

---

## 19. Lazy Loading and Suspense

```jsx
import { lazy, Suspense } from "react";

// lazy load component — only loaded when needed
const Dashboard   = lazy(() => import("./Dashboard"));
const MovieDetail = lazy(() => import("./MovieDetail"));

const App = () => (
    <Suspense fallback={<div>Loading...</div>}>
        <Routes>
            <Route path="/dashboard"   element={<Dashboard />} />
            <Route path="/movies/:id"  element={<MovieDetail />} />
        </Routes>
    </Suspense>
);
```

---

## 20. Error Boundaries

```jsx
class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true };
    }

    componentDidCatch(error, errorInfo) {
        console.error("Error:", error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return <h1>Something went wrong.</h1>;
        }
        return this.props.children;
    }
}

// usage
<ErrorBoundary>
    <MovieList />
</ErrorBoundary>
```

---

## 21. Styling Options

```jsx
// 1. CSS className
import "./MovieCard.css";
<div className="movie-card active">

// 2. Inline styles (camelCase)
<div style={{ backgroundColor: "red", fontSize: "16px" }}>

// 3. CSS Modules
import styles from "./MovieCard.module.css";
<div className={styles.card}>

// 4. Tailwind CSS
<div className="bg-gray-900 rounded-xl p-4 text-white">

// 5. Template literals (conditional classes)
<div className={`card ${isActive ? "active" : ""}`}>

// 6. clsx library
import clsx from "clsx";
<div className={clsx("card", { active: isActive, large: isLarge })}>
```

---

## 22. API Calls — fetch and axios

```jsx
// fetch
const fetchMovies = async () => {
    try {
        const response = await fetch("https://api.example.com/movies");
        if (!response.ok) throw new Error("Failed to fetch");
        const data = await response.json();
        setMovies(data);
    } catch (error) {
        setError(error.message);
    }
};

// axios
import axios from "axios";

const api = axios.create({
    baseURL: "https://api.example.com",
    timeout: 5000,
    headers: { "Authorization": `Bearer ${token}` }
});

const fetchMovies = async () => {
    try {
        const { data } = await api.get("/movies");
        setMovies(data);
    } catch (error) {
        setError(error.message);
    }
};
```

---

## 23. Performance Optimization

```jsx
// 1. React.memo — skip re-render if props unchanged
const MovieCard = memo(({ movie }) => <div>{movie.title}</div>);

// 2. useCallback — stable function reference
const handleDelete = useCallback((id) => deleteOrder(id), []);

// 3. useMemo — memoize expensive computation
const total = useMemo(() =>
    orders.reduce((sum, o) => sum + o.amount, 0), [orders]);

// 4. Lazy loading — load components on demand
const Dashboard = lazy(() => import("./Dashboard"));

// 5. Key prop — help React identify changed items
{items.map(item => <Card key={item.id} item={item} />)}

// 6. Virtualization — render only visible items (react-window)
import { FixedSizeList } from "react-window";
<FixedSizeList height={600} itemCount={10000} itemSize={50}>
    {({ index, style }) => <div style={style}>Item {index}</div>}
</FixedSizeList>

// 7. Code splitting — split bundle per route
const MovieList = lazy(() => import("./MovieList"));
```

---

## 24. All Hooks Summary

| Hook | Purpose | When to use |
|---|---|---|
| `useState` | Local state | Component data that changes |
| `useEffect` | Side effects | API calls, subscriptions, timers |
| `useRef` | DOM reference / persist value | Focus input, store mutable value |
| `useCallback` | Memoize function | Pass stable function to child |
| `useMemo` | Memoize value | Expensive calculations |
| `useContext` | Global state | Auth, theme, language |
| `useReducer` | Complex state | Multiple related state updates |
| `useLayoutEffect` | DOM measurement | Before browser paint |
| `useId` | Unique ID | Accessibility attributes |
| `useTransition` | Mark non-urgent update | Search, filter (keep UI responsive) |
| `useDeferredValue` | Defer value update | Large list rendering |

---

## 25. Common Interview Questions

| Question | Answer |
|---|---|
| **Virtual DOM** | In-memory DOM copy — diffs with real DOM, updates only changed nodes |
| **Props vs State** | Props = external (read-only), State = internal (mutable) |
| **Why keys in lists** | Help React identify changed/added/removed items |
| **Controlled vs Uncontrolled** | Controlled = value from state, Uncontrolled = value from DOM ref |
| **useEffect cleanup** | Return function runs on unmount — prevents memory leaks |
| **useCallback vs useMemo** | useCallback = memoize function, useMemo = memoize value |
| **React.memo** | Skip re-render if props unchanged |
| **Reconciliation** | Process of diffing Virtual DOM with Real DOM |
| **Lifting state up** | Move shared state to nearest common parent |
| **Prop drilling** | Passing props through many levels — solve with Context |
| **useState vs useReducer** | useState for simple, useReducer for complex related state |
| **Strict Mode** | Double renders in dev — catches side effects and deprecated APIs |
| **Fragment** | Group elements without extra DOM node `<>...</>` |
| **Portal** | Render child outside parent DOM hierarchy |
| **Synthetic events** | React wrapper around browser events — consistent cross-browser |
