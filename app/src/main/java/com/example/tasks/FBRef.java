package com.example.tasks;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Utility class for managing Firebase Realtime Database and Authentication references.
 *
 * <p>This class provides static references to Firebase services such as {@link FirebaseAuth}
 * and {@link FirebaseDatabase}. It also offers helper methods to initialize database
 * references specific to the currently authenticated user.
 *
 * <p>The database references are structured to store user-specific data under
 * distinct paths like "Users", "Tasks", "Done_Tasks", and "Years".
 * The {@link #getUser(FirebaseUser)} method should be called after a user
 * successfully signs in to set up the user-specific database paths.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * // After user authentication:
 * FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
 * if (currentUser != null) {
 *     FBRef.getUser(currentUser);
 *     // Now you can use FBRef.refTasks, FBRef.refDoneTasks, etc.
 *     FBRef.refTasks.child("newTask").setValue("Task details");
 * }
 * }
 * </pre>
 */
public class FBRef {
    /**
     * Static reference to the Firebase Authentication service.
     * Used for user authentication operations like sign-in, sign-up, and sign-out.
     */
    public static FirebaseAuth refAuth=FirebaseAuth.getInstance();

    /**
     * Static reference to the Firebase Realtime Database service.
     * Used as the entry point for accessing the database.
     */
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    /**
     * Static reference to the "Users" node in the Firebase Realtime Database.
     * This node can be used to store general user information.
     */
    public static DatabaseReference refUsers=FBDB.getReference("Users");
    /**
     * Static reference to the current user's "Tasks" node in the Firebase Realtime Database.
     * This reference is initialized by calling {@link #getUser(FirebaseUser)} and points to
     * "Tasks/{userId}". It is used to store active tasks for the current user.
     * This field will be null until {@link #getUser(FirebaseUser)} is called.
     */
    public static DatabaseReference refTasks;
    /**
     * Static reference to the current user's "Done_Tasks" node in the Firebase Realtime Database.
     * This reference is initialized by calling {@link #getUser(FirebaseUser)} and points to
     * "Done_Tasks/{userId}". It is used to store completed tasks for the current user.
     * This field will be null until {@link #getUser(FirebaseUser)} is called.
     */
    public static DatabaseReference refDoneTasks;
    /**
     * Static reference to the current user's "Years" node in the Firebase Realtime Database.
     * This reference is initialized by calling {@link #getUser(FirebaseUser)} and points to
     * "Years/{userId}". It can be used to store year-specific data for the current user.
     * This field will be null until {@link #getUser(FirebaseUser)} is called.
     */
    public static DatabaseReference refYears;

    /**
     * Stores the unique ID (UID) of the currently authenticated Firebase user.
     * This field is populated by the {@link #getUser(FirebaseUser)} method.
     * It will be null until {@link #getUser(FirebaseUser)} is called with a non-null user.
     */
    public static String uid;

    /**
     * Initializes user-specific Firebase Realtime Database references.
     * This method should be called after a user successfully authenticates.
     * It sets the {@link #uid} and initializes {@link #refTasks},
     * {@link #refDoneTasks}, and {@link #refYears} to point to the
     * respective data paths for the provided Firebase user.
     *
     * @param fbuser The currently authenticated {@link FirebaseUser}.
     *               If null, the behavior of subsequent database operations
     *               using user-specific references is undefined (likely to cause errors).
     */
    public static void getUser(FirebaseUser fbuser){
        uid = fbuser.getUid();
        refTasks=FBDB.getReference("Tasks").child(uid);
        refDoneTasks=FBDB.getReference("Done_Tasks").child(uid);
        refYears=FBDB.getReference("Years").child(uid);
    }
}
