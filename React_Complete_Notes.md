# React Complete Notes
> Crash Course — From Setup to Deployment

---

## 1. Intro

**Why React?**
- React uses a **Virtual DOM** which makes it faster and more popular
- It won't reload the entire page — only the modified component is replaced dynamically
- When something changes → React compares Virtual DOM with Real DOM → only the changed part of Real DOM is updated

```
Change detected → Virtual DOM updated → Diff with Real DOM → Only changed node updated
```

---

## 2. React Setup

**Prerequisites:**
- React requires **Node.js** — it helps run JavaScript outside the browser
- Download **LTS (Long Term Support)** version from nodejs.org
- Node.js installation also installs **npm** (Node Package Manager)
- npm helps add libraries like animations, routing, form handling, etc.

**Recommended IDE:** WebStorm or VS Code

---

## 3. Create React Project

```bash
# Using Vite (recommended — faster than CRA)
npm create vite@latest my-app -- --template react
cd my-app
npm install
npm run dev
```

**During setup it asks:**
- Project name
- Framework → React
- Language → JavaScript or TypeScript

**Project runs on:** `http://localhost:5173`

---

## 4. Project Structure

```
my-app/
├── public/          → static files (favicon, images)
├── src/
│   ├── assets/      → images, icons (import and use in components)
│   ├── components/  → reusable React components
│   ├── App.jsx      → main UI of the app
│   ├── main.jsx     → entry point of React application
│   └── index.css    → global CSS styles
├── index.html       → HTML page with <div id="root">
├── package.json     → project metadata, scripts, dependencies
├── package-lock.json→ locks all dependency versions (auto-generated)
├── eslint.config.js → code quality rules, catches bugs and style violations
└── .gitignore       → includes node_modules (never commit node_modules)
```

**Key files explained:**
- `index.html` → contains `<div id="root">` where React mounts
- `main.jsx` → entry point — mounts `<App />` into root div
- `App.jsx` → main UI component
- `package-lock.json` → ensures app runs exactly the same locally and on servers
- `node_modules` → managed by npm, never touch manually, add to `.gitignore`

---

## 5. Components

React components can be defined in **two ways:**

### Class Component (old way — not widely used)
```jsx
import React, { Component } from 'react';

class MovieCard extends Component {
  render() {
    return <div>Movie Card</div>;
  }
}

export default MovieCard;
```

### Functional Component (modern — recommended)
```jsx
const MovieCard = () => {
  return (
    <div className="movie-card">
      <h2>Movie Title</h2>
    </div>
  );
};

export default MovieCard;
```

### Reusing Components
```jsx
// App.jsx
import MovieCard from './components/MovieCard';

const App = () => {
  return (
    <div>
      <MovieCard />
      <MovieCard />
      <MovieCard />
    </div>
  );
};
```

---

## 6. Props

Props allow passing data from **parent to child** component.

### Passing props (parent)
```jsx
// App.jsx — parent
const App = () => {
  return (
    <div>
      <MovieCard title="Inception" rating={9.0} year={2010} />
      <MovieCard title="Interstellar" rating={8.6} year={2014} />
    </div>
  );
};
```

### Receiving props (child)
```jsx
// MovieCard.jsx — child
const MovieCard = ({ title, rating, year }) => {
  return (
    <div className="movie-card">
      <h2>{title}</h2>
      <p>Rating: {rating}</p>
      <p>Year: {year}</p>
    </div>
  );
};
```

### Multiple props with object spread
```jsx
const movie = { title: "Inception", rating: 9.0, year: 2010, poster: "url" };
<MovieCard {...movie} />
```

**Reusability:** Same component, different data — update the prop value and the UI updates automatically.

---

## 7. Styles

### 3 ways to apply CSS in React:

### 1. className (external CSS file)
```jsx
// index.css or MovieCard.css
.movie-card {
  background: #1a1a2e;
  border-radius: 10px;
  padding: 20px;
}

// Component
<div className="movie-card">...</div>
```

