package bsuir.ksis.angieapp.storage.room.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import bsuir.ksis.angieapp.storage.room.entities.User;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface UserDao {
    @Insert(onConflict = REPLACE)
    void saveUser(User user);

    @Query("SELECT * FROM users u WHERE u.id = :id")
    User getCurrentUser(int id);

    @Query("SELECT * FROM users u WHERE u.login = :login AND u.password = :password ")
    User getAuthenticatedUser(String login, String password);

    @Query("SELECT * FROM users u")
    List<User> getUsers();

    @Query("SELECT * FROM users u WHERE u.login = :login")
    User getAuthenticatedUserByLogin(String login);

}
