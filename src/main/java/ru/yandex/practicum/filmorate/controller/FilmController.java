package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    // Максимальная длина описания фильма
    static final int MAX_DESCRIPTION_LENGTH = 200;

    // Минимальная дата релиза фильма
    static final LocalDate MOVIE_BIRTHDAY = LocalDate.of(1895, 12, 28);


    /**
     * Возвращает новый id фильма
     * @return Новый id фильма
     */
    private int getFreshId() {
        return films.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
    }


    // region Checkers

    /**
     * Проверка даты релиза
     * @param releaseDate Дата релиза
     * @throws ValidationException Дата релиза не указана или некорректна
     * @apiNote Дата релиза — не раньше 28 декабря 1895 года;
     */
    private void checkReleaseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            throw new ValidationException("Не указана дата релиза");
        }
        if (releaseDate.isBefore(MOVIE_BIRTHDAY)) {
            throw new ValidationException("Некорректная дата релиза: " + releaseDate);
        }
    }

    /**
     * Проверка описания фильма
     * @param description Описание
     * @throws ValidationException если длина описания превышает заданное число символов
     */
    private void checkDescription(String description) {

        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Слишком длинное описание (не более " +
                    MAX_DESCRIPTION_LENGTH + " символов)");
        }
    }

    /**
     * Проверка длительности фильма
     * @param duration Длительность фильма в минутах
     * @throws ValidationException если задана нулевая или отрицательная длительность
     */
    private void checkDuration(Integer duration) {
        if (duration == null) {
            throw new ValidationException("Не указана длительность фильма");
        }
        if (duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    // endregion


    // region Mapping

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {

        try {
            // Название не может быть пустым;
            final String name = film.getName();
            if (name == null || name.isBlank()) {
                throw new ValidationException("Название фильма не может быть пустым");
            }

            // Проверка описания
            final String description = film.getDescription();
            if (description != null)
                checkDescription(description);

            // Проверка даты релиза
            checkReleaseDate(film.getReleaseDate());

            // Проверка продолжительности фильма
            checkDuration(film.getDuration());

            // Создание нового фильма
            film.setId(getFreshId());
            films.put(film.getId(), film);
            log.info("Добавлен фильм {}", film);
            return film;

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {

        try {
            if (film.getId() == null) {
                throw new ValidationException("Не указан id фильма");
            }

            Film existingFilm = films.get(film.getId());
            if (existingFilm == null) {
                throw new ValidationException("Фильм с id = " + film.getId() + " не найден");
            }

            // Вначале производим все проверки, а затем обновляем объект,
            // чтобы избежать его частичного обновления
            String newName = film.getName();
            if (newName == null) {
                newName = existingFilm.getName();
            }

            // Проверка нового описания (если задано)
            String newDescription = film.getDescription();
            if (newDescription != null && !Objects.equals(newDescription, existingFilm.getDescription())) {
                checkDescription(newDescription);
            } else {
                newDescription = existingFilm.getDescription();
            }

            // Проверка новой даты релиза (если задана)
            LocalDate newReleaseDate = film.getReleaseDate();
            if (newReleaseDate != null && !Objects.equals(newReleaseDate, existingFilm.getReleaseDate())) {
                checkReleaseDate(newReleaseDate);
            } else {
                newReleaseDate = existingFilm.getReleaseDate();
            }

            // Проверка новой длительности (если задана)
            Integer newDuration = film.getDuration();
            if (newDuration != null && !Objects.equals(newDuration, existingFilm.getDuration())) {
                checkDuration(newDuration);
            } else {
                newDuration = existingFilm.getDuration();
            }

            // Все проверки пройдены - можно обновляться
            existingFilm.setName(newName);
            existingFilm.setDescription(newDescription);
            existingFilm.setReleaseDate(newReleaseDate);
            existingFilm.setDuration(newDuration);
            log.info("Информация о фильме с id = {} обновлена", film.getId());
            return existingFilm;

        } catch (ValidationException ve) {
            log.warn(ve.getMessage());
            throw ve;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    // endregion
}
