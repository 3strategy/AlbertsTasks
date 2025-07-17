package com.example.tasks.Activities;

import static com.example.tasks.FBRef.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tasks.FBRef;
import com.example.tasks.Obj.User;
import com.example.tasks.R;
import com.example.tasks.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Activity for handling user login and registration.
 * <p>
 * This activity allows users to either log in with existing credentials or
 * register a new account using their email and password. It interacts with
 * Firebase Authentication for user management and Firebase Realtime Database
 * to store additional user information (username).
 * <p>
 * Features:
 * <ul>
 *     <li>User login with email and password.</li>
 *     <li>New user registration with name, email, and password.</li>
 *     <li>"Stay connected" option to remember the user's login session using SharedPreferences.</li>
 *     <li>Retrieval of the last active year or setting a new active year for new users
 *         via {@link YearsActivity}.</li>
 *     <li>Dynamic UI changes to switch between login and registration forms.</li>
 * </ul>
 * Upon successful login or registration, the user is navigated to {@link MainActivity}.
 *
 * @see AppCompatActivity
 * @see FirebaseAuth
 * @see FirebaseDatabase
 * @see SharedPreferences
 */
public class LoginActivity extends AppCompatActivity {

    private TextView tVtitle, tVregister;
    private EditText eTname, eTemail, eTpass;
    private CheckBox cBstayconnect;
    private Button btn;

    private String name, email, password, uid;
    private User userdb;
    private Boolean stayConnect, registered;
    private SharedPreferences settings;
    private int activeYear = 1970;
    private final int REQUEST_CODE = 100;

    /**
     * Called when the activity is first created.
     * Initializes views, SharedPreferences, and sets up the initial UI state for login.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        settings=getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
        stayConnect=false;
        registered=true;
        regoption();
    }

    /**
     * Initializes all UI view components by finding them by their ID.
     */
    private void initViews() {
        tVtitle=(TextView) findViewById(R.id.tVtitle);
        eTname=(EditText)findViewById(R.id.eTname);
        eTemail=(EditText)findViewById(R.id.eTemail);
        eTpass=(EditText)findViewById(R.id.eTpass);
        cBstayconnect=(CheckBox)findViewById(R.id.cBstayconnect);
        tVregister=(TextView) findViewById(R.id.tVregister);
        btn=(Button)findViewById(R.id.btn);
    }

