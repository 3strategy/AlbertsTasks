package com.example.tasks;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            refYears;

    // ─── NEW: per-user branches ────────────────────────────────────────
    public static DatabaseReference  refStudents,
            refPresence,
            refMaakav;

    // ─── NEW: per-user per-year branches ──────────────────────────────
    public static DatabaseReference  refStudentsYear,
            refPresenceYear,
            refMaakavYear;

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
        refPresence    = FBDB.getReference("Presence").child(uid);
        refMaakav      = FBDB.getReference("Maakav").child(uid);
    }

    /**
     * Once you know activeYear (e.g. from SharedPreferences),
     * call this so your three new branches narrow to {uid}/{year}.
     */
    public static void setActiveYear(int activeYear) {
        String year = String.valueOf(activeYear);
        refStudentsYear   = refStudents.child(year);
        refPresenceYear   = refPresence.child(year);
        refMaakavYear     = refMaakav.child(year);
    }

    /**
     * Convenience overload: do both in one call.
     */
    public static void getUser(FirebaseUser fbuser, int activeYear) {
        getUser(fbuser);
        setActiveYear(activeYear);
    }
}
