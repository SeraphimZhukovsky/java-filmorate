package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilm(film.getId()) == null) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        Film film = filmStorage.getFilm(id);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return film;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilm(filmId);
        // Проверяем существование пользователя через userStorage
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilm(filmId);
        // Проверяем существование пользователя через userStorage
        if (userStorage.getUser(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        List<Film> films = new ArrayList<>(filmStorage.getAllFilms());
        films.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));

        return films.subList(0, Math.min(count, films.size()));
    }
}
