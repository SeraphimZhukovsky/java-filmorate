package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaDto getMpaRatingById(int id) {
        return mpaStorage.getMpaRatingById(id)
                .map(mpa -> new MpaDto(mpa.getId(), mpa.getName()))
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с ID " + id + " не найден"));
    }

    public List<MpaDto> getAllMpaRatings() {
        return mpaStorage.getAllMpaRatings().stream()
                .map(mpa -> new MpaDto(mpa.getId(), mpa.getName()))
                .collect(Collectors.toList());
    }
}
