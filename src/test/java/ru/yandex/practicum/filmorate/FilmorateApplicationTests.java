package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FilmorateApplicationTests {

	@Autowired
	private FilmController filmController;

	@Autowired
	private UserController userController;

	@Test
	void contextLoads() {
	}

	@Test
	void filmControllerShouldBeLoaded() {
		assertNotNull(filmController, "FilmController должен быть загружен в контекст приложения");
	}

	@Test
	void userControllerShouldBeLoaded() {
		assertNotNull(userController, "UserController должен быть загружен в контекст приложения");
	}
}
