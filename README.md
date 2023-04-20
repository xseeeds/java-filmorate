# java-filmorate
#### Template repository for Filmorate project.

#### Ссылка на структуру базы данных для приложения Filmorate: \scr\main\resources\sql\bddiagram.png
![bddiagram](/src/main/resources/sql/bddiagram.png)

#### Ссылка на структуру эндпоинтов для приложения Filmorate: \src\main\resources\endpointsPNG\endpointFilmorate.png
![endpoints](/src/main/resources/endpointsPNG/endpointFilmorate.png)

# Примеры запросов

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

# Примеры запросов SQL
#### добавление записи в таблицу USERS:
INSERT INTO public.users (email, login, name, birthday) VALUES (?, ?, ?, ?)

#### обновление данных в таблице USERS:
UPDATE public.users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?

#### добавление записи в таблицу FILMS:
INSERT INTO public.films (name, description, release_date, duration) values (?, ?, ?, ?)

#### обновление данных в таблице FILMS:
UPDATE public.films SET name = ?, description = ?, duration = ?, release_date = ?, mpa = ? WHERE id = ?

#### добавление записи в таблицу GENRES:
INSERT INTO public.genre (name) VALUES (?)

#### добавление записи в таблицу MPA:
INSERT INTO public.mpa (id, name) VALUES (?, ?)

#### добавление записи в таблицу FRIEND:
INSERT INTO friend (user_id, friend_id, status) VALUES (?, ?, ?)

#### обновление данных в таблице FRIEND:
UPDATE public.friend SET status = ? WHERE user_id = ? AND friend_id = ? AND user_id <> friend_id

#### добавление записи в таблицу LIKE:
INSERT INTO public.\"LIKE\" (user_id, film_id) VALUES (?, ?)

#### добавление записи в таблицу GENRE:
INSERT INTO public.genre (film_id, genre_id) VALUES (?, ?)

#### пример выборки данных из таблиц USERS и USER_FRIENDS получение общих друзей двух пользователей:
SELECT public.users.id, public.users.email, public.users.login, public.users.name, public.users.birthday 
FROM public.users
JOIN public.friend AS f1 ON f1.friend_id = public.users.id 
JOIN public.friend AS f2 ON f1.friend_id = f2.friend_id
WHERE f1.user_id = ? and f2.user_id = ?


## dbdiagram ТЗ10
#### Ссылка на структуру базы данных для приложения Filmorate: \scr\main\resources\sql\schema.png
![schema](/src/main/resources/sql/schema.png)