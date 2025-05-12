package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Film.
 */
@Getter
@Setter
// Фильм однозначно определяется названием, датой релиза и длиной
@EqualsAndHashCode(of = {"name", "releaseDate", "duration"})
public class Film {
    private Integer id;
    private String name;
    private String description;
    private Date releaseDate;
    private int duration;
}
