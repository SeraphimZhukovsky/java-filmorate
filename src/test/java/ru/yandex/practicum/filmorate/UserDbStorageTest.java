package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@ContextConfiguration(classes = FilmorateApplication.class)
@Import(UserDbStorage.class)
class UserDbStorageTest {
    @Autowired
    private UserDbStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setLogin("testLogin");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void addUser_ShouldSaveUserWithGeneratedId() {
        User savedUser = userStorage.addUser(testUser);
        assertThat(savedUser.getId()).isPositive();
    }

    @Test
    void getUser_ShouldReturnSavedUser() {
        User savedUser = userStorage.addUser(testUser);
        Optional<User> foundUser = userStorage.getUser(savedUser.getId());
        assertThat(foundUser).isPresent().contains(savedUser);
    }

    @Test
    void updateUser_ShouldModifyExistingUser() {
        User savedUser = userStorage.addUser(testUser);
        savedUser.setName("Updated Name");
        User updatedUser = userStorage.updateUser(savedUser);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void addFriend_ShouldCreateFriendship() {
        User user1 = userStorage.addUser(testUser);
        User user2 = userStorage.addUser(createAnotherUser());
        userStorage.addFriend(user1.getId(), user2.getId());
        Collection<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1).extracting(User::getId).contains(user2.getId());
    }

    private User createAnotherUser() {
        User user = new User();
        user.setEmail("friend@example.com");
        user.setLogin("friendLogin");
        user.setName("Friend");
        user.setBirthday(LocalDate.of(1995, 5, 5));
        return user;
    }
}