package bsuir.ksis.angieapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import bsuir.ksis.angieapp.interfaces.ISignInManager;
import bsuir.ksis.angieapp.interfaces.ISignUpManager;
import bsuir.ksis.angieapp.storage.IStorage;
import bsuir.ksis.angieapp.storage.room.AppDatabase;
import bsuir.ksis.angieapp.storage.room.Storage;
import bsuir.ksis.angieapp.storage.room.entities.User;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class AuthenticationActivity extends AppCompatActivity implements ISignUpManager, ISignInManager {

    IStorage storage;
    NavController navController;

    public static final int REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        setSupportActionBar((Toolbar) findViewById(R.id.auth_toolbar));

        navController = Navigation.findNavController(this, R.id.auth_activity_fragment);
        setupActionBar();

        AppDatabase db = AppDatabase.getDatabase(this);
        storage = new Storage(db);

        checkIfSignedIn();
    }

    private void setupActionBar() {
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
        return navigated || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auth_toolbar, menu);
        return true;
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
        }
    }

    @Override
    public boolean signIn(String login, String password) {
        boolean isAuthenticated = storage.authenticateUser(login, password);
        if (isAuthenticated) {
            login(login);
        }
        return isAuthenticated;
    }

    @Override
    public boolean register(String login, String password) {
        User user = new User(login, password);
        user = storage.createUser(user);
        if (user != null)
            Navigation.findNavController(this, R.id.auth_activity_fragment).navigate(R.id.destination_sign_in);
        return user != null;
    }

    private void checkIfSignedIn()
    {
        SharedPreferences preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        int currentUserId = preferences.getInt(getString(R.string.current_user), -1);
        if (currentUserId == -1) return;
        User user = storage.getUser(currentUserId);
        if (user == null) {
            preferences.edit().remove(getString(R.string.current_user)).apply();
            return;
        }
        login(user.login);
    }

    private void login(String login) {
        User user = storage.getUser(login);
        this.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).edit().putInt(getString(R.string.current_user), user.id).apply();
        startActivity(new Intent(this, HomeActivity.class));
    }
}
