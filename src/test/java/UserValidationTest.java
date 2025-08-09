import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().minusYears(20));
    }

    @Test
    void shouldPassValidationForValidUser() {
        assertDoesNotThrow(() -> validateUser(user));
    }

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        user.setName("");
        validateUser(user);
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        user.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> validateUser(user));

        user.setEmail("");
        assertThrows(ValidationException.class, () -> validateUser(user));
    }

    @Test
    void shouldFailWhenLoginIsInvalid() {
        user.setLogin("");
        assertThrows(ValidationException.class, () -> validateUser(user));

        user.setLogin("login with spaces");
        assertThrows(ValidationException.class, () -> validateUser(user));
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> validateUser(user));
    }

    private void validateUser(User user) throws ValidationException {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
