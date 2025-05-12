// src/components/AdminDashboard.jsx

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    getAllPreferences,
    exportPreferencesToExcel,
    logout,
    register as apiRegister
} from '../api';

export default function AdminDashboard() {
    const navigate = useNavigate();

    // Конфиг главной страницы
    const defaultConfig = {
        semesterButtonText: 'Пожелания к семестру',
        semesterDeadline: '',
        sessionButtonText: 'Пожелания к сессии',
        sessionDeadline: ''
    };
    const [config, setConfig] = useState(defaultConfig);

    // Состояние списка пожеланий
    const [prefsSemester, setPrefsSemester] = useState([]);
    const [prefsSession, setPrefsSession] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Состояние формы регистрации
    const [newFullName, setNewFullName] = useState('');
    const [newLogin, setNewLogin] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [regError, setRegError] = useState('');
    const [regSuccess, setRegSuccess] = useState('');
    const [regLoading, setRegLoading] = useState(false);

    // Загрузка данных при монтировании
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) {
            navigate('/login', { replace: true });
            return;
        }
        const saved = localStorage.getItem('adminConfig');
        if (saved) {
            try { setConfig(JSON.parse(saved)); }
            catch { /* ignore */ }
        }

        (async () => {
            try {
                const all = await getAllPreferences();
                setPrefsSemester(all.filter(p => p.type === 'semester'));
                setPrefsSession(all.filter(p => p.type === 'session'));
            } catch (e) {
                setError(e.message || 'Ошибка загрузки данных');
            } finally {
                setLoading(false);
            }
        })();
    }, [navigate]);

    // Сохранить конфиг
    const handleConfigSave = () => {
        localStorage.setItem('adminConfig', JSON.stringify(config));
        alert('Настройки сохранены');
    };

    // Выход
    const handleLogout = () => {
        logout();
        navigate('/login', { replace: true });
    };

    // Скачать Excel
    const downloadExcel = async () => {
        try {
            await exportPreferencesToExcel();
        } catch (e) {
            alert('Не удалось скачать: ' + e.message);
        }
    };

    // Отправить форму регистрации
    const handleRegister = async (e) => {
        e.preventDefault();
        setRegError('');
        setRegSuccess('');
        if (!newFullName || !newLogin || newPassword.length < 6) {
            setRegError('Заполните все поля, пароль ≥ 6 символов');
            return;
        }
        setRegLoading(true);
        try {
            await apiRegister({ fullName: newFullName, login: newLogin, password: newPassword });
            setRegSuccess(`Пользователь «${newLogin}» успешно создан`);
            setNewFullName(''); setNewLogin(''); setNewPassword('');
        } catch (err) {
            setRegError(err.message || 'Ошибка при создании пользователя');
        } finally {
            setRegLoading(false);
        }
    };

    if (loading) {
        return <p>Загрузка данных…</p>;
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white p-4">
            <div className="max-w-5xl mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-2xl font-bold">Панель администратора</h1>
                    <div className="space-x-4">
                        <button
                            onClick={handleLogout}
                            className="text-red-600 hover:underline"
                        >
                            Выйти
                        </button>
                    </div>
                </div>

                

                {/* Настройки главной страницы */}
                <div className="bg-white p-6 rounded-lg shadow mb-8">
                    <h2 className="text-xl font-semibold mb-4">Настройки главной страницы</h2>
                    <div className="grid gap-4 md:grid-cols-2">
                        <div>
                            <label className="block text-gray-700 mb-1">
                                Текст кнопки «{config.semesterButtonText}»
                            </label>
                            <input
                                type="text"
                                className="w-full border px-3 py-2 rounded"
                                value={config.semesterButtonText}
                                onChange={e => setConfig({ ...config, semesterButtonText: e.target.value })}
                            />
                        </div>
                        <div>
                            <label className="block text-gray-700 mb-1">Дедлайн:</label>
                            <input
                                type="date"
                                className="w-full border px-3 py-2 rounded"
                                value={config.semesterDeadline}
                                onChange={e => setConfig({ ...config, semesterDeadline: e.target.value })}
                            />
                        </div>
                        <div>
                            <label className="block text-gray-700 mb-1">
                                Текст кнопки «{config.sessionButtonText}»
                            </label>
                            <input
                                type="text"
                                className="w-full border px-3 py-2 rounded"
                                value={config.sessionButtonText}
                                onChange={e => setConfig({ ...config, sessionButtonText: e.target.value })}
                            />
                        </div>
                        <div>
                            <label className="block text-gray-700 mb-1">Дедлайн:</label>
                            <input
                                type="date"
                                className="w-full border px-3 py-2 rounded"
                                value={config.sessionDeadline}
                                onChange={e => setConfig({ ...config, sessionDeadline: e.target.value })}
                            />
                        </div>
                    </div>
                    <button
                        onClick={handleConfigSave}
                        className="mt-6 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                    >
                        Сохранить настройки
                    </button>
                </div>

                {/* Списки пожеланий */}
                {error && <div className="text-red-600 mb-4">{error}</div>}
                <div className="bg-white p-6 rounded-lg shadow mb-8">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-xl font-semibold">Пожелания преподавателей</h2>
                        <button
                            onClick={downloadExcel}
                            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 transition"
                        >
                            Скачать все в Excel
                        </button>
                    </div>
                    <h3 className="font-medium mb-2">Семестр</h3>
                    <ul className="list-disc list-inside text-sm mb-6">
                        {prefsSemester.map((p, i) => (
                            <li key={i}>
                                <strong>{p.teacherName}</strong> — {p.subject} — группы: {p.groups}
                            </li>
                        ))}
                    </ul>
                    <h3 className="font-medium mb-2">Сессия</h3>
                    <ul className="list-disc list-inside text-sm">
                        {prefsSession.map((p, i) => (
                            <li key={i}>
                                <strong>{p.teacherName}</strong> — {p.subject} — группы: {p.groups}
                            </li>
                        ))}
                    </ul>
                </div>
                {/* Форма регистрации нового преподавателя */}
                <div className="bg-white p-6 rounded-lg shadow mb-8">
                    <h2 className="text-xl font-semibold mb-4">Регистрация нового пользователя</h2>
                    {regError && <div className="text-red-600 mb-2">{regError}</div>}
                    {regSuccess && <div className="text-green-700 mb-2">{regSuccess}</div>}
                    <form onSubmit={handleRegister} className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <input
                            type="text"
                            placeholder="ФИО"
                            value={newFullName}
                            onChange={e => setNewFullName(e.target.value)}
                            className="border px-3 py-2 rounded"
                            required
                        />
                        <input
                            type="text"
                            placeholder="Логин"
                            value={newLogin}
                            onChange={e => setNewLogin(e.target.value)}
                            className="border px-3 py-2 rounded"
                            required
                        />
                        <input
                            type="password"
                            placeholder="Пароль"
                            value={newPassword}
                            onChange={e => setNewPassword(e.target.value)}
                            className="border px-3 py-2 rounded"
                            minLength={6}
                            required
                        />
                        <div className="md:col-span-3 flex justify-end">
                            <button
                                type="submit"
                                disabled={regLoading}
                                className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition"
                            >
                                {regLoading ? 'Создание…' : 'Создать'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
