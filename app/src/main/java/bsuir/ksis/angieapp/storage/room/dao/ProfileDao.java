package bsuir.ksis.angieapp.storage.room.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import bsuir.ksis.angieapp.storage.room.entities.Profile;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface ProfileDao {
    @Insert(onConflict = REPLACE)
    void saveProfile(Profile profile);

    @Query("SELECT * FROM users u, profiles p WHERE u.id = p.id AND u.id = :id")
    Profile getProfile(int id);

    @Query("SELECT * FROM profiles p")
    List<Profile> getProfiles();
}
