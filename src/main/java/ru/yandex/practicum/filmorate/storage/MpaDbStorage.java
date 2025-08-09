package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(
                rs.getInt("id"),
                rs.getString("name")
        ));
    }

    @Override
    public Optional<Mpa> getMpaRatingById(int id) {
        Optional<Mpa> result;
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            result = Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Mpa(
                    rs.getInt("id"),
                    rs.getString("name")
            ), id));
        } catch (EmptyResultDataAccessException e) {
            result = Optional.empty();
        }
        return result;
    }
}