### 2. Inline styles
```jsx
<div style={{ backgroundColor: '#1a1a2e', borderRadius: '10px', padding: '20px' }}>
  Movie Card
</div>
```

### 3. CSS Variables (in index.css)
```css
:root {
  --primary-color: #1a1a2e;
  --accent-color: #e94560;
}
/* Use -- and lowercase letters, values without quotes */
```

**Priority:** Inline styles > External CSS

### Import CSS files
```jsx
import './MovieCard.css';         // component-specific CSS
import './index.css';             // global CSS
```

### Import assets
```jsx
import logo from './assets/logo.png';
import heroIcon from './assets/hero.svg';

// Use in component
<img src={logo} alt="Logo" />
<heroIcon className="icon" />
```

---

## 8. State and Hooks

**State** is the brain of a component — it holds information that changes over time and triggers re-render when updated.

### Any function starting with `use` is a Hook in React

### useState
```jsx
import { useState } from 'react';

const MovieCard = () => {
  const [hasLiked, setHasLiked] = useState(false);  // [variable, setter]
  const [count, setCount] = useState(0);

  const handleLike = () => {
    setHasLiked(!hasLiked);  // triggers re-render
  };

  return (
    <div>
      <button onClick={handleLike}>
        {hasLiked ? '❤️ Liked' : '🤍 Like'}
      </button>
      <p>Likes: {count}</p>
    </div>
  );
};
```

**Note:** On page load, every state resets to its initial value.

### Common Hooks
```jsx
useState      → manage local state
useEffect     → side effects (API calls, subscriptions)
useRef        → reference DOM element, persists without re-render
useCallback   → memoize functions
useMemo       → memoize expensive calculations
useContext    → consume global context
useReducer    → complex state management
```

---

## 9. useEffect Hook

`useEffect` runs side effects — API calls, subscriptions, timers — after render.

### Syntax
```jsx
useEffect(() => {
  // side effect code here
  return () => {
    // cleanup (runs on unmount or before next effect)
  };
}, [dependencies]);  // dependency array
```

### Examples
```jsx
import { useState, useEffect } from 'react';

const MovieList = () => {
  const [movies, setMovies] = useState([]);

  // Runs ONCE on mount (empty dependency array)
  useEffect(() => {
    fetchMovies();
  }, []);

  // Runs whenever searchTerm changes
  useEffect(() => {
    fetchMovies(searchTerm);
  }, [searchTerm]);

  // Runs on EVERY render (no dependency array)
  useEffect(() => {
    console.log('Component rendered');
  });

  // With cleanup
  useEffect(() => {
    const timer = setTimeout(() => fetchMovies(searchTerm), 500);
    return () => clearTimeout(timer);  // cleanup previous timer
  }, [searchTerm]);
};
```

---

## 10. React Snippets (VS Code)

**Install:** ES7+ React/Redux/React-Native Snippets extension

| Shortcut | Generates |
|---|---|
| `rafce` | Arrow function component with export |
| `rfce` | Function component with export |
| `useState` | useState snippet |
| `useEffect` | useEffect snippet |
| `imr` | import React |

---

## 11. Tailwind CSS Setup

```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

**tailwind.config.js**
```js
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: { extend: {} },
  plugins: [],
}
```

**index.css**
```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

**Usage in components:**
```jsx
<div className="bg-gray-900 rounded-xl p-4 shadow-lg hover:scale-105 transition">
  <h2 className="text-white text-xl font-bold">Movie Title</h2>
</div>
```

---

## 12. Assets & Styles — Movie App

### Folder structure
```
src/
├── assets/
│   ├── hero.png
│   ├── star.svg
│   └── search.svg
├── components/
│   ├── MovieCard.jsx
│   └── Search.jsx
├── App.jsx
└── index.css
```

### Global CSS variables
```css
/* index.css */
:root {
  --primary: #030014;
  --secondary: #1a1a2e;
  --accent: #e94560;
  --text: #ffffff;
}

body {
  background-color: var(--primary);
  color: var(--text);
  font-family: 'DM Sans', sans-serif;
}
```

---

## 13. Develop Header

