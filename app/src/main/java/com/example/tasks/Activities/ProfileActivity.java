package com.example.tasks.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends MasterActivity {

    private EditText usernameEdit;
    private Spinner activeYearSpinner;
    private Spinner defaultScreenSpinner;
    private Button takePictureBtn;
    private ImageView profileImage;

    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private boolean isSpinnersLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Views
        usernameEdit = findViewById(R.id.usernameEdit);
        activeYearSpinner = findViewById(R.id.activeYearSpinner);
        defaultScreenSpinner = findViewById(R.id.defaultScreenSpinner);
        takePictureBtn = findViewById(R.id.takePictureBtn);
        profileImage = findViewById(R.id.profileImage);

        // Firebase refs
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid());

        // Populate spinners
        List<String> years = Arrays.asList("2023", "2024", "2025", "2026");
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activeYearSpinner.setAdapter(yearAdapter);

        List<String> screens = Arrays.asList("Presence", "Task", "Reports", "MainTask");
        ArrayAdapter<String> screenAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, screens);
        screenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        defaultScreenSpinner.setAdapter(screenAdapter);

        // Listeners for saving (ignore initial selections)
        activeYearSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String value) {
                if (isSpinnersLoaded) {
                    userRef.child("activvvYear").setValue(value);
                }
            }
        });
        defaultScreenSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String value) {
                if (isSpinnersLoaded) {
                    userRef.child("preferredActivity").setValue(value);
                }
            }
        });

        // Load existing data
        loadProfile();

        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: launch camera intent and handle onActivityResult
            }
        });
    }

    private void loadProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                // Username
                String uname = snap.child("username").getValue(String.class);
                if (uname != null) usernameEdit.setText(uname);

                // Active year: new key then fallback
                String year = snap.child("activvvYear").getValue(String.class);
                if (year == null) {
                    year = snap.child(currentUser.getUid()).getValue(String.class);
                    if (year != null) {
                        userRef.child("activvvYear").setValue(year);
                    }
                }
                if (year != null) {
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) activeYearSpinner.getAdapter();
                    int pos = adapter.getPosition(year);
                    if (pos >= 0) activeYearSpinner.setSelection(pos);
                }

                // Default screen
                String screen = snap.child("preferredActivity").getValue(String.class);
                if (screen != null) {
                    ArrayAdapter<String> adapter2 = (ArrayAdapter<String>) defaultScreenSpinner.getAdapter();
                    int pos2 = adapter2.getPosition(screen);
                    if (pos2 >= 0) defaultScreenSpinner.setSelection(pos2);
                }

                // Profile image
                String b64 = snap.child("profileImage").getValue(String.class);
                if (b64 != null) {
                    byte[] data = Base64.decode(b64, Base64.DEFAULT);
                    profileImage.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                }

                // Now allow spinner callbacks to write changes
                isSpinnersLoaded = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Utility to encode and upload image
    private void uploadImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] bytes = baos.toByteArray();
        String b64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        userRef.child("b64jpg").setValue(b64);
    }

    // Spinner helper
    private abstract static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
            onItemSelected(parent.getItemAtPosition(pos).toString());
        }

        public abstract void onItemSelected(String value);
    }
}
