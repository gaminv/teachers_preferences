// src/components/Login.jsx

import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login as apiLogin } from '../api';
import logo from '../assets/logo.png';

function Spinner() {
    return (
        <svg
            className="animate-spin h-5 w-5 text-white"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            aria-hidden="true"
        >
            <circle
                className="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                strokeWidth="4"
            />
            <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v8z"
            />
        </svg>
    );
}

export default function Login() {
    const [loginValue, setLoginValue] = useState('');
    const [password, setPassword] = useState('');
    const [showPwd, setShowPwd] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const validate = () => {
        if (!loginValue) {
            setError('Логин обязателен');
            return false;
        }
        if (!password) {
            setError('Пароль обязателен');
            return false;
        }
        if (password.length < 6) {
            setError('Пароль минимум 6 символов');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!validate()) return;

        setLoading(true);
        try {
            const data = await apiLogin({ login: loginValue, password });
            if (!data.token) {
                throw new Error('Не удалось получить токен');
            }
            localStorage.setItem('token', data.token);
            localStorage.setItem(
                'user',
                JSON.stringify({ fullName: data.fullName, role: data.role })
            );
            navigate(data.role === 'ADMIN' ? '/admin' : '/teacher', {
                replace: true,
            });
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Ошибка при входе');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-white relative overflow-hidden">
            {/* Фоновый SVG-паттерн */}
            <svg
                className="absolute inset-0 w-full h-full opacity-10"
                preserveAspectRatio="none"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 600 600"
            >
                <circle cx="300" cy="300" r="200" fill="#3b82f6" />
            </svg>

            <div className="relative z-10 w-full max-w-md bg-white p-8 rounded-2xl shadow-xl ring-1 ring-gray-200">
                {/* Логотип */}
                <div className="flex justify-center mb-4">
                    <img src={logo} alt="Logo" className="h-10 w-auto" />
                </div>

                <p className="text-center text-gray-700 font-medium text-lg tracking-wide mb-8">
                    Войдите, чтобы заполнить свои предпочтения по расписанию.
                </p>

                {error && (
                    <div
                        role="alert"
                        aria-live="assertive"
                        className="bg-red-100 text-red-700 px-4 py-2 rounded mb-4 text-center"
                    >
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-5">
                    {/* Логин */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Логин
                        </label>
                        <input
                            type="text"
                            value={loginValue}
                            onChange={(e) => setLoginValue(e.target.value)}
                            placeholder="Введите ваш логин"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 transition-shadow duration-200"
                        />
                    </div>

                    {/* Пароль */}
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Пароль
                        </label>
                        <input
                            type={showPwd ? 'text' : 'password'}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            className="w-full pr-10 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400 transition-shadow duration-200"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPwd((p) => !p)}
                            className="absolute right-3 top-9 text-gray-500 hover:text-gray-700"
                            aria-label={showPwd ? 'Скрыть пароль' : 'Показать пароль'}
                        >
                            {showPwd ? (
                                /* Иконка «скрыть» */
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="h-5 w-5"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={2}
                                        d="M13.875 18.825A10.05 10.05 0 0112 19c-5.523 0-10-4.477-10-10a9.965 9.965 0 01.175-1.875M4.868 4.868A9.965 9.965 0 0112 5c5.523 0 10 4.477 10 10 0 .73-.088 1.428-.253 2.084M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                                    />
                                </svg>
                            ) : (
                                /* Иконка «показать» */
                                <svg
                                    xmlns="http://www.w3.org/2000/svg"
                                    className="h-5 w-5"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={2}
                                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                                    />
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={2}
                                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                                    />
                                </svg>
                            )}
                        </button>
                    </div>

                    {/* Кнопка входа */}
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full flex items-center justify-center py-2 px-4 bg-blue-600 text-white font-semibold rounded-lg shadow hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-transform duration-100 active:scale-95 disabled:opacity-50"
                    >
                        {loading ? <Spinner /> : 'Войти'}
                    </button>
                </form>
            </div>
        </div>
    );
}
