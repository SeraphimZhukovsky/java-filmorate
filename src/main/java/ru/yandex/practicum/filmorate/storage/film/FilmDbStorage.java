package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        validateMpa(film.getMpa().getId());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " + "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateFilmGenres(film);
        return film;
    }

    private void validateMpa(int mpaId) {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa_ratings WHERE id = ?", Integer.class, mpaId);

            if (count == null || count == 0) {
                throw new DataIntegrityViolationException("MPA rating with id " + mpaId + " not found");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new DataIntegrityViolationException("MPA rating with id " + mpaId + " not found");
        }
    }

    @Override
    public Film updateFilm(Film film) {
        validateMpa(film.getMpa().getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " + "WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());

        updateFilmGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            if (film != null) {
                film.setGenres(getFilmGenres(id));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        films.forEach(film -> film.setGenres(getFilmGenres(film.getId())));
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = mapRowToFilm(rs, rowNum);
            return film;
        }, count);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_rating_id"), rs.getString("mpa_name")));
        return film;
    }

    private Set<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Genre(rs.getInt("id"), rs.getString("name")),
                filmId));
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            for (Genre genre : film.getGenres()) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }
}