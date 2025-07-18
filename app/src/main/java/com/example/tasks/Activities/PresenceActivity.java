package com.example.tasks.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.R;
import com.example.tasks.SpeechToTextService;
import com.example.tasks.models.Student;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
/// all students is loaded asynchronouly from RTDB during Login, and openning of the
/// PresenceActivity is conditioned by its completion.
import static com.example.tasks.FBRef.allStudents;

/// this is a narrow branch calculated reference.
/// An efficient flat tree branch (3 levels):
/// /P{YY}_{uid}/W{current week}/{MMDDMMM_HHmmL#}/
/// /P25_tTe3W4vIjHe0HSqRXxAIUBxIzKg1/W29/0718Jul_0830L1
/// the month apears twich - 1st for lexicograpic sort, and then for legibility of 18Jul date
/// it holds a lesson report (here the lesson at 0830 is L1 meaning lesson #1 of the day)
import static com.example.tasks.FBRef.refPresUidCurrentWeek;


public class PresenceActivity extends MasterActivity {

    private TextView statusSummaryTextView;
    private TextView disturbanceLabel;

    // Data model to hold each student's current status and disturbances flag
    private Map<String, String> studentStatus = new HashMap<>();
    private Set<String> disturbanceSet = new HashSet<>();

    //private List<Student> allStudents;              // loaded from RTDB
    private Set<String> collectedWords = new HashSet<>();
    List<String> classNicks;
    private String detectedClassName = null;
    private TextView classNameTextView;
    private SpeechToTextService speechService;
    private final List<String> rawTranscripts = new ArrayList<>();

    private TextView transcriptTextView;
    private TextView classNameLabel, missingStudentsLabel;
    private ListView attendanceList;
    private Button btnStart, btnStop, btnDebug;
    private static final int REQUEST_RECORD_AUDIO = 1001;
    private ArrayAdapter<String> adapter;
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

        classNameLabel = findViewById(R.id.classNameLabel);
        missingStudentsLabel = findViewById(R.id.missingStudentsLabel);
        transcriptTextView = findViewById(R.id.transcriptTextView);
        attendanceList = findViewById(R.id.attendanceList);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        //btnDebug = findViewById(R.id.btnDebug);
        statusSummaryTextView = findViewById(R.id.statusSummaryTextView);
        disturbanceLabel = findViewById(R.id.disturbanceLabel);

        //updateStatusUI("some data", "some data2");

