package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final Date MOVIE_BIRTHDAY; //  = LocalDate.of(1895, Month.DECEMBER, 28).
    //new Date(1895, Calendar.DECEMBER, 28);
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(1895-1900, Calendar.DECEMBER, 28);
        MOVIE_BIRTHDAY = cal.getTime();
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public void addFilm(@RequestBody Film film) {

        // Название не может быть пустым;
        final String name = film.getName();
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        // Максимальная длина описания — MAX_DESCRIPTION_LENGTH символов;
        final String description = film.getDescription();
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Максимальная длина описания — " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        // Дата релиза — не раньше 28 декабря 1895 года;
        final Date releaseDate = film.getReleaseDate();
        if (releaseDate == null) {
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        checkReleaseDate(releaseDate);

        // Продолжительность фильма должна быть положительным числом.
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        // Проверка на дублирование (медленная, O(n))
        // Можно сделать нормально - хранить в Set<Film>, но в ТЗ этой проверки почему-то нет.
        // Или я просто бегу впереди паровоза.
        if (films.values().stream().anyMatch(f -> f.equals(film))) {
            throw new ValidationException("Дублирующая запись фильма");
        }

        film.setId(getFreshId());
        films.put(film.getId(), film);
    }

    private int getFreshId() {
        return films.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }

    private Date checkReleaseDate(Date releaseDate) {
        if (releaseDate.before(MOVIE_BIRTHDAY)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        return releaseDate;
    }

    @PutMapping
    public void updateFilm(@RequestBody Film film) {

        if (film.getId() == null) {
            throw new ValidationException("Не указан id фильма");
        }

        Film oldFilm = films.get(film.getId());
        if (oldFilm == null) {
            throw new ValidationException("Фильм с id = " + film.getId() + " не найден");
        }

        if (film.getName() != null)
            oldFilm.setName(film.getName());
        if (film.getDescription() != null)
            oldFilm.setDescription(film.getDescription());
        if (film.getReleaseDate() != null)
            oldFilm.setReleaseDate(checkReleaseDate(film.getReleaseDate()));
        if (film.getDuration() > 0)
            oldFilm.setDuration(film.getDuration());
    }
}
