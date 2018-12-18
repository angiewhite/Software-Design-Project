package bsuir.ksis.angieapp.storage.room.entities;

import androidx.room.*;

@Entity(tableName = "users")
public class User {

    public User(String login, String password)
    {
        this.login = login;
        this.password = password;
    }

    public User() {}

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "login")
    public String login;

    @ColumnInfo(name = "password")
    public String password;
}
