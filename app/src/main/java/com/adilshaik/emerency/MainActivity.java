package com.adilshaik.emerency;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button mDoctor, mPatient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDoctor = (Button) findViewById(R.id.doctor);
        mPatient = (Button) findViewById(R.id.patient);

        mDoctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DoctorLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PatientLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

}
