package bsuir.ksis.angieapp.storage.room;

import bsuir.ksis.angieapp.storage.IStorage;
import bsuir.ksis.angieapp.storage.room.entities.Profile;
import bsuir.ksis.angieapp.storage.room.entities.User;

public class Storage implements IStorage {

    private AppDatabase appDatabase;

    public Storage(AppDatabase appDatabase)
    {
        this.appDatabase = appDatabase;
    }

    @Override
    public Boolean createUser(User user) {
        if (appDatabase.userDao().getAuthenticatedUserByLogin(user.login) != null) return false;

        appDatabase.userDao().saveUser(user);

        User newUser = appDatabase.userDao().getAuthenticatedUserByLogin(user.login);
        if (newUser == null) return false;
        Profile profile = new Profile();
        profile.id = newUser.id;
        appDatabase.profileDao().saveProfile(profile);
        return true;
    }

    @Override
    public Boolean authenticateUser(String login, String password) {
        User user = appDatabase.userDao().getAuthenticatedUserByLogin(login);
        if (user == null) return false;

        return password.equals(user.password);
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
