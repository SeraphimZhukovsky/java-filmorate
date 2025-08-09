package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, TestConfig.class}) // TestConfig для инициализации данных
class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(new Mpa(1, "G"));
    }

    @Test
    void addFilm_ShouldSaveFilmWithGeneratedId() {
        Film savedFilm = filmStorage.addFilm(testFilm);
        assertThat(savedFilm.getId()).isPositive();
    }

    @Test
    void getFilm_ShouldReturnSavedFilm() {
        Film savedFilm = filmStorage.addFilm(testFilm);
        Optional<Film> foundFilm = filmStorage.getFilm(savedFilm.getId());
        assertThat(foundFilm).isPresent().contains(savedFilm);
    }

    @Test
    void getPopularFilms_ShouldReturnOrderedByLikes() {
        Film film1 = filmStorage.addFilm(testFilm);
        Film film2 = filmStorage.addFilm(createAnotherFilm());
        filmStorage.addLike(film1.getId(), 1);
        filmStorage.addLike(film1.getId(), 2);
        filmStorage.addLike(film2.getId(), 1);
        var popularFilms = filmStorage.getPopularFilms(2);
        assertThat(popularFilms).extracting(Film::getId).containsExactly(film1.getId(), film2.getId());
    }

    private Film createAnotherFilm() {
        Film film = new Film();
        film.setName("Another Film");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(90);
        film.setMpa(new Mpa(2, "PG"));
        return film;
    }
}