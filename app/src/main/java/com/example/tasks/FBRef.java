package com.example.tasks;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

// author: Guy Siedes 3strategy@gmail.com
// with GPT o4 mini high, private chat  :  https://chatgpt.com/c/6878cad2-5d50-800e-8499-6db3a8fb9d88
// usage guideline: https://מבני.שלי.com/android/projectSteps/newFBref
public class FBRef {

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


    /**
     * Convenience overload: do both in one call.
     */
//    public static void getUser(FirebaseUser fbuser, int activeYear) {
//        getUser(fbuser);
//        setActiveYear(activeYear);
//    }
}
