package bsuir.ksis.angieapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

public class AboutFragment extends Fragment {

    private static final int REQUEST_READ_PHONE_STATE = 78;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayVersion();

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestReadPhoneStatePermission();
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                displayIMEI();
            }
        } else {
            displayIMEI();
        }
    }

    private void displayVersion()
    {
        TextView textView = getActivity().findViewById(R.id.version_text_view);
        textView.setText(BuildConfig.VERSION_NAME);
    }

    private void displayIMEI()
    {
        Activity activity = getActivity();

        TextView imei_tv = activity.findViewById(R.id.imei_text_view);
        TelephonyManager manager = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);

        String imei;
        try {
            imei = manager.getDeviceId();
            imei_tv.setText(imei == null ? getString(R.string.IMEI, getString(R.string.unknown)) :
                    getString(R.string.IMEI, imei));
        } catch (SecurityException e) {
            throw e;
        }

        TextView textView = getActivity().findViewById(R.id.imei_text_view);
        textView.setText(imei == null ? getString(R.string.IMEI, getString(R.string.unknown)) :
                                        getString(R.string.IMEI, imei));
    }

    private void requestReadPhoneStatePermission() {
        final Activity activity = getActivity();

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_PHONE_STATE)) {
            String message = "Give Phone permission to the app to enable IMEI display functionality.";
            Snackbar.make(activity.findViewById(R.id.parent_container), message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.snackbar_action, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            REQUEST_READ_PHONE_STATE);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_READ_PHONE_STATE);
        }
    }
}
