package bsuir.ksis.angieapp.storage;

import bsuir.ksis.angieapp.storage.room.entities.Profile;
import bsuir.ksis.angieapp.storage.room.entities.User;

public interface IStorage {
    User createUser(User user);

    Boolean authenticateUser(String login, String password);

    void saveProfile(Profile profile);
    Profile getProfile(int id);

    void savePhoto(int id, String photoPath);
}
