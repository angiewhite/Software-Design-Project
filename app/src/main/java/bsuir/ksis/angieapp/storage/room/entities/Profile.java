package bsuir.ksis.angieapp.storage.room.entities;

import androidx.room.*;

@Entity(tableName = "profiles")
@ForeignKey(entity = User.class, parentColumns = "id", childColumns = "id")
public class Profile {

    public Profile(String surname, String name, String email, String phone)
    {
        this.surname = surname;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Profile() {}

    @PrimaryKey()
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "surname")
    public String surname;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "image_path")
    public String imagePath;
}
