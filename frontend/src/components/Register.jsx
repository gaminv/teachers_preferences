// frontend/src/components/Register.jsx

import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../api'

export default function Register() {
    const [fullName, setFullName] = useState('')
    const [login, setlogin] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [message, setMessage] = useState('')
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()

    const handleSubmit = async e => {
        e.preventDefault()
        setError('')
        setLoading(true)
        try {
            await register({ fullName, login, password })
            setMessage('Успешно зарегистрированы!')
            // navigate('/login')
        } catch (err) {
            setError(err.message || 'Ошибка регистрации')
        } finally {
            setLoading(false)
        }
    }

    return (
        // фон страницы и overflow-hidden
        <div className="min-h-screen flex items-center justify-center
                    bg-gradient-to-br from-blue-50 to-white relative overflow-hidden">

            {/* Сюда вставлен ОДИН В ОДИН тот же овал, что в Login.jsx */}
            <svg
                className="absolute inset-0 w-full h-full opacity-10"
                preserveAspectRatio="none"
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 600 600"
            >
                <circle cx="300" cy="300" r="200" fill="#3b82f6" />
            </svg>

            <div className="relative z-10 w-full max-w-md bg-white p-8
                      rounded-2xl shadow-xl ring-1 ring-gray-200">

                <h1 className="text-3xl font-extrabold text-center text-gray-800 mb-4">
                    Регистрация
                </h1>
                <p className="text-center text-gray-600 mb-6">
                    Создайте аккаунт, чтобы начать работу.
                </p>

                {message && (
                    <div className="bg-green-100 text-green-700 px-4 py-2 rounded mb-4 text-center">
                        {message}
                    </div>
                )}
                {error && (
                    <div className="bg-red-100 text-red-700 px-4 py-2 rounded mb-4 text-center">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            ФИО
                        </label>
                        <input
                            type="text"
                            required
                            placeholder="Введите ваше ФИО"
                            value={fullName}
                            onChange={e => setFullName(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg
                         focus:outline-none focus:ring-2 focus:ring-blue-400
                         transition-shadow duration-200"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            login
                        </label>
                        <input
                            type="login"
                            required
                            placeholder="Введите ваш login"
                            value={login}
                            onChange={e => setlogin(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg
                         focus:outline-none focus:ring-2 focus:ring-blue-400
                         transition-shadow duration-200"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Пароль (мин. 6 символов)
                        </label>
                        <input
                            type="password"
                            required
                            minLength={6}
                            placeholder="••••••••"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg
                         focus:outline-none focus:ring-2 focus:ring-blue-400
                         transition-shadow duration-200"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full flex justify-center py-2 px-4 bg-green-600 text-white
                       font-semibold rounded-lg shadow hover:bg-green-700
                       focus:outline-none focus:ring-2 focus:ring-offset-2
                       focus:ring-green-500 transition-colors duration-200
                       disabled:opacity-50"
                    >
                        {loading ? 'Регистрация...' : 'Зарегистрироваться'}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm text-gray-600">
                    Уже есть аккаунт?{' '}
                    <Link to="/login" className="text-blue-600 hover:underline">
                        Войти
                    </Link>
                </div>
            </div>
        </div>
    )
}
