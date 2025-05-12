// src/main.jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
// <- если файл лежит в src/App.jsx, то:
import App from './App.jsx'
import './index.css' 
import logoUrl from './assets/logo.png'
// или, при правильной настройке Vite, можно:
// import App from './App'
const link = document.createElement('link')
link.rel = 'icon'
link.type = 'image/png'
link.href = logoUrl
document.head.append(link)

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
)
