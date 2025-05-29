package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.validators.FilmValidator;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    private long id;


    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film getFilm(long id) {

        Film film = films.get(id);
        if (film == null) {
            String msg = "Фильм с id = " + id + " не найден";
            log.warn(msg);
            throw new NotFoundException(msg);
        }
        return film;
    }

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
                FilmValidator.checkDescription(description);

            // Проверка даты релиза
            FilmValidator.checkReleaseDate(film.getReleaseDate());

            // Проверка продолжительности фильма
            FilmValidator.checkDuration(film.getDuration());

            // Создание нового фильма
            film.setId(++id);
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

    public Film updateFilm(@RequestBody Film film) {

        try {
            Long requestedId = film.getId();
            if (requestedId == null) {
                throw new ValidationException("Не указан id фильма");
            }

            Film existingFilm = getFilm(requestedId);

            // Вначале производим все проверки, а затем обновляем объект,
            // чтобы избежать его частичного обновления
            String newName = film.getName();
            if (newName == null) {
                newName = existingFilm.getName();
            }

            // Проверка нового описания (если задано)
            String newDescription = film.getDescription();
            if (newDescription != null && !Objects.equals(newDescription, existingFilm.getDescription())) {
                FilmValidator.checkDescription(newDescription);
            } else {
                newDescription = existingFilm.getDescription();
            }

            // Проверка новой даты релиза (если задана)
            LocalDate newReleaseDate = film.getReleaseDate();
            if (newReleaseDate != null && !Objects.equals(newReleaseDate, existingFilm.getReleaseDate())) {
                FilmValidator.checkReleaseDate(newReleaseDate);
            } else {
                newReleaseDate = existingFilm.getReleaseDate();
            }

            // Проверка новой длительности (если задана)
            Integer newDuration = film.getDuration();
            if (newDuration != null && !Objects.equals(newDuration, existingFilm.getDuration())) {
                FilmValidator.checkDuration(newDuration);
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

}
