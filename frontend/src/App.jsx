// src/App.jsx

import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import AdminDashboard from './components/AdminDashboard';
import TeacherDashboard from './components/TeacherDashboard';
import PreferencesForm from './components/PreferencesForm';
import Register from './components/Register';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/admin" element={<AdminDashboard />} />
                <Route path="/teacher" element={<TeacherDashboard />} />
                {/* здесь ':type' — будет либо 'semester', либо 'session' */}
                <Route path="/teacher/:type" element={<PreferencesForm />} />
                <Route path="*" element={<h2>Страница не найдена</h2>} />
            </Routes>
        </BrowserRouter>
    );
}
