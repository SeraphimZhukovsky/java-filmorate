package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class MpaDto {
    private int id;
    private String name;

    public MpaDto(int id, String name) {
        this.id = id;
        this.name = name;
    }
}