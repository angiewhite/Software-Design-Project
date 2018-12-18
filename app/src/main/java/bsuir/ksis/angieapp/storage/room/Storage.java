package bsuir.ksis.angieapp.storage.room;

import bsuir.ksis.angieapp.storage.IStorage;
import bsuir.ksis.angieapp.storage.room.entities.Profile;
import bsuir.ksis.angieapp.storage.room.entities.User;
import org.mindrot.jbcrypt.BCrypt;

public class Storage implements IStorage {

    private AppDatabase appDatabase;

    public Storage(AppDatabase appDatabase)
    {
        this.appDatabase = appDatabase;
    }

    @Override
    public User createUser(User user) {
        if (appDatabase.userDao().getAuthenticatedUserByLogin(user.login) != null) return null;

        user.password = BCrypt.hashpw(user.password, BCrypt.gensalt());
        appDatabase.userDao().saveUser(user);

        User newUser = appDatabase.userDao().getAuthenticatedUserByLogin(user.login);
        if (newUser == null) return null;
        Profile profile = new Profile();
        profile.id = newUser.id;
        appDatabase.profileDao().saveProfile(profile);
        return newUser;
    }

    @Override
    public User getUser(String login) {
        return appDatabase.userDao().getAuthenticatedUserByLogin(login);
    }

    @Override
    public User getUser(int id) {
        return appDatabase.userDao().getCurrentUser(id);
    }

    @Override
    public Boolean authenticateUser(String login, String password) {
        User user = appDatabase.userDao().getAuthenticatedUserByLogin(login);
        if (user == null) return false;

        return BCrypt.checkpw(password, user.password);
    }

    @Override
    public void saveProfile(Profile profile) {
        Profile oldProfile = appDatabase.profileDao().getProfile(profile.id);
        if (oldProfile == null) return;

        oldProfile.name = profile.name;
        oldProfile.surname = profile.surname;
        oldProfile.phone = profile.phone;
        oldProfile.email = profile.email;

        appDatabase.profileDao().saveProfile(oldProfile);
    }

    @Override
    public Profile getProfile(int id) {
        Profile profileEntity = appDatabase.profileDao().getProfile(id);
        if (profileEntity == null) return null;
        return profileEntity;

    }

    @Override
    public void savePhoto(int id, String photoPath) {
        Profile profileEntity = appDatabase.profileDao().getProfile(id);
        if (profileEntity == null) return;
        profileEntity.imagePath = photoPath;
        appDatabase.profileDao().saveProfile(profileEntity);
    }
}
