package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<GenreDto> getAllGenres() {
        return genreStorage.getAllGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .collect(Collectors.toList());
    }

    public GenreDto getGenreById(int id) {
        Genre genre = genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с ID " + id + " не найден"));
        return new GenreDto(genre.getId(), genre.getName());
    }
}