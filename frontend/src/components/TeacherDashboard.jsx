// src/components/TeacherDashboard.jsx

import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function TeacherDashboard() {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [config, setConfig] = useState({
        semesterButtonText: 'Пожелания к семестру',
        semesterDeadline: '',
        sessionButtonText: 'Пожелания к сессии',
        sessionDeadline: ''
    });

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (!storedUser) {
            navigate('/login', { replace: true });
            return;
        }
        setUser(JSON.parse(storedUser));

        const saved = localStorage.getItem('adminConfig');
        if (saved) {
            try { setConfig(JSON.parse(saved)); }
            catch { }
        }
    }, [navigate]);

    if (!user) return null;

    const now = new Date();
    const semDeadline = config.semesterDeadline ? new Date(config.semesterDeadline) : null;
    const sesDeadline = config.sessionDeadline ? new Date(config.sessionDeadline) : null;
    const semExpired = semDeadline && semDeadline < now;
    const sesExpired = sesDeadline && sesDeadline < now;

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login', { replace: true });
    };

    const baseBtn = 'block p-6 rounded-2xl shadow-lg text-white transition hover:opacity-90';

    const ExpiredBadge = () => (
        <span className="ml-2 inline-block bg-red-100 text-red-700 text-xs font-semibold px-2 py-0.5 rounded">
            Просрочено
        </span>
    );

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white p-4">
            <div className="max-w-3xl mx-auto">
                <header className="flex justify-between items-center mb-8">
                    <h1 className="text-3xl font-bold text-gray-800">
                        Здравствуйте, {user.fullName}!
                    </h1>
                    <button onClick={handleLogout} className="text-red-600 hover:underline">
                        Выйти
                    </button>
                </header>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    {/* Семестр */}
                    <Link
                        to={semExpired ? '#' : '/teacher/semester'}
                        onClick={e => {
                            if (semExpired) {
                                e.preventDefault();
                                alert('Срок подачи заявок на семестр истёк');
                            }
                        }}
                        className={`${baseBtn} bg-blue-600`}
                    >
                        <h2 className="text-xl font-semibold mb-2">
                            {config.semesterButtonText}
                        </h2>
                        {semDeadline && (
                            <p className="text-sm text-gray-100 flex items-center">
                                до {semDeadline.toLocaleDateString('ru-RU')}
                                {semExpired && <ExpiredBadge />}
                            </p>
                        )}
                    </Link>

                    {/* Сессия */}
                    <Link
                        to={sesExpired ? '#' : '/teacher/session'}
                        onClick={e => {
                            if (sesExpired) {
                                e.preventDefault();
                                alert('Срок подачи заявок на сессию истёк');
                            }
                        }}
                        className={`${baseBtn} bg-green-600`}
                    >
                        <h2 className="text-xl font-semibold mb-2">
                            {config.sessionButtonText}
                        </h2>
                        {sesDeadline && (
                            <p className="text-sm text-gray-100 flex items-center">
                                до {sesDeadline.toLocaleDateString('ru-RU')}
                                {sesExpired && <ExpiredBadge />}
                            </p>
                        )}
                    </Link>
                </div>
            </div>
        </div>
    );
}
