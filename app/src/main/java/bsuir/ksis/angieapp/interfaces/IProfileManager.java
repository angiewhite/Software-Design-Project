package bsuir.ksis.angieapp.interfaces;

import bsuir.ksis.angieapp.ProfileFragment;
import bsuir.ksis.angieapp.storage.room.entities.Profile;

public interface IProfileManager {
    Profile getProfileInfo();

    void saveProfileInfo(Profile profile);

    void uploadPhoto();

    Boolean getChangeMode();

    void setChangeMode(Boolean isChangeMode);
}