```jsx
// Header.jsx
const Header = () => {
  return (
    <header className="w-full py-6 px-8 flex items-center justify-between">
      <img src={logo} alt="Logo" className="w-12 h-12" />
      <h1 className="text-3xl font-bold text-white">
        Find <span className="text-accent">Movies</span> You'll Enjoy
      </h1>
    </header>
  );
};
```

---

## 14. The Movie DB API (TMDB)

**Setup:**
1. Sign up at themoviedb.org
2. Get API key from Settings → API
3. Store in `.env` file

```bash
# .env
VITE_TMDB_API_KEY=your_api_key_here
VITE_TMDB_BASE_URL=https://api.themoviedb.org/3
```

**Fetch movies:**
```jsx
const API_KEY = import.meta.env.VITE_TMDB_API_KEY;
const BASE_URL = import.meta.env.VITE_TMDB_BASE_URL;

const fetchMovies = async (query = '') => {
  const endpoint = query
    ? `${BASE_URL}/search/movie?api_key=${API_KEY}&query=${query}`
    : `${BASE_URL}/discover/movie?api_key=${API_KEY}&sort_by=popularity.desc`;

  const response = await fetch(endpoint);
  const data = await response.json();
  return data.results;
};
```

**Movie object structure:**
```json
{
  "id": 123,
  "title": "Inception",
  "poster_path": "/poster.jpg",
  "vote_average": 8.8,
  "release_date": "2010-07-16",
  "overview": "A thief who steals..."
}
```

---

## 15. Movie Card Component

```jsx
// MovieCard.jsx
const MovieCard = ({ movie }) => {
  const { title, poster_path, vote_average, release_date } = movie;
  const imageUrl = poster_path
    ? `https://image.tmdb.org/t/p/w500${poster_path}`
    : '/no-poster.png';

  return (
    <div className="movie-card bg-secondary rounded-xl overflow-hidden shadow-lg hover:scale-105 transition duration-300">
      <img src={imageUrl} alt={title} className="w-full h-64 object-cover" />
      <div className="p-4">
        <h3 className="text-white font-bold text-lg truncate">{title}</h3>
        <div className="flex justify-between mt-2">
          <span className="text-yellow-400">⭐ {vote_average.toFixed(1)}</span>
          <span className="text-gray-400">{release_date?.split('-')[0]}</span>
        </div>
      </div>
    </div>
  );
};

export default MovieCard;
```

---

## 16. Implement Search

```jsx
// Search.jsx
const Search = ({ searchTerm, setSearchTerm }) => {
  return (
    <div className="search-bar flex items-center bg-secondary rounded-xl px-4 py-3">
      <img src={searchIcon} alt="search" className="w-5 h-5" />
      <input
        type="text"
        placeholder="Search for movies..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="bg-transparent text-white ml-3 outline-none w-full"
      />
    </div>
  );
};
```

```jsx
// App.jsx — wire up search
const App = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [movies, setMovies] = useState([]);

  useEffect(() => {
    fetchMovies(searchTerm).then(setMovies);
  }, [searchTerm]);

  return (
    <div>
      <Header />
      <Search searchTerm={searchTerm} setSearchTerm={setSearchTerm} />
      <div className="grid grid-cols-4 gap-6">
        {movies.map(movie => <MovieCard key={movie.id} movie={movie} />)}
      </div>
    </div>
  );
};
```

---

## 17. Optimize Search (Debouncing)

**Problem:** API called on every keystroke → too many requests

**Solution:** Debounce — wait until user stops typing (500ms)

```jsx
// App.jsx — optimized search with debounce
const [debouncedSearch, setDebouncedSearch] = useState('');

useEffect(() => {
  const timer = setTimeout(() => {
    setDebouncedSearch(searchTerm);  // only update after 500ms of no typing
  }, 500);

  return () => clearTimeout(timer);  // cleanup previous timer
}, [searchTerm]);

