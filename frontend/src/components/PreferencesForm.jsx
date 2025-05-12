// src/components/PreferencesForm.jsx

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    getTeacherPreferences,
    saveTeacherPreferences,
    logout
} from '../api';

import roomsData from '../data/rooms.json';

export default function PreferencesForm() {
    const { type } = useParams(); // "semester" или "session"
    const navigate = useNavigate();
    const isSemester = type === 'semester';

    // Конфиг кнопок — подтягиваем из adminConfig, если есть
    const [config, setConfig] = useState({
        semesterButtonText: 'Пожелания к семестру',
        sessionButtonText: 'Пожелания к сессии'
    });

    useEffect(() => {
        const saved = localStorage.getItem('adminConfig');
        if (saved) {
            try {
                const parsed = JSON.parse(saved);
                // берём только нужные поля
                setConfig(cfg => ({
                    ...cfg,
                    semesterButtonText: parsed.semesterButtonText || cfg.semesterButtonText,
                    sessionButtonText: parsed.sessionButtonText || cfg.sessionButtonText
                }));
            } catch {
                // если парсинг упал — игнорируем
            }
        }
    }, []);

    // Состояния формы
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [entries, setEntries] = useState([]);

    // Словарь вариантов "Корпус/Аудитория"
    const [rooms, setRooms] = useState([]);

    // Пустая запись
    function createEmpty() {
        return {
            subject: '', groups: '',
            days: [], daysPriority: '',
            times: '', timesPriority: '',
            preferredDates: '', preferredDatesPriority: '',
            avoidDates: '', avoidDatesPriority: '',
            newYearPref: '', newYearPrefPriority: '',
            loadType: '', loadTypePriority: '',
            buildingRoom: '', buildingRoomPriority: '',
            boardType: '', boardTypePriority: '',
            computers: [], computersPriority: '',
            format: '', formatPriority: '',
            comments: '', commentsPriority: '',
            showRooms: false
        };
    }

    useEffect(() => {
        async function init() {
            setLoading(true);
            // rooms
            const combo = Array.from(new Set(
                roomsData.map(r => `${r.building}/${r.room}`)
            ));
            setRooms(combo);

            // prefs
            try {
                const prefs = await getTeacherPreferences(type);
                setEntries(Array.isArray(prefs) && prefs.length
                    ? prefs
                    : [createEmpty()]
                );
            } catch (err) {
                if (err.message.includes('401')) {
                    logout();
                    navigate('/login', { replace: true });
                } else {
                    setError(err.message);
                }
            } finally {
                setLoading(false);
            }
        }
        init();
    }, [type, navigate]);

    // обновление полей и чекбоксов — без изменений
    const updateField = (i, field, val) =>
        setEntries(es => es.map((e, idx) => idx === i ? { ...e, [field]: val } : e));
    const toggleComputer = (i, os) =>
        setEntries(es => es.map((e, idx) => {
            if (idx !== i) return e;
            const has = e.computers.includes(os);
            return {
                ...e,
                computers: has
                    ? e.computers.filter(x => x !== os)
                    : [...e.computers, os]
            };
        }));
    const toggleDay = (i, day) =>
        setEntries(es => es.map((e, idx) => {
            if (idx !== i) return e;
            const has = e.days.includes(day);
            return {
                ...e,
                days: has
                    ? e.days.filter(d => d !== day)
                    : [...e.days, day]
            };
        }));

    const addEntry = () => setEntries(es => [...es, createEmpty()]);
    const removeEntry = i => setEntries(es => es.filter((_, idx) => idx !== i));
    const copyPrev = i => {
        if (i === 0) return;
        setEntries(es => es.map((ent, idx) => idx === i ? { ...es[i - 1] } : ent));
    };

    const handleSubmit = async e => {
        e.preventDefault();
        setError(''); setSuccess(''); setLoading(true);

        const payload = entries.map(ent => ({
            ...ent,
            type,
            days: ent.days,
            daysPriority: ent.daysPriority ? +ent.daysPriority : null,
            timesPriority: ent.timesPriority ? +ent.timesPriority : null,
            preferredDatesPriority: ent.preferredDatesPriority ? +ent.preferredDatesPriority : null,
            avoidDatesPriority: ent.avoidDatesPriority ? +ent.avoidDatesPriority : null,
            newYearPrefPriority: ent.newYearPrefPriority ? +ent.newYearPrefPriority : null,
            loadTypePriority: ent.loadTypePriority ? +ent.loadTypePriority : null,
            buildingRoomPriority: ent.buildingRoomPriority ? +ent.buildingRoomPriority : null,
            boardTypePriority: ent.boardTypePriority ? +ent.boardTypePriority : null,
            computersPriority: ent.computersPriority ? +ent.computersPriority : null,
            formatPriority: ent.formatPriority ? +ent.formatPriority : null,
            commentsPriority: ent.commentsPriority ? +ent.commentsPriority : null
        }));

        try {
            const saved = await saveTeacherPreferences(type, payload);
            setEntries(saved);
            setSuccess('✅ Ваши пожелания успешно сохранены');
        } catch (err) {
            if (err.message.includes('401')) {
                logout();
                navigate('/login', { replace: true });
            } else {
                setError(err.message);
            }
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <p>Загрузка…</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-white p-4">
            <div className="max-w-4xl mx-auto bg-white shadow-md rounded-xl p-6">
                {/* Заголовок + Назад */}
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold">
                        {isSemester
                            ? config.semesterButtonText
                            : config.sessionButtonText}
                    </h2>
                    <button
                        type="button"
                        onClick={() => navigate(-1)}
                        className="text-blue-600 hover:underline"
                    >
                        ← Назад
                    </button>
                </div>

                {error && <div className="mb-4 text-center text-red-600">{error}</div>}
                {success && <div className="mb-4 text-center text-green-700">{success}</div>}

                <form onSubmit={handleSubmit} className="space-y-8">
                    {entries.map((e, idx) => (
                        <div key={idx} className="relative border rounded-lg p-4 bg-gray-50">
                            {/* Кнопки копировать/удалить */}
                            <div className="flex justify-between mb-4">
                                <div>
                                    {idx > 0 && (
                                        <button
                                            type="button"
                                            onClick={() => copyPrev(idx)}
                                            className="text-blue-600 hover:underline mr-4"
                                        >
                                            Копировать предыдущий
                                        </button>
                                    )}
                                </div>
                                <div>
                                    {entries.length > 1 && (
                                        <button
                                            type="button"
                                            onClick={() => removeEntry(idx)}
                                            className="text-red-600 hover:underline"
                                        >
                                            ✕
                                        </button>
                                    )}
                                </div>
                            </div>

                            {/* Предмет + Группы */}
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label className="block font-medium">Название предмета</label>
                                    <input
                                        type="text"
                                        className="w-full border px-2 py-1 rounded"
                                        value={e.subject}
                                        onChange={v => updateField(idx, 'subject', v.target.value)}
                                    />
                                </div>
                                <div>
                                    <label className="block font-medium">Группы (через запятую)</label>
                                    <input
                                        type="text"
                                        className="w-full border px-2 py-1 rounded"
                                        value={e.groups}
                                        onChange={v => updateField(idx, 'groups', v.target.value)}
                                    />
                                </div>
                            </div>

                            {/* Семестровые или сессионные поля */}
                            {isSemester ? (
                                <div className="mt-4 grid grid-cols-3 gap-4">
                                    {/* дни */}
                                    <div className="col-span-2">
                                        <label className="block font-medium">Нежелательные дни</label>
                                        <div className="flex flex-wrap gap-4 mt-1">
                                            {['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'].map(day => (
                                                <label key={day} className="inline-flex items-center mr-4">
                                                    <input
                                                        type="checkbox"
                                                        className="mr-1"
                                                        checked={e.days.includes(day)}
                                                        onChange={() => toggleDay(idx, day)}
                                                    />
                                                    {day}
                                                </label>
                                            ))}
                                        </div>
                                    </div>
                                    <div>
                                        <label className="block font-medium">Приоритет</label>
                                        <select
                                            className="w-full border px-2 py-1 rounded"
                                            value={e.daysPriority}
                                            onChange={v => updateField(idx, 'daysPriority', v.target.value)}
                                        >
                                            <option value="">—</option>
                                            {[1, 2, 3, 4, 5].map(n => (
                                                <option key={n} value={n}>{n}</option>
                                            ))}
                                        </select>
                                    </div>
                                    {/* время */}
                                    <div className="col-span-2">
                                        <label className="block font-medium">Нежелательное время</label>
                                        <input
                                            type="text"
                                            className="w-full border px-2 py-1 rounded"
                                            value={e.times}
                                            onChange={v => updateField(idx, 'times', v.target.value)}
                                        />
                                    </div>
                                    <div>
                                        <label className="block font-medium">Приоритет</label>
                                        <select
                                            className="w-full border px-2 py-1 rounded"
                                            value={e.timesPriority}
                                            onChange={v => updateField(idx, 'timesPriority', v.target.value)}
                                        >
                                            <option value="">—</option>
                                            {[1, 2, 3, 4, 5].map(n => (
                                                <option key={n} value={n}>{n}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                            ) : (
                                <div className="mt-4 grid grid-cols-3 gap-4">
                                    {/* предпочтительные даты */}
                                    <div className="col-span-2">
                                        <label className="block font-medium">Предпочтительные даты</label>
                                        <input
                                            type="text"
                                            className="w-full border px-2 py-1 rounded"
                                            value={e.preferredDates}
                                            onChange={v => updateField(idx, 'preferredDates', v.target.value)}
                                        />
                                    </div>
                                    <div>
                                        <label className="block font-medium">Приоритет</label>
                                        <select
                                            className="w-full border px-2 py-1 rounded"
                                            value={e.preferredDatesPriority}
                                            onChange={v => updateField(idx, 'preferredDatesPriority', v.target.value)}
                                        >
                                            <option value="">—</option>
                                            {[1, 2, 3, 4, 5].map(n => (
                                                <option key={n} value={n}>{n}</option>
                                            ))}
                                        </select>
                                    </div>
                                    {/* avoidDates */}
                                    <div className="col-span-2">
                                        <label className="block font-medium">Даты, в которые НЕ ставить</label>
                                        <input
                                            type="text"
                                            className="w-full border px-2 py-1 rounded"
                                            value={e.avoidDates}
                                            onChange={v => updateField(idx, 'avoidDates', v.target.value)}
                                        />
                                    </div>
                                    <div>
                                        <label className="block font-medium">Приоритет</label>
                                        <select
                                            className="w-full border px-2 py-1.rounded"
                                            value={e.avoidDatesPriority}
                                            onChange={v => updateField(idx, 'avoidDatesPriority', v.target.value)}
                                        >
                                            <option value="">—</option>
                                            {[1, 2, 3, 4, 5].map(n => (
                                                <option key={n} value={n}>{n}</option>
                                            ))}
                                        </select>
                                    </div>
                                    {/* newYear */}
                                    <div className="col-span-2">
                                        <label className="block font-medium">Пожелания до/после Нового года</label>
                                        <input
                                            type="text"
                                            className="w-full border px-2 py-1.rounded"
                                            value={e.newYearPref}
                                            onChange={v => updateField(idx, 'newYearPref', v.target.value)}
                                        />
                                    </div>
                                    <div>
                                        <label className="block font-medium">Приоритет</label>
                                        <select
                                            className="w-full border px-2 py-1.rounded"
                                            value={e.newYearPrefPriority}
                                            onChange={v => updateField(idx, 'newYearPrefPriority', v.target.value)}
                                        >
                                            <option value="">—</option>
                                            {[1, 2, 3, 4, 5].map(n => (
                                                <option key={n} value={n}>{n}</option>
                                            ))}
                                        </select>
                                    </div>
                                </div>
                            )}

                            {/* Общие поля в нужном порядке */}
                            {/* 1) Корпус/аудитория */}
                            <div className="mt-4 grid grid-cols-3 gap-4">
                                <div className="col-span-2 relative">
                                    <label className="block font-medium">Корпус/аудитория</label>
                                    <input
                                        type="text"
                                        className="w-full border px-2 py-1 rounded"
                                        value={e.buildingRoom}
                                        onChange={v => {
                                            updateField(idx, 'buildingRoom', v.target.value);
                                            updateField(idx, 'showRooms', true);
                                        }}
                                        onFocus={() => updateField(idx, 'showRooms', true)}
                                        onBlur={() => {
                                            // небольшая задержка, чтобы клик по опции успел сработать
                                            setTimeout(() => updateField(idx, 'showRooms', false), 100);
                                        }}
                                    />
                                    {e.showRooms && (
                                        <ul className="absolute left-0 right-0 mt-1 bg-white border rounded max-h-48 overflow-y-auto z-10">
                                            {rooms
                                                .filter(r =>
                                                    r.toLowerCase().includes(e.buildingRoom.toLowerCase())
                                                )
                                                .map(r => (
                                                    <li
                                                        key={r}
                                                        className="px-2 py-1 hover:bg-gray-200 cursor-pointer"
                                                        onMouseDown={() => {
                                                            // onMouseDown, чтобы сработало до onBlur у инпута
                                                            updateField(idx, 'buildingRoom', r);
                                                            updateField(idx, 'showRooms', false);
                                                        }}
                                                    >
                                                        {r}
                                                    </li>
                                                ))
                                            }
                                        </ul>
                                    )}
                                </div>

                                <div>
                                    <label className="block font-medium">Приоритет</label>
                                    <select
                                        className="w-full border px-2 py-1 rounded"
                                        value={e.buildingRoomPriority}
                                        onChange={v => updateField(idx, 'buildingRoomPriority', v.target.value)}
                                    >
                                        <option value="">—</option>
                                        {[1, 2, 3, 4, 5].map(n => (
                                            <option key={n} value={n}>{n}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            {/* 2) Компьютеры */}
                            <div className="mt-4 grid grid-cols-3 gap-4">
                                <div className="col-span-2">
                                    <label className="block font-medium">Компьютеры</label>
                                    <div className="flex space-x-4 mt-1">
                                        {['Windows', 'Linux'].map(os => (
                                            <label key={os} className="inline-flex items-center">
                                                <input
                                                    type="checkbox"
                                                    className="mr-1"
                                                    checked={e.computers.includes(os)}
                                                    onChange={() => toggleComputer(idx, os)}
                                                />
                                                {os}
                                            </label>
                                        ))}
                                    </div>
                                </div>
                                <div>
                                    <label className="block font-medium">Приоритет</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.computersPriority}
                                        onChange={v => updateField(idx, 'computersPriority', v.target.value)}
                                    >
                                        <option value="">—</option>
                                        {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                                    </select>
                                </div>
                            </div>

                            {/* 3) Тип нагрузки */}
                            <div className="mt-4 grid grid-cols-3 gap-4">
                                <div className="col-span-2">
                                    <label className="block font-medium">Тип нагрузки</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.loadType}
                                        onChange={v => updateField(idx, 'loadType', v.target.value)}
                                    >
                                        <option value="">— выберите —</option>
                                        <option value="compact">Компактно</option>
                                        <option value="even">Равномерно</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block font-medium">Приоритет</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.loadTypePriority}
                                        onChange={v => updateField(idx, 'loadTypePriority', v.target.value)}
                                    >
                                        <option value="">—</option>
                                        {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                                    </select>
                                </div>
                            </div>

                            {/* 4) Тип доски */}
                            <div className="mt-4 grid grid-cols-3 gap-4">
                                <div className="col-span-2">
                                    <label className="block font-medium">Тип доски</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.boardType}
                                        onChange={v => updateField(idx, 'boardType', v.target.value)}
                                    >
                                        <option value="">— выберите —</option>
                                        <option value="marker">Маркер</option>
                                        <option value="chalk">Мел</option>
                                        <option value="digital">Цифровая</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block font-medium">Приоритет</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.boardTypePriority}
                                        onChange={v => updateField(idx, 'boardTypePriority', v.target.value)}
                                    >
                                        <option value="">—</option>
                                        {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                                    </select>
                                </div>
                            </div>

                            {/* 5) Формат занятий */}
                            <div className="mt-4 grid grid-cols-3 gap-4">
                                <div className="col-span-2">
                                    <label className="block font-medium">Формат занятий</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.format}
                                        onChange={v => updateField(idx, 'format', v.target.value)}
                                    >
                                        <option value="">— выберите —</option>
                                        <option value="in-person">Очно</option>
                                        <option value="remote">Дистанционно</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block font-medium">Приоритет</label>
                                    <select
                                        className="w-full border px-2 py-1.rounded"
                                        value={e.formatPriority}
                                        onChange={v => updateField(idx, 'formatPriority', v.target.value)}
                                    >
                                        <option value="">—</option>
                                        {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                                    </select>
                                </div>
                            </div>

                            {/* 6) Комментарии */}
                            <div className="mt-4 grid grid-cols-3 gap-4">
                                <div className="col-span-2">
                                    <label className="block font-medium">Комментарии</label>
                                    <textarea
                                        className="w-full border px-2 py-1.rounded"
                                        rows={3}
                                        value={e.comments}
                                        onChange={v => updateField(idx, 'comments', v.target.value)}
                                    />
                                </div>
                                <div>
                                    <label className="block font-medium">Приоритет</label>
                                    <select
                                        className="w-full border px-2 py-1 rounded"
                                        value={e.commentsPriority}
                                        onChange={v => updateField(idx, 'commentsPriority', v.target.value)}
                                    >
                                        <option value="">—</option>
                                        {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                                    </select>
                                </div>
                            </div>
                        </div>
                    ))}

                    {/* Кнопки */}
                    <div className="flex justify-between items-center">
                        <button
                            type="button"
                            onClick={addEntry}
                            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
                        >
                            Добавить ещё
                        </button>
                        <div>
                            <button
                                type="submit"
                                disabled={loading}
                                className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                {loading ? 'Сохраняем…' : 'Сохранить'}
                            </button>
                            <button
                                type="button"
                                onClick={() => navigate(-1)}
                                className="ml-4 text-blue-600 hover:underline"
                            >
                                ← Назад
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