        // Prepare list adapter for final results
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new java.util.ArrayList<>()
        );
        attendanceList.setAdapter(adapter);

        /// The speech service will do Speech To Text and after accumulating some names
        /// will identify which class they belong to, and display the data of
        /// the names that are yet to be read and accounted for.
        /// all this is stored in RTDB
        /// in an efficient flat tree branch (3 levels):
        /// /P{YY}_{uid}/W{current week}/{MMDDMMM_HHmmL#}/
        /// /P25_tTe3W4vIjHe0HSqRXxAIUBxIzKg1/W29/0718Jul_0650L-1/
        /// the storage topology is aimed at give the teacher a NARROW branch to look at (the Week level)
        /// instead of a monthly or yearly view which contains too many reports.
        speechService = new SpeechToTextService(this,
                new SpeechToTextService.OnSpeechRecognizedListener() {
                    @Override
                    public void onPartialResults(String text) {
                        runOnUiThread(() -> transcriptTextView.setText(text));
                    }

                    @Override
                    public void onResults(String text) {
                        runOnUiThread(() -> {
                            // 1. Timestamp + push to UI & RTDB
                            String ts = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    .format(new Date());
                            String entry = "[" + ts + "] " + text;
                            adapter.add(entry);
                            rawTranscripts.add(entry);

                            /// 2. we need this collection to be able to compare against nicknames
                            /// what we do is: Split into words, normalize, collect
                            /// later on the collected words will be used by detectClass
                            /// to identify the class.
                            for (String w : text.split("\\s+")) {
                                String norm = w
                                        .replaceAll("[^\\p{L}\\p{Nd}]", "")      // strip punctuation
                                        .toLowerCase(Locale.getDefault());
                                if (!norm.isEmpty()) {
                                    collectedWords.add(norm);
                                }
                            }

                            /// 3. Once we have enough words, try to detect class
                            /// here - we meet the entry condition for detectClass() >= 8 >= 6
                            /// so we try detecting againts the collectedWords
                            if (detectedClassName == null && collectedWords.size() > 3 &&
                                    detectClass()) { // run onces
                                classNameLabel.setText("כיתה: " + detectedClassName);

                                // build list of this class’s nicknames
                                classNicks = new ArrayList<>();
                                for (Student s : allStudents) {
                                    if (detectedClassName.equals(s.getClassName())) {
                                        classNicks.add(s.getNickName());
                                    }
                                }
                            }

                            if (classNicks != null) {
                                // filter out the ones we heard
                                List<String> missing = new ArrayList<>();
                                for (String nick : classNicks) {
                                    /// now that we have the class's nicknames
                                    /// we can check which names are missing (i.e. teacher has not
                                    /// called out their name - and they may be present).
                                    if (!collectedWords.contains(nick.toLowerCase(Locale.getDefault()))) {
                                        missing.add(nick);
                                    }
                                }

                                /// This red missingStudentsLabel will keep getting updated
                                /// again and again (each time teacher stops talking) and will
                                /// become smaller and smaller
                                /// Eventually, the teacher has every student accounted for
                                /// and all is stored in DB.
                                /// /class: יוד571
                                /// /rawTranscript/
                                /// [08:31:15] נאיה אלה הילה
                                /// [08:31:22] ארז יותם יובל
                                /// [08:31:27] אורי נועם עומר
                                /// while this data may seem useless (inaccurate) it can be
                                /// reverse mapped to the Student's id, so this is in fact
                                /// both legible and useful.
                                String missingText = missing.isEmpty()
                                        ? "לא דווחו: כל התלמידים"
                                        : "לא דווחו: " + TextUtils.join(", ", missing);
                                missingStudentsLabel.setText(missingText);

                                analyzeTranscriptLine(text); // send text for special anaysis for being late / missing etc.

                                pushRawTranscript(entry);
                            }

                            //pushRawTranscript(entry);
                        });
                    }
                }
        );

        // 🧪 Hook debug button
        //btnDebug.setOnClickListener(v -> populateMockData());

        // 🔊 Start recognition
        btnStart.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO);
            } else {
                transcriptTextView.setText(""); // clear on new session
                speechService.startRecognition();
                btnStop.setEnabled(true);
            }
        });

        // 🔇 Stop recognition
        btnStop.setOnClickListener(v -> {
            speechService.stopRecognition();
            btnStop.setEnabled(false);
        });
    }

    /// was used in the dev process before Speech was introduced.
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




        // 🔄 MOCK Data payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("class", "יוד571");

        payload.put("statusSummary", statusSummaryTextView.getText().toString());
        payload.put("disturbances", disturbanceLabel.getText().toString());

        payload.put("rawTranscript", rawTranscripts);
        //payload.put("timestamp", System.currentTimeMillis());

        // 🪄 Write using the clean FBRef
        refPresUidCurrentWeek
                .child(timestampAndLesson)
                .setValue(payload)
                .addOnSuccessListener(unused -> Log.i("PresenceActivity", "Upload success"))
                .addOnFailureListener(e -> Log.e("PresenceActivity", "Upload failed", e));
    }


    /// was used in the dev process before speech was introduced.
    /// to work with this you need to revive the btnDebug that was
    /// btn deleted on build of 18Jul 07:48am
    private void populateMockData() {
        // Set class name
        classNameLabel.setText("כיתה: יוד571");

        // set raw data too:
        rawTranscripts.clear();
        rawTranscripts.add("Alice is present");
        rawTranscripts.add("Bob is also here");
        rawTranscripts.add("I think Charlie said something funny");


        // Set detected names
        StringBuilder detected = new StringBuilder("Recorded Names:");
        for (String name : mockDetectedNames) {
            detected.append("\n• ").append(name);
        }

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
     * - L1 starts at 08:30
     * - 50-minute slots (roughly covers for intermissions)
     * - Round to nearest slot start using ±25 min
     * 💡 A more robust solution could work againt a list of start/end tuples representing school day schedules
     *
     * @param now current time
     * @return Pair of ("HHmm" rounded start time string, lesson number), or (-1) if out of range
     */
    public static Pair<String, Integer> getRoundedTimeAndLessonSlot(Calendar now) {
        int totalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        int baseStart = 8 * 60 + 30; // 08:30 in minutes
        int maxLessons = 20;
        int lessonDuration = 50;

        int minValid = baseStart - 25;
        int maxValid = baseStart + lessonDuration * (maxLessons - 1) + 25;
// I prefer all hours of the day return some result and not -1.
//        if (totalMinutes < minValid || totalMinutes > maxValid) {
//            return new Pair<>(null, -1);  // outside school time
//        }

        int roundedIndex = Math.round((totalMinutes - baseStart) / (float) lessonDuration);
        int lessonStartMinutes = baseStart + roundedIndex * lessonDuration;

        int hour = lessonStartMinutes / 60;
        int minute = lessonStartMinutes % 60;
        String roundedHHmm = String.format("%02d%02d", hour, minute);

        return new Pair<>(roundedHHmm, roundedIndex + 1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speechService.startRecognition();
            } else {
                Toast.makeText(this,
                        "Microphone permission is required",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 🔄 Pushes a single transcript entry to RTDB using rounded-time + lesson key
     * /P{YY}_{uid}/W{current week}/{MMDDMMM_HHmmL#}/
     * /P25_tTe3W4vIjHe0HSqRXxAIUBxIzKg1/W29/0718Jul_0830L1
     */
    private void pushRawTranscript(String newEntry) {
        // 🗓 Get current time
        Calendar now = Calendar.getInstance();

        // 🔢 Build key parts
        int week = now.get(Calendar.WEEK_OF_YEAR);
        String weekStr = "W" + week;

        String MMdd = String.format(Locale.getDefault(), "%02d%02d",
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH)
        );
        String MMM = new SimpleDateFormat("MMM", Locale.ENGLISH)
                .format(now.getTime());

        Pair<String, Integer> lessonInfo = getRoundedTimeAndLessonSlot(now);
        String roundedHHmm = lessonInfo.first;
        int lesson = lessonInfo.second;
        if (lesson == -4) {
            Log.e("PresenceActivity", "Time not in valid school hours");
            return;
        }
        String timestampAndLesson = MMdd + MMM + "_" + roundedHHmm + "L" + lesson;

        // 🔄 Data payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("class", detectedClassName);

        payload.put("statusSummary", statusSummaryTextView.getText().toString());
        payload.put("disturbances", disturbanceLabel.getText().toString());

        //payload.put("detected", rawTranscripts);
        //payload.put("missing", new ArrayList<>());
        payload.put("rawTranscript", rawTranscripts);
        //payload.put("timestamp", System.currentTimeMillis());

        // 🪄 Write using the clean FBRef
        refPresUidCurrentWeek
                .child(timestampAndLesson)
                .setValue(payload)
                .addOnSuccessListener(unused -> Log.i("PresenceActivity", "Upload success"))
                .addOnFailureListener(e -> Log.e("PresenceActivity", "Upload failed", e));
    }


    /**
     * 🔍 identifying the most probable class.
     */
    private Boolean detectClass() {
        // 1. Build map of ClassName → count
        Map<String, Integer> classMatches = new HashMap<>();
        for (Student s : allStudents) {
            classMatches.putIfAbsent(s.getClassName(), 0);
        }

        /// 2. For each class, count how many nicknames appear
        ///  this is just counting how many times each nickname appears
        for (String cls : new ArrayList<>(classMatches.keySet())) {
            int count = 0;
            for (Student s : allStudents) {
                if (!s.getClassName().equals(cls)) continue;
                String nick = s.getNickName().toLowerCase(Locale.ROOT);
                if (collectedWords.contains(nick)) {
                    count++;
                }
            }
            classMatches.put(cls, count);
        }

        // 3. Find best match - identifying the most probable class.
        String bestClass = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> e : classMatches.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                bestClass = e.getKey();
            }
        }

        // 4. If we have at least one hit, update UI once
        if (bestCount >= 1 && detectedClassName == null) {
            detectedClassName = bestClass;
            runOnUiThread(() ->
                    classNameLabel.setText("כיתה: " + detectedClassName)
            );
            return true;
        }
        return false;
    }

    /**
     * Parses a single transcript line and updates student statuses.
     * Call this inside onResults() for each new full transcript.
     */
    private void analyzeTranscriptLine(String line) {
        // Normalize line
        String text = line.trim();
        if (text.isEmpty()) return;

        // Determine category keyword and student names
        String[] tokens = text.split("\\s+");
        String keyword = tokens[0];
        List<String> names = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));

        // Map Hebrew keyword to status
        String status;
        switch (keyword) {
            case "בזמן":
                status = "בזמן";
                break;
            case "איחור":
                status = "מאחרים";
                break;
            case "מאחרים":
                status = "מאחרים";
                break;
            case "חיסור":
                status = "חסרים";
            case "חסרים":
                status = "חסרים";
                break;
            case "חסרות" /* alternative form */:
                status = "חסרים";
                break;
            case "חסרה":
                status = "חסרים";
                break;
            case "חולים":
                status = "מחלה";
                break;
            case "הפרעה":
                // disturbance only, keep existing status
                disturbanceSet.addAll(names);
                updateUI();
                return;
            default:
                return; // unrecognized
        }

        // Update each student
        for (String name : names) {
            // If clearing (בזמן), remove disturbance flag and reset status only for these names
            if (status.equals("בזמן")) {
                studentStatus.put(name, "בזמן");
                //disturbanceSet.remove(name);
            } else {
                // For lateness, always overwrite missing
                studentStatus.put(name, status);
            }
        }

        // Refresh UI after each line
        updateUI();
    }

    /**
     * Builds the summary strings and updates the TextViews.
     */
    private void updateUI() {
        // Group by status
        Map<String, List<String>> grouped = new HashMap<>();
        for (Map.Entry<String, String> e : studentStatus.entrySet()) {
            grouped.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        // Build status summary
        StringBuilder summary = new StringBuilder();
        for (String key : Arrays.asList("בזמן", "מאחרים", "חסרים", "מחלה")) {
            List<String> list = grouped.get(key);
            if (list != null && !list.isEmpty()) {
                summary.append(key).append(": ").append(TextUtils.join(", ", list)).append("\n");
            }
        }

        // Build disturbance summary
        String disturbanceText = disturbanceSet.isEmpty()
                ? "הפרעות: אין"
                : "הפרעות: " + TextUtils.join(", ", disturbanceSet);

        // Apply to UI
        statusSummaryTextView.setText(summary.toString().trim());
        disturbanceLabel.setText(disturbanceText);
    }

    // In onResults(String text):
    // for (String line : text.split("\\r?\\n")) {
    //     analyzeTranscriptLine(line);
    // }

//    /**
//     * Call this from your transcript update logic to refresh the UI
//     */
//    private void updateStatusUI(String summary, String disturbances) {
//        statusSummaryTextView.setText(summary);
//        disturbanceLabel.setText(disturbances);
//    }
}