# java-filmorate
#### Template repository for Filmorate project.

---
<details>
  <summary><h2> ER Диаграмма </h2></summary>

#### Ссылка на структуру базы данных для приложения Filmorate: \scr\main\resources\sql\bddiagram.png
![bddiagram](/src/main/resources/sql/bddiagram.png)
</details>

<details>
  <summary><h2> endpointFilmorate </h2></summary>

#### Ссылка на структуру эндпоинтов для приложения Filmorate: \src\main\resources\endpointsPNG\endpointFilmorate.png
![endpoints](/src/main/resources/endpointsPNG/endpointFilmorate.png)
</details>

<details>
  <summary><h2> Примеры запросов </h2></summary>

#### Получить всех пользователей
http://localhost:8080/users

#### Получить пользователя с id 1
http://localhost:8080/users/1

#### Получить общих друзей пользователя с id 1 и с id 2
http://localhost:8080/users/1/friends/common/2

#### Получить все фильмы
http://localhost:8080/films

#### Получить фильм с id 1
http://localhost:8080/films/1

#### Возрат списка первых по количеству лайков count фильмов
http://localhost:8080/films/popular?count={count}
</details>

<details>
  <summary><h2> Примеры запросов SQL </h2></summary>

```h2
-- добавление записи в таблицу USERS:
INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?);

-- обновление данных в таблице USERS:
UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?;

-- добавление записи в таблицу FILMS:
INSERT INTO films (name, description, release_date, duration) values (?, ?, ?, ?);

-- обновление данных в таблице FILMS:
UPDATE films SET name = ?, description = ?, duration = ?, release_date = ? WHERE id = ?;

-- добавление записи в таблицу GENRES:
INSERT INTO genres (name) VALUES (?);

-- добавление записи в таблицу MPAS:
INSERT INTO mpas (id, name) VALUES (?, ?);

-- добавление записи в таблицу FRIENDSHIP:
INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?);

-- обновление данных в таблице FRIENDSHIP:
UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ? AND user_id <> friend_id;

-- добавление записи в таблицу USER_FILM_LIKE:
INSERT INTO user_film_like (user_id, film_id) VALUES (?, ?);

-- добавление записи в таблицу FILM_GENRE:
INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?);

-- добавление записи в таблицу FILM_MPA:
INSERT INTO film_mpa (film_id, mpa_id) VALUES (?, ?);

-- пример выборки данных из таблиц FILMS получение топ фильмов с лимитом выборки:
SELECT f.id, f.name, f.description, f.duration, f.release_date, f.rate
FROM films AS f
LEFT JOIN user_film_like AS ufl ON f.id = ufl.film_id
GROUP BY f.id, f.rate
ORDER BY f.rate DESC
LIMIT ?;

-- пример выборки данных из таблиц USERS и USER_FRIENDS получение общих друзей двух пользователей:
SELECT users.id, users.email, users.login, users.name, users.birthday 
FROM users
JOIN friendship AS fs1 ON fs1.friend_id = users.id 
JOIN friendship AS fs2 ON fs1.friend_id = fs2.friend_id
AND fs1.user_id = ? AND fs2.user_id = ?
AND (fs2.status LIKE 'FRIENDSHIP'
OR fs2.status LIKE 'SUBSCRIPTION')
```
</details>

<details>
  <summary><h3> dbdiagram ТЗ10 </h3></summary>

#### Ссылка на структуру базы данных для приложения Filmorate: \scr\main\resources\sql\schema.png
![schema](/src/main/resources/sql/schema.png)
</details>