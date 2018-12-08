package bsuir.ksis.angieapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_READ_PHONE_STATE = 78;

    private boolean wasStopped = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView version_tv = findViewById(R.id.version_text_view);

        version_tv.setText(BuildConfig.VERSION_NAME);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestReadPhoneStatePermission();
        } else {
            tryShowIMEI();
        }
    }

    protected void onStop() {
        super.onStop();

        wasStopped = true;
    }

    protected void onResume() {
        super.onResume();

        if (!wasStopped) return;
        wasStopped = false;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestReadPhoneStatePermission();
        } else {
            tryShowIMEI();
        }
    }

    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            String message = "Give Phone permission to the app to enable IMEI display functionality.";
            Snackbar.make(findViewById(R.id.parent_container), message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.snackbar_action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            REQUEST_READ_PHONE_STATE);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_READ_PHONE_STATE);
        }
    }

    private void tryShowIMEI() {
        TextView imei_tv = findViewById(R.id.imei_text_view);
        TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        try {
            String imei = manager.getDeviceId();
            imei_tv.setText(imei == null ? getString(R.string.IMEI, getString(R.string.unknown)) :
                    getString(R.string.IMEI, imei));
        } catch (SecurityException e) {
            throw e;
        }
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
                    tryShowIMEI();
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
