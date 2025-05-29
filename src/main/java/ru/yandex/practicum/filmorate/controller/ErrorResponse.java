package ru.yandex.practicum.filmorate.controller;

import lombok.Getter;

@SuppressWarnings("ClassCanBeRecord")
@Getter
public class ErrorResponse {

    private final String error;

    public ErrorResponse(String error) {
        this.error = error;
    }
}
