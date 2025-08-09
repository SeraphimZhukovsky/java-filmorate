package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> getFilm(int id);

    Collection<Film> getAllFilms();

    List<Film> getPopularFilms(int count);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

}
