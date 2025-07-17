package com.example.tasks.Activities;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.tasks.FBRef.refPresUidCurrentWeek;
import static com.example.tasks.FBRef.refPresenceRoot;
//import static com.example.tasks.FBRef.refPresenceYear;


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


    private void uploadMockDataToFirebase() {
        // 🗓 Get current time
        Calendar now = Calendar.getInstance();

        // 🔢 Build key parts
        int week = now.get(Calendar.WEEK_OF_YEAR);
        String weekStr = "W" + week;

        String MMdd = String.format("%02d%02d", now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        String MMM = new SimpleDateFormat("MMM", Locale.ENGLISH).format(now.getTime());

        Pair<String, Integer> lessonInfo = getRoundedTimeAndLessonSlot(Calendar.getInstance());
        String roundedHHmm = lessonInfo.first;
        int lesson = lessonInfo.second;

        if (lesson == -1) {
            Log.e("PresenceActivity", "Time not in valid school hours");
            return;
        }
        String timestampAndLesson = MMdd + MMM + "_" + roundedHHmm + "L" + lesson;

        // 🔄 Data payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("class", "יוד571");
        payload.put("detected", mockDetectedNames);
        payload.put("missing", mockMissingNames);
        payload.put("timestamp", System.currentTimeMillis());

        // 🪄 Write using the clean FBRef
        refPresUidCurrentWeek
                .child(timestampAndLesson)
                .setValue(payload)
                .addOnSuccessListener(unused -> Log.i("PresenceActivity", "Upload success"))
                .addOnFailureListener(e -> Log.e("PresenceActivity", "Upload failed", e));
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
        uploadMockDataToFirebase();
    }

    /**
     * TEMPORARY: Heuristic to calculate lesson number (L#) from current time.
     * Assumes:
     * - First lesson starts at 08:30
     * - Each lesson is 50 minutes
     * - Round to the nearest lesson slot using ±25 min tolerance
     * <p>
     * NOTE: This logic will be replaced:
     * - First with school-specific if-clauses
     * - Then later with profile info from teacher's settings
     */
    /**
     * TEMPORARY: Determine rounded time and lesson number based on current time.
     * Assumes:
     *   - L1 starts at 08:30
     *   - 50-minute slots
     *   - Round to nearest slot start using ±25 min
     *
     * @param now current time
     * @return Pair of ("HHmm" rounded start time string, lesson number), or (-1) if out of range
     */
    public static Pair<String, Integer> getRoundedTimeAndLessonSlot(Calendar now) {
        int totalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        int baseStart = 8 * 60 + 30; // 08:30 in minutes
        int maxLessons = 14;
        int lessonDuration = 50;

        int minValid = baseStart - 25;
        int maxValid = baseStart + lessonDuration * (maxLessons - 1) + 25;

        if (totalMinutes < minValid || totalMinutes > maxValid) {
            return new Pair<>(null, -1);  // outside school time
        }

        int roundedIndex = Math.round((totalMinutes - baseStart) / (float) lessonDuration);
        int lessonStartMinutes = baseStart + roundedIndex * lessonDuration;

        int hour = lessonStartMinutes / 60;
        int minute = lessonStartMinutes % 60;
        String roundedHHmm = String.format("%02d%02d", hour, minute);

        return new Pair<>(roundedHHmm, roundedIndex + 1);
    }
}