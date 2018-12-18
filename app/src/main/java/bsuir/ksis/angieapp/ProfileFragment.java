package bsuir.ksis.angieapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import bsuir.ksis.angieapp.interfaces.IProfileManager;
import bsuir.ksis.angieapp.storage.room.AppDatabase;
import bsuir.ksis.angieapp.storage.room.Storage;
import bsuir.ksis.angieapp.storage.room.entities.Profile;
import bsuir.ksis.angieapp.storage.room.entities.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class ProfileFragment extends Fragment {

    static Boolean isChangeable = false;
    IProfileManager profileManager;

    EditText[] editViews;
    TextView[] textViews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        profileManager = (IProfileManager) context;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Profile profile = profileManager.getProfileInfo();
        if (profile != null) displayProfile(profile);

        Activity activity = getActivity();

        editViews = new EditText[]{activity.findViewById(R.id.surnameEditView),
                activity.findViewById(R.id.nameEditView),
                activity.findViewById(R.id.phoneEditView),
                activity.findViewById(R.id.emailEditView)};
        textViews = new TextView[]{activity.findViewById(R.id.surnameTextView),
                activity.findViewById(R.id.nameTextView),
                activity.findViewById(R.id.phoneTextView),
                activity.findViewById(R.id.emailTextView)};

        if (isChangeable) changeProfilePressed();

        getActivity().findViewById(R.id.profilePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChangeable) {
                    profileManager.uploadPhoto();
                    displayProfile(profileManager.getProfileInfo());
                }
            }
        });

        addKeyboardListener();

        getActivity().findViewById(R.id.changeProfileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChangeable) {
                    changeProfilePressed();
                    isChangeable = true;
                } else {
                    saveProfilePressed();
                    isChangeable = false;
                }
            }
        });
    }

    private void addKeyboardListener() {
        final Activity activity = getActivity();
        activity.findViewById(R.id.profileView).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View profileView = activity.findViewById(R.id.profileView);
                if (profileView == null) return;
                Rect r = new Rect();
                profileView.getWindowVisibleDisplayFrame(r);
                int screenHeight = profileView.getRootView().getHeight();

                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine button height.
                    activity.findViewById(R.id.changeProfileButton).setVisibility(View.GONE);
                } else {
                    activity.findViewById(R.id.changeProfileButton).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void changeProfilePressed() {
        for (EditText editView : editViews)
            editView.setVisibility(View.VISIBLE);

        for (TextView textView : textViews)
            textView.setVisibility(View.GONE);

        getActivity().findViewById(R.id.profilePhotoEditImage).setVisibility(View.VISIBLE);
        ((ImageView)getActivity().findViewById(R.id.changeProfileButton)).setImageResource(R.drawable.ic_done);
    }

    private void saveProfilePressed() {
        for (EditText editView : editViews)
            editView.setVisibility(View.GONE);

        for (TextView textView : textViews)
            textView.setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.profilePhotoEditImage).setVisibility(View.INVISIBLE);
        ((ImageView)getActivity().findViewById(R.id.changeProfileButton)).setImageResource(R.drawable.ic_edit);

        Profile profile = new Profile(((EditText)getActivity().findViewById(R.id.surnameEditView)).getText().toString(),
                ((EditText)getActivity().findViewById(R.id.nameEditView)).getText().toString(),
                ((EditText)getActivity().findViewById(R.id.emailEditView)).getText().toString(),
                ((EditText)getActivity().findViewById(R.id.phoneEditView)).getText().toString());

        saveProfile(profile);
    }

    private void saveProfile(Profile profile) {
        SharedPreferences preferences = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        int userId = preferences.getInt(getString(R.string.current_user), -1);
        if (userId == -1) {
            User user = new User();
            user.login = profile.name;
            user.password = "default";
            Storage storage = new Storage(AppDatabase.getDatabase(getContext()));
            user = storage.createUser(user);
            userId = user.id;
            preferences.edit().putInt(getActivity().getString(R.string.current_user), user.id).apply();
        }
        profile.id = userId;
        Profile oldProfile = profileManager.getProfileInfo();
        profile.imagePath = oldProfile.imagePath;
        profileManager.saveProfileInfo(profile);
        displayProfile(profile);
    }

    private void displayProfile(Profile newProfile) {
        if (newProfile == null) return;

        Activity activity = getActivity();
        ((TextView)activity.findViewById(R.id.nameTextView)).setText(newProfile.name);
        ((TextView)activity.findViewById(R.id.surnameTextView)).setText(newProfile.surname);
        ((TextView)activity.findViewById(R.id.phoneTextView)).setText(newProfile.phone);
        ((TextView)activity.findViewById(R.id.emailTextView)).setText(newProfile.email);

        ((EditText)activity.findViewById(R.id.nameEditView)).setText(newProfile.name, TextView.BufferType.EDITABLE);
        ((EditText)activity.findViewById(R.id.surnameEditView)).setText(newProfile.surname, TextView.BufferType.EDITABLE);
        ((EditText)activity.findViewById(R.id.phoneEditView)).setText(newProfile.phone, TextView.BufferType.EDITABLE);
        ((EditText)activity.findViewById(R.id.emailEditView)).setText(newProfile.email, TextView.BufferType.EDITABLE);

        if (newProfile.imagePath != null)
        ((ImageView)activity.findViewById(R.id.profilePhoto)).setImageBitmap(getBitmap(newProfile.imagePath));

    }

    private Bitmap getBitmap(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) return BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.user_profile);

        return BitmapFactory.decodeFile(filePath);
    }
}
