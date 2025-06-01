package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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


    @Override
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

    @Override
    public Film addFilm(@RequestBody Film film) {

        film.setId(++id);
        films.put(film.getId(), film);
        log.info("Добавлен фильм {}", film);
        return film;
    }

    @Override
    public Film updateFilm(@RequestBody Film film) {

        Film existingFilm = getFilm(film.getId());

        String newName = film.getName();
        if (newName != null) {
            existingFilm.setName(newName);
        }

        String newDescription = film.getDescription();
        if (newDescription != null) {
            existingFilm.setDescription(newDescription);
        }

        LocalDate newReleaseDate = film.getReleaseDate();
        if (newReleaseDate != null) {
            existingFilm.setReleaseDate(newReleaseDate);
        }

        Integer newDuration = film.getDuration();
        if (newDuration != null && !Objects.equals(newDuration, existingFilm.getDuration())) {
            existingFilm.setDuration(newDuration);
        }

        log.info("Информация о фильме с id = {} обновлена", film.getId());
        return existingFilm;
    }
}