useEffect(() => {
  fetchMovies(debouncedSearch).then(setMovies);
}, [debouncedSearch]);  // only fires when debounced value changes
```

```
Without debounce: "Inc" → 3 API calls (I, In, Inc)
With debounce:    "Inc" → 1 API call (after user stops typing 500ms)
```

---

## 18. Trending Movies Feature

### Using Appwrite (Backend as a Service) for tracking trending

```bash
npm install appwrite
```

```jsx
// appwrite.js
import { Client, Databases, Query } from 'appwrite';

const client = new Client()
  .setEndpoint('https://cloud.appwrite.io/v1')
  .setProject(import.meta.env.VITE_APPWRITE_PROJECT_ID);

const databases = new Databases(client);

// Track movie search
export const updateSearchCount = async (movie) => {
  try {
    // Check if movie already exists
    const result = await databases.listDocuments(DATABASE_ID, COLLECTION_ID, [
      Query.equal('movie_id', movie.id)
    ]);

    if (result.documents.length > 0) {
      // Increment count
      await databases.updateDocument(DATABASE_ID, COLLECTION_ID, result.documents[0].$id, {
        count: result.documents[0].count + 1
      });
    } else {
      // Create new entry
      await databases.createDocument(DATABASE_ID, COLLECTION_ID, ID.unique(), {
        movie_id: movie.id,
        title: movie.title,
        poster_path: movie.poster_path,
        count: 1
      });
    }
  } catch (error) {
    console.error('Error updating search count:', error);
  }
};

// Get trending movies
export const getTrendingMovies = async () => {
  const result = await databases.listDocuments(DATABASE_ID, COLLECTION_ID, [
    Query.orderDesc('count'),
    Query.limit(5)
  ]);
  return result.documents;
};
```

---

## 19. Show Trending Movies

```jsx
// TrendingMovies.jsx
const TrendingMovies = ({ trendingMovies }) => {
  if (!trendingMovies?.length) return null;

  return (
    <section className="trending">
      <h2 className="text-white text-2xl font-bold mb-4">🔥 Trending</h2>
      <ul className="flex gap-4 overflow-x-auto">
        {trendingMovies.map((movie, index) => (
          <li key={movie.movie_id} className="min-w-[150px]">
            <p className="text-accent font-bold text-5xl">{index + 1}</p>
            <img
              src={`https://image.tmdb.org/t/p/w200${movie.poster_path}`}
              alt={movie.title}
              className="rounded-lg"
            />
          </li>
        ))}
      </ul>
    </section>
  );
};
```

```jsx
// App.jsx — fetch and display trending
const [trendingMovies, setTrendingMovies] = useState([]);

useEffect(() => {
  getTrendingMovies().then(setTrendingMovies);
}, []);

// Track search
useEffect(() => {
  if (movies.length > 0 && debouncedSearch) {
    updateSearchCount(movies[0]); // track first result
  }
}, [movies]);
```

---

## 20. Deployment (Netlify)

### Steps:
```bash
# 1. Build the project
npm run build
# Creates /dist folder with optimized production files

# 2. Deploy to Netlify
# Option A — Drag and drop /dist folder to netlify.com
# Option B — Connect GitHub repo for auto-deploy on push
```

### Environment variables on Netlify:
```
Site Settings → Environment Variables → Add:
VITE_TMDB_API_KEY = your_key
VITE_APPWRITE_PROJECT_ID = your_id
```

### netlify.toml (for React Router support)
```toml
[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

---

## Quick Reference Cheat Sheet

| Concept | Syntax |
|---|---|
| Functional component | `const App = () => <div>Hello</div>` |
| Props | `<Card title="Hello" />` → `({ title })` |
| State | `const [val, setVal] = useState(0)` |
| Effect on mount | `useEffect(() => {}, [])` |
| Effect on change | `useEffect(() => {}, [dep])` |
| Conditional render | `{isLoading && <Spinner />}` |
| List render | `{items.map(i => <Card key={i.id} />)}` |
| Event handler | `<button onClick={handleClick}>` |
| Input binding | `value={val} onChange={e => setVal(e.target.value)}` |
| Import image | `import img from './assets/img.png'` |
| Env variable | `import.meta.env.VITE_API_KEY` |
