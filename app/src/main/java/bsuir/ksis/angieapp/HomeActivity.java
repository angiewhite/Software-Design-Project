package bsuir.ksis.angieapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.NavigationUI;
import bsuir.ksis.angieapp.interfaces.IProfileManager;
import bsuir.ksis.angieapp.services.ProfileService;
import bsuir.ksis.angieapp.storage.IStorage;
import bsuir.ksis.angieapp.storage.room.AppDatabase;
import bsuir.ksis.angieapp.storage.room.Storage;
import bsuir.ksis.angieapp.storage.room.entities.Profile;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class HomeActivity extends AppCompatActivity implements IProfileManager {

    public static final int REQUEST_READ_PHONE_STATE = 1;
    public static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_OPEN_GALLERY = 3;
    public static final int REQUEST_PERMISSION_CAMERA = 4;
    public static final int REQUEST_OPEN_CAMERA = 5;

    IStorage storage;
    ProfileService service;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        storage = new Storage(AppDatabase.getDatabase(this));
        service = new ProfileService(this, storage);

        final NavController navController = Navigation.findNavController(this, R.id.fragment);

        setupBottomNavMenu(navController);
        setupActionBar(navController);

    }

    private void setupBottomNavMenu(final NavController navController)
    {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        if (bottomNavigationView == null) return;
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                if (item.getItemId() != R.id.destination_profile && ProfileFragment.getIsChangeable()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setMessage("If you leave now you will lose all the changes. Which would you prefer to do?")
                            .setTitle("Leaving a page in edit mode.");

                    builder.setPositiveButton("Leave and save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            findViewById(R.id.changeProfileButton).performClick();
                            navController.navigate(item.getItemId());
                        }
                    })
                    .setNeutralButton("Leave without saving", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            findViewById(R.id.cancelButton).performClick();
                            navController.navigate(item.getItemId());
                        }
                    })
                    .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.show();
                } else {
                    navController.navigate(item.getItemId());
                }

                return false;
            }
        });
    }

    private void setupActionBar(NavController navController)
    {
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        final NavController navController = Navigation.findNavController(this, R.id.fragment);
        if (navController.getCurrentDestination().getId() == R.id.destination_profile) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setMessage("If you leave now you will lose all the changes. Which would you prefer to do?")
                    .setTitle("Leaving a page in edit mode.");

            builder.setPositiveButton("Leave and save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    findViewById(R.id.changeProfileButton).performClick();
                    navController.navigateUp();
                }
            })
            .setNeutralButton("Leave without saving", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    findViewById(R.id.cancelButton).performClick();
                    navController.navigateUp();
                }
            })
            .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            builder.show();
            return false;
        }
        return navController.navigateUp();
    }

    private void openSettingsForPhoneStatePermission(){
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivityForResult(appSettingsIntent, REQUEST_READ_PHONE_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    TextView imei_tv = findViewById(R.id.imei_text_view);
                    imei_tv.setText(getString(R.string.imei_fail));
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_PHONE_STATE)) {
                        String message = "You can grant the permission later in app settings (Permissions section)";
                        Snackbar.make(findViewById(R.id.parent_container), message, Snackbar.LENGTH_LONG)
                                .setAction("SETTINGS", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        openSettingsForPhoneStatePermission();
                                    }
                                })
                                .show();
                    }
                }
                break;
            }
            case HomeActivity.REQUEST_PERMISSION_EXTERNAL_STORAGE: {
                //Open the gallery
                if ((grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    service.getImageFromGallery();
                }
                //Show the explanation for the permission if it was denied
                else if ((grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    showPermissionExplanation(Manifest.permission.READ_EXTERNAL_STORAGE,
                            getString(R.string.read_external_storage_permission_explanation),
                            HomeActivity.REQUEST_PERMISSION_EXTERNAL_STORAGE);
                }
                break;
            }
            case HomeActivity.REQUEST_PERMISSION_CAMERA: {
                //Open the gallery
                if ((grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    service.getImageFromCamera();
                }
                //Show the explanation for the permission if it was denied
                else if ((grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                    showPermissionExplanation(Manifest.permission.CAMERA,
                            getString(R.string.camera_permission_explanation),
                            HomeActivity.REQUEST_PERMISSION_CAMERA);
                }
                break;
            }
        }
    }

    public void showPermissionExplanation (final String permission, String explanation,
                                            final int permissionRequestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                permission)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String dialogQuestion = getString(R.string.permission_explanation_dialog_question);
            builder.setMessage(String.format("%s %s", explanation, dialogQuestion))
                    .setTitle(R.string.permission_explanation_dialog_title);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{permission}, permissionRequestCode);
                }
            });

            builder.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            signOut();
            return true;
        }

        NavController navController = Navigation.findNavController(this, R.id.fragment);
        boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
        return navigated || super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case HomeActivity.REQUEST_OPEN_GALLERY : {
                if (data == null) return;
                Uri selectedImage = (Uri)data.getData();
                if (selectedImage == null) return;
                service.updatePhoto(selectedImage);
                break;
            }
            case HomeActivity.REQUEST_OPEN_CAMERA : {
                service.updatePhoto(null);
                break;
            }
    }
    }

    private void signOut() {
        SharedPreferences preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getString(R.string.current_user));
        editor.apply();
        startActivity(new Intent(this, AuthenticationActivity.class));
    }

    @Override
    public Profile getProfileInfo() {
        return service.getProfile();
    }

    @Override
    public void saveProfileInfo(Profile profile) {
        service.saveProfileInfo(profile);
    }

    @Override
    public void uploadPhoto() {
        service.uploadPhoto();
    }
}
