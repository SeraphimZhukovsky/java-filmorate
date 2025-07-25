package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User addUser(User user) {
        processUserName(user);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        processUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUser(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void processUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
