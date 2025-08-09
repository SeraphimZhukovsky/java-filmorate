package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        getUserOrThrow(user.getId());
        return userStorage.updateUser(user);
    }

    public User getUser(int id) {
        return getUserOrThrow(id);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        getUserOrThrow(friendId);
        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
    }

    public void confirmFriendship(int userId, int friendId) {
        getUserOrThrow(userId).getFriends().put(friendId, FriendshipStatus.CONFIRMED);
        getUserOrThrow(friendId).getFriends().put(userId, FriendshipStatus.CONFIRMED);
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getFriends(int id) {
        return getUserOrThrow(id).getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(int id, int otherId) {
        Map<Integer, FriendshipStatus> userFriends = getUserOrThrow(id).getFriends();
        Map<Integer, FriendshipStatus> otherFriends = getUserOrThrow(otherId).getFriends();

        return userFriends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .filter(entry -> otherFriends.getOrDefault(entry.getKey(), null) == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(int id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}
