package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreStorage genreStorage,
                       MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film addFilm(Film film) {
        validateMpa(film.getMpa().getId());
        if (film.getGenres() != null) {
            validateGenres(film.getGenres());
        }
        return filmStorage.addFilm(film);
    }

    private void validateMpa(int mpaId) {
        if (!mpaStorage.getMpaRatingById(mpaId).isPresent()) {
            throw new NotFoundException("MPA рейтинг с ID " + mpaId + " не найден");
        }
    }

    private void validateGenres(Set<Genre> genres) {
        genres.forEach(genre -> {
            if (!genreStorage.getGenreById(genre.getId()).isPresent()) {
                throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
            }
        });
    }

    public Film updateFilm(Film film) {
        validateMpa(film.getMpa().getId());
        getFilmOrThrow(film.getId());
        return filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        Film film = getFilmOrThrow(id); // Проверяем существование фильма
        Set<Genre> genres = genreStorage.getGenresByFilmId(id); // Получаем жанры
        film.setGenres(genres); // Обогащаем фильм
        return film;
    }

    public Collection<Film> getAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();
        if (films.isEmpty()) {
            return films;
        }

        // Получаем все жанры для всех фильмов одним запросом
        Map<Integer, Set<Genre>> filmGenresMap = genreStorage.getGenresForFilms(
                films.stream().map(Film::getId).collect(Collectors.toList())
        );

        // Обогащаем фильмы жанрами из мапы
        films.forEach(film -> film.setGenres(
                filmGenresMap.getOrDefault(film.getId(), Collections.emptySet())
        ));

        return films;
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        User user = getUserOrThrow(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        getUserOrThrow(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = filmStorage.getPopularFilms(count);
        if (films.isEmpty()) {
            return films;
        }

        // Загружаем жанры для всех популярных фильмов одним запросом
        Map<Integer, Set<Genre>> filmGenresMap = genreStorage.getGenresForFilms(
                films.stream().map(Film::getId).collect(Collectors.toList())
        );

        // Добавляем жанры к фильмам
        films.forEach(film -> film.setGenres(
                filmGenresMap.getOrDefault(film.getId(), Collections.emptySet())
        ));

        return films;
    }

    private Film getFilmOrThrow(int id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    private User getUserOrThrow(int id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}
