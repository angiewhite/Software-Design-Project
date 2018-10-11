package bsuir.ksis.angieapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView imei_tv = findViewById(R.id.imei_text_view);
        imei_tv.setText("lkfjdlks");
    }
}
