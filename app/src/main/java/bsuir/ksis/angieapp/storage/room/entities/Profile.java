package bsuir.ksis.angieapp.storage.room.entities;

import androidx.room.*;

@Entity(tableName = "profiles")
@ForeignKey(entity = User.class, parentColumns = "id", childColumns = "id")
public class Profile {
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
