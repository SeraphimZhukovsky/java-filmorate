package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
class FilmorateApplicationTests {
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очистка данных
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM mpa_ratings");

        // Добавление тестовых MPA рейтингов
        jdbcTemplate.update("INSERT INTO mpa_ratings (id, name) VALUES (1, 'G'), (2, 'PG'), (3, 'PG-13')");
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        // Устанавливаем валидный MPA по умолчанию (ID=1 соответствует 'G')
        film.setMpa(new Mpa(1, "G"));
        return film;
    }

    @Test
    void testAddFilmWithValidMpa() {
        Film film = createTestFilm();
        Film savedFilm = filmStorage.addFilm(film);
        assertThat(savedFilm.getId()).isPositive();
    }

    @Test
    void testAddFilmWithInvalidMpaShouldFail() {
        Film film = createTestFilm();
        // Явно устанавливаем невалидный ID
        film.setMpa(new Mpa(999, "Invalid"));

        assertThatThrownBy(() -> filmStorage.addFilm(film))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("MPA rating with id 999 not found");
    }
}