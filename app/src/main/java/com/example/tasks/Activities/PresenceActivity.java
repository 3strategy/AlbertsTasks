package com.example.tasks.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PresenceActivity extends MasterActivity {


    private TextView classNameLabel, missingStudentsLabel, detectedNamesLabel;
    private ListView attendanceList;
    private Button btnStart, btnStop, btnDebug;

    private final List<String> mockDetectedNames = Arrays.asList("Alice", "Bob", "Charlie");
    private final List<String> mockMissingNames = Arrays.asList("David", "Eve");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure content takes up full screen (EdgeToEdge behavior)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_presence);

        // Respect system bars (status/nav bar insets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔗 Bind views
        classNameLabel = findViewById(R.id.classNameLabel);
        missingStudentsLabel = findViewById(R.id.missingStudentsLabel);
        detectedNamesLabel = findViewById(R.id.detectedNamesLabel);
        attendanceList = findViewById(R.id.attendanceList);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnDebug = findViewById(R.id.btnDebug);

        // 🧪 Hook debug button
        btnDebug.setOnClickListener(v -> populateMockData());
    }


    private void populateMockData() {
        // Set class name
        classNameLabel.setText("כיתה: יוד571");

        // Set detected names
        StringBuilder detected = new StringBuilder("Recorded Names:");
        for (String name : mockDetectedNames) {
            detected.append("\n• ").append(name);
        }
        detectedNamesLabel.setText(detected.toString());

        // Set missing students
        StringBuilder missing = new StringBuilder("לא דווחו:");
        for (String name : mockMissingNames) {
            missing.append("\n• ").append(name);
        }
        missingStudentsLabel.setText(missing.toString());

        // Fill attendance list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mockDetectedNames
        );
        attendanceList.setAdapter(adapter);

        // Store timestamp for Firebase (not shown yet)
        String isoDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
        Log.d("PresenceActivity", "Simulated date: " + isoDate);

        // 🔜 Next: send this data to Firebase RTDB
    }
}