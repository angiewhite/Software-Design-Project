package bsuir.ksis.angieapp.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import bsuir.ksis.angieapp.BuildConfig;
import bsuir.ksis.angieapp.HomeActivity;
import bsuir.ksis.angieapp.R;
import bsuir.ksis.angieapp.storage.IStorage;
import bsuir.ksis.angieapp.storage.room.entities.Profile;

public class ProfileService {

    private Activity activity;
    private IStorage storage;
    private String takenPhotoPath = "";

    public ProfileService(Activity activity, IStorage storage)
    {
        this.activity = activity;
        this.storage = storage;
    }

    public Profile getProfile() {
        SharedPreferences preferences = activity.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        int current_user_id = preferences.getInt(activity.getResources().getString(R.string.current_user),-1);
        if (current_user_id == -1) return null;
        return  storage.getProfile(current_user_id);
    }

    public void uploadPhoto() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setPositiveButton("Take Photo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getImageFromCamera();
            }
        }).setNeutralButton("Upload from gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getImageFromGallery();
            }
        });

        builder.show();
    }

    public void getImageFromGallery() {
         int permissionGranted = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        //If the permission was denied show the dialog window to ask the permission
        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    HomeActivity.REQUEST_PERMISSION_EXTERNAL_STORAGE);
        }
        else {
            Intent gallery =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(gallery, HomeActivity.REQUEST_OPEN_GALLERY);
        }
    }

    public void getImageFromCamera() {
        int permissionGranted = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA);

        //If the permission was denied show the dialog window to ask the permission
        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    HomeActivity.REQUEST_PERMISSION_CAMERA);
        }
        else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = getPhotoFile();
                } catch (IOException ex) {
                    return;
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(activity,
                            "com.example.android.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    activity.startActivityForResult(takePictureIntent, HomeActivity.REQUEST_OPEN_CAMERA);
                }
            }
        }
    }

    private File getPhotoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file =  File.createTempFile(
                String.format("JPEG_%s_", timeStamp), /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        takenPhotoPath = file.getAbsolutePath();
        return file;
    }

    public void saveProfileInfo(Profile profile) {
        storage.saveProfile(profile);
    }

    public Boolean getChangeMode() {
        return true || false;
    }

    public void setChangeMode(Boolean isChangeMode) {
        // set change mode
    }

    private Bitmap getBitmap(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) return BitmapFactory.decodeResource(activity.getResources(),
                R.drawable.user_profile);

        return BitmapFactory.decodeFile(filePath);
    }

    public void updatePhoto(Uri photoPath) {
        String path = photoPath == null ? takenPhotoPath : getPhotoPath(photoPath);
        SharedPreferences preferences = activity.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        int current_user_id = preferences.getInt(activity.getResources().getString(R.string.current_user),-1);
        storage.savePhoto(current_user_id, path);
        ((ImageView)activity.findViewById(R.id.profilePhoto)).setImageBitmap(getBitmap(path));
    }

    private String getPhotoPath(Uri uri) {
        String[] filePathColumn = new String[]{MediaStore.Images.Media.DATA};

        Cursor cursor = activity.getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return picturePath;
    }
}
