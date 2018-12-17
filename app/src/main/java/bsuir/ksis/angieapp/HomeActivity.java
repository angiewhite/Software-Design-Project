package bsuir.ksis.angieapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 78;

    private boolean wasStopped = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavController navController = Navigation.findNavController(this, R.id.fragment);

        setupBottomNavMenu(navController);
        setupActionBar(navController);
    }

    private void setupBottomNavMenu(NavController navController)
    {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        if (bottomNavigationView == null) return;
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        NavController navController = Navigation.findNavController(this, R.id.fragment);
        boolean navigated = NavigationUI.onNavDestinationSelected(item, navController);
        return navigated || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment);
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
        }
    }
}
