package com.example.tasks;

import com.example.tasks.models.Student;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.tasks.models.Student;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// author: Guy Siedes 3strategy@gmail.com
// with GPT o4 mini high, private chat  :  https://chatgpt.com/c/6878cad2-5d50-800e-8499-6db3a8fb9d88
// usage guideline: https://מבני.שלי.com/android/projectSteps/newFBref
public class FBRef {

    // all students is loaded asynchronouly from RTDB during Login, and openning of the
    // PresenceActivity is conditioned by its completion.
    public static List<Student> allStudents = new ArrayList<>();

    // ─── auth & root DB ───────────────────────────────────────────────
    public static FirebaseAuth       refAuth     = FirebaseAuth.getInstance();
    public static FirebaseDatabase   FBDB        = FirebaseDatabase.getInstance();

    // ─── your existing roots ─────────────────────────────────────────
    /** root of all users; you still do refUsers.child(uid)… elsewhere */
    public static DatabaseReference  refUsers    = FBDB.getReference("Users");
    public static DatabaseReference  refTasks,
            refDoneTasks,
            refYears,
            refStudents,
            refMaakav;

    // NEW: smart tree root: P{YY}.{uid}
    public static DatabaseReference  refPresenceRoot,
            refStudentsYear,
            refMaakavYear;

    public static DatabaseReference refPresUidCurrentWeek;


    public static String uid;

    /**
     * Call on login to set uid + your existing refs,
     * and also set up Students/Presence/Maakav for this user.
     */
    public static void getUser(FirebaseUser fbuser) {
        uid            = fbuser.getUid();
        refTasks       = FBDB.getReference("Tasks").child(uid);
        refDoneTasks   = FBDB.getReference("Done_Tasks").child(uid);
        refYears       = FBDB.getReference("Years").child(uid);

        // ─── NEW branches ───
        refStudents    = FBDB.getReference("Students").child(uid);
        refMaakav      = FBDB.getReference("Maakav").child(uid);

        int activeYear = Calendar.getInstance().get(Calendar.YEAR);  // or SharedPreferences.getInt(...)
        setActiveYear(activeYear);
    }

    /**
     * Once you know activeYear (e.g. from SharedPreferences),
     * call this so your three new branches narrow to {uid}/{year}.
     */
    public static void setActiveYear(int activeYear) {
        String yy = String.valueOf(activeYear).substring(2);     // 👈 e.g. "25"
        String rootKey = "P" + yy + "_" + uid;                    // 👈 P25.abcd123
        refPresenceRoot = FBDB.getReference(rootKey);            // 👈 single root for that teacher-year
        refStudentsYear = refStudents.child(String.valueOf(activeYear));
        refMaakavYear   = refMaakav.child(String.valueOf(activeYear));

        // Also set current week subnode reference
        int week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
        refPresUidCurrentWeek = refPresenceRoot.child("W" + week);  // e.g., P25.abcd123/W29
    }


    public static void loadAllStudents(final Runnable onComplete) {
        allStudents.clear();
        refStudentsYear.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    allStudents.add(child.getValue(Student.class));
                }
                if (onComplete != null) onComplete.run();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("FBRef", "Failed to load students", error.toException());
                if (onComplete != null) onComplete.run();
            }
        });
    }
    /**
     * Convenience overload: do both in one call.
     */
//    public static void getUser(FirebaseUser fbuser, int activeYear) {
//        getUser(fbuser);
//        setActiveYear(activeYear);
//    }
}
