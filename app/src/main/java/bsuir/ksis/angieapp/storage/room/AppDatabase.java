package bsuir.ksis.angieapp.storage.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import bsuir.ksis.angieapp.storage.room.dao.ProfileDao;
import bsuir.ksis.angieapp.storage.room.dao.UserDao;
import bsuir.ksis.angieapp.storage.room.entities.Profile;
import bsuir.ksis.angieapp.storage.room.entities.User;

@Database(entities = { User.class, Profile.class }, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ProfileDao profileDao();

    private static AppDatabase instance;
    public static AppDatabase getDatabase(Context context)
    {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context,
                    AppDatabase.class,
                    "angie_db"
                ).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return instance;
    }

    public void destroyDatabase()
    {
        instance = null;
    }
}
