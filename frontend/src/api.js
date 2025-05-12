// src/api/index.js

const BASE_URL = 'http://localhost:8080/api';

function authHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
}

export async function register({ fullName, login, password }) {
    const res = await fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName, login, password }),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Ошибка регистрации');
    }
    return res.json();
}

export async function login({ login, password }) {
    const res = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, password }),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || 'Ошибка входа');
    }
    return res.json();
}

export function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
}

export async function getTeacherPreferences(type) {
    const res = await fetch(
        `${BASE_URL}/teacher/preferences?type=${encodeURIComponent(type)}`,
        { method: 'GET', headers: authHeaders() }
    );
    if (res.status === 204 || res.status === 403) return [];
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || res.statusText);
    }
    return res.json();
}

export async function saveTeacherPreferences(type, dtos) {
    const payload = dtos.map(dto => ({ ...dto, type }));
    const res = await fetch(`${BASE_URL}/teacher/preferences`, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: res.statusText }));
        throw new Error(err.message || res.statusText);
    }
    return res.json();
}

export async function getAllPreferences() {
    const res = await fetch(`${BASE_URL}/admin/preferences`, {
        headers: authHeaders()
    });
    if (!res.ok) throw new Error(await res.text());
    return res.json();
}

export async function exportPreferencesToExcel() {
    const res = await fetch(`${BASE_URL}/admin/preferences/export`, {
        headers: authHeaders()
    });
    if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || res.statusText);
    }
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `preferences.xlsx`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}

export default {
    register,
    login,
    logout,
    getTeacherPreferences,
    saveTeacherPreferences,
    getAllPreferences,
    exportPreferencesToExcel,
};