    /**
     * Called when the activity is becoming visible to the user.
     * <p>
     * Checks if a user is already logged in and if the "stay connected" option
     * was previously selected. If both conditions are true, it bypasses the login screen
     * and navigates directly to {@link MainActivity}.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Boolean isChecked=settings.getBoolean("stayConnect",false);
        Intent si = new Intent(LoginActivity.this, PresenceActivity.class);
        if (refAuth.getCurrentUser()!=null && isChecked) {
            FBRef.getUser(refAuth.getCurrentUser());
            stayConnect=true;
            si.putExtra("isNewUser", false);
            // only go to PresenceActivity once students are loaded
            FBRef.loadAllStudents(() -> startActivity(si));
        }
    }

    /**
     * Called when the activity is no longer visible to the user.
     * If the {@code stayConnect} flag is true (meaning the user logged in and chose
     * to be remembered, and was not auto-logged out), this activity is finished.
     * This typically happens if MainActivity is started from here.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (stayConnect) finish();
    }

    /**
     * Sets up the clickable text ("Register here!") to switch to registration mode.
     * When clicked, it updates the UI to show the registration form elements
     * (e.g., name field, changes button text) and sets {@code registered} to false.
     * It then calls {@link #logoption()} to set up the "Login here!" text.
     */
    private void regoption() {
        SpannableString ss = new SpannableString("Don't have an account?  Register here!");
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                tVtitle.setText("Register");
                eTname.setVisibility(View.VISIBLE);
                btn.setText("Register");
                registered=false;
                logoption();
            }
        };
        ss.setSpan(span, 24, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tVregister.setText(ss);
        tVregister.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Sets up the clickable text ("Login here!") to switch to login mode.
     * When clicked, it updates the UI to show the login form elements
     * (e.g., hides name field, changes button text) and sets {@code registered} to true.
     * It then calls {@link #regoption()} to set up the "Register here!" text.
     */
    private void logoption() {
        SpannableString ss = new SpannableString("Already have an account?  Login here!");
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                tVtitle.setText("Login");
                eTname.setVisibility(View.INVISIBLE);
                btn.setText("Login");
                registered=true;
                regoption();
            }
        };
        ss.setSpan(span, 26, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tVregister.setText(ss);
        tVregister.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Handles the login or registration process when the main button is clicked.
     * <p>
     * If {@code registered} is true, it attempts to sign in the user with Firebase Authentication
     * using the provided email and password.
     * If {@code registered} is false, it attempts to create a new user account with Firebase
     * Authentication and stores the user's name and UID in the Firebase Realtime Database.
     * <p>
     * Displays a {@link ProgressDialog} during the Firebase operations.
     * On successful login/registration, it saves the "stay connected" preference,
     * handles active year retrieval/setting, and navigates to {@link MainActivity}.
     * Toasts are shown for success or failure messages.
     *
     * @param view The view that was clicked (the login/register button).
     */
    public void logorreg(View view) {
        if (registered) {
            email=eTemail.getText().toString();
            password=eTpass.getText().toString();

            final ProgressDialog pd=ProgressDialog.show(this,"Login","Connecting...",true);
            refAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.dismiss();
                            if (task.isSuccessful()) {
                                FBRef.getUser(refAuth.getCurrentUser());
                                Log.d("PresenceActivity", "signinUserWithEmail:success");
                                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                                settings = getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
                                activeYear = settings.getInt("activeYear",1970);
                                if (activeYear == 1970) {
                                    lostData();
                                } else {
                                    SharedPreferences.Editor editor=settings.edit();
                                    editor.putBoolean("stayConnect",cBstayconnect.isChecked());
                                    editor.commit();
                                    final Intent si = new Intent(LoginActivity.this, PresenceActivity.class);
                                    // delay navigation until students are ready
                                    FBRef.loadAllStudents(() -> startActivity(si));
                                }
                            } else {
                                Log.d("PresenceActivity", "signinUserWithEmail:fail");
                                Toast.makeText(LoginActivity.this, "e-mail or password are wrong!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            name=eTname.getText().toString();
            email=eTemail.getText().toString();
            password=eTpass.getText().toString();

            final ProgressDialog pd=ProgressDialog.show(this,"Register","Registering...",true);
            refAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.dismiss();
                            if (task.isSuccessful()) {
                                FirebaseUser user = refAuth.getCurrentUser();
                                FBRef.getUser(user);
                                Log.d("PresenceActivity", "createUserWithEmail:success");
                                uid = user.getUid();
                                userdb=new User(uid,name);
                                refUsers.child(uid).setValue(userdb);
                                Toast.makeText(LoginActivity.this, "Successful registration", Toast.LENGTH_SHORT).show();
                                setActiveYear();
                            } else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException)
                                    Toast.makeText(LoginActivity.this, "User with e-mail already exist!", Toast.LENGTH_SHORT).show();
                                else {
                                    Log.w("PresenceActivity", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "User create failed.",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Attempts to retrieve the last active year for the user from Firebase.
     * This method is called if a logged-in user's active year is not found in local SharedPreferences.
     * <p>
     * It queries the "Years" node in Firebase for the latest entry.
     * If an active year is found, it's saved to SharedPreferences, and the user is navigated to {@link PresenceActivity}.
     * If no active year is found (e.g., for a user who previously registered but didn't set an active year),
     * it calls {@link #setActiveYear()} to prompt the user to choose one.
     * @deprecated This method seems to fetch a global last year, not user-specific.
     *             Consider fetching user-specific active year from their profile or redesigning.
     *             If it's intended to be a global last year, its usage after login is questionable.
     */
    private void lostData() {
        Query query = refYears.orderByKey().limitToLast(1);
        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DataSnapshot> tsk) {
                if (tsk.isSuccessful()) {
                    DataSnapshot dS = tsk.getResult();
                    for(DataSnapshot data : dS.getChildren()) {
                        activeYear = Integer.parseInt(data.getKey());
                    }
                    if (activeYear == 1970) {
                        setActiveYear();
                    } else {
                        SharedPreferences.Editor editor=settings.edit();
                        editor.putInt("activeYear",activeYear);
                        editor.putBoolean("stayConnect",cBstayconnect.isChecked());
                        editor.commit();
                        Intent si = new Intent(LoginActivity.this,PresenceActivity.class);
                        startActivity(si);
                    }
                }
                else {
                    Log.e("firebase", "Error getting data", tsk.getException());
                }
            }
        });
    }

    /**
     * Navigates to {@link YearsActivity} to allow a new user to set their initial active year.
     * The result (the selected year) is expected back in {@link #onActivityResult(int, int, Intent)}.
     */
    private void setActiveYear() {
        Intent sifr = new Intent(LoginActivity.this,YearsActivity.class);
        sifr.putExtra("isNewUser",true);
        startActivityForResult(sifr, REQUEST_CODE);
    }

    /**
     * Callback for the result from starting an activity with {@link #startActivityForResult(Intent, int)}.
     * <p>
     * This method handles the result from {@link YearsActivity}. If the result is {@code Activity.RESULT_OK}
     * and a valid active year is returned, it saves this year to SharedPreferences,
     * updates the "stay connected" preference, and navigates to {@link PresenceActivity}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    activeYear = data.getIntExtra("activeYear", 1970);
                    SharedPreferences.Editor editor=settings.edit();
                    editor.putInt("activeYear",activeYear);
                    editor.putBoolean("stayConnect",cBstayconnect.isChecked());
                    editor.commit();
                    final Intent si = new Intent(LoginActivity.this, PresenceActivity.class);
                    // delay navigation until students are ready
                    FBRef.loadAllStudents(() -> startActivity(si));
                }
            }
        }
    }

}