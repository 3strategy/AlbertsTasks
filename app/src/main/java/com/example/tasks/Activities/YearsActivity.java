package com.example.tasks.Activities;

import static com.example.tasks.FBRef.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tasks.FBRef;
import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Activity for managing academic years and associated classes.
 * <p>
 * This activity allows users to view, add, and select academic years. For each selected year,
 * users can also view and add classes they teach. It plays a crucial role in setting up
 * the application for new users by prompting them to add their first year and class.
 * For existing users, it allows switching between active years or adding new ones.
 * <p>
 * Features:
 * <ul>
 *     <li>Displays a {@link Spinner} to select an academic year.</li>
 *     <li>Displays a {@link ListView} to show classes for the selected year.</li>
 *     <li>Allows adding new academic years via an {@link AlertDialog}.</li>
 *     <li>Allows adding new classes for the currently selected year via an {@link AlertDialog}.</li>
 *     <li>Handles initial setup for new users, forcing them to add a year and class.</li>
 *     <li>Saves the selected active year to {@link SharedPreferences} for existing users.</li>
 *     <li>Returns the selected active year to {@link LoginActivity} for new users.</li>
 *     <li>Interacts with Firebase Realtime Database to store and retrieve year and class data.</li>
 *     <li>Implements context menu (via {@link #onItemClick} on ListView) for deleting classes.</li>
 * </ul>
 *
 * @see MasterActivity
 * @see SharedPreferences
 * @see AlertDialog
 * @see FBRef
 */
public class YearsActivity extends MasterActivity implements AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {
    private Spinner spYears;
    private ListView lVClasses;
    private Intent gi;
    private boolean isNewUser;
    private int activeYear, newYear;
    private String newClass;
    private ArrayList<Integer> years;
    private ArrayList<String> classes;
    private ArrayAdapter<Integer> yearsAdp;
    private ArrayAdapter<String> classesAdp;
    private ValueEventListener velYears = new ValueEventListener() {
        /**
         * Called when data for the list of years changes in Firebase.
         * Updates the {@code years} ArrayList and notifies the {@code yearsAdp} to refresh the spinner.
         * Sets the spinner selection to the {@code activeYear} if it's valid.
         *
         * @param dS The DataSnapshot containing the updated list of years.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dS) {
            years.clear();
            for(DataSnapshot data : dS.getChildren()) {
                years.add(Integer.parseInt(data.getKey()));
            }
            yearsAdp.notifyDataSetChanged();
            if (activeYear != 1970) {
                spYears.setSelection(years.indexOf(activeYear));
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    };
    private ValueEventListener velClass = new ValueEventListener() {
        /**
         * Called when data for the list of classes for the {@code activeYear} changes in Firebase.
         * Updates the {@code classes} ArrayList and notifies the {@code classesAdp} to refresh the ListView.
         *
         * @param dS The DataSnapshot containing the updated list of classes for the active year.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dS) {
            classes.clear();
            for(DataSnapshot data : dS.getChildren()) {
                classes.add(data.getValue(String.class));
            }
            classesAdp.notifyDataSetChanged();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    };
    private SharedPreferences settings;

    /**
     * Called when the activity is first created.
     * Initializes views, SharedPreferences, and sets up the initial state
     * based on whether the user is new or existing.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_years);

        settings = getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
    }

    /**
     * Initializes all UI view components and their adapters.
     * Retrieves intent extras to determine if the user is new.
     * If the user is new, it sets a default {@code activeYear} and immediately prompts
     * to add a new year. Otherwise, it loads the previously active year from SharedPreferences.
     */
    private void initViews() {
        spYears = findViewById(R.id.spYears);
        lVClasses = findViewById(R.id.lVClasses);
        years = new ArrayList<>();
        years.clear();
        classes = new ArrayList<>();
        classes.clear();
        yearsAdp = new ArrayAdapter<>(YearsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, years);
        spYears.setAdapter(yearsAdp);
        spYears.setOnItemSelectedListener(this);
        classesAdp = new ArrayAdapter<>(YearsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, classes);
        lVClasses.setAdapter(classesAdp);
        lVClasses.setOnItemClickListener(this);
        gi = getIntent();
        isNewUser = gi.getBooleanExtra("isNewUser", false);
        if (isNewUser) {
            activeYear = 1970;
            addNewYear(null);
        } else {
            activeYear = settings.getInt("activeYear",1970);
        }
    }

    /**
     * Called when the activity is becoming visible to the user.
     * Attaches Firebase ValueEventListeners to listen for changes in years and classes,
     * but only if it's not a new user and a valid {@code activeYear} is set.
     * For new users, listeners are typically attached after they add their first year/class.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (!isNewUser && activeYear != 1970) {
            refYears.addValueEventListener(velYears);
            refYears.child(String.valueOf(activeYear)).addValueEventListener(velClass);
        }
    }

    /**
     * Called when the activity is no longer visible to the user.
     * Removes the Firebase ValueEventListeners to prevent memory leaks and
     * unnecessary background updates.
     */
    @Override
    protected void onStop() {
        super.onStop();
        refYears.removeEventListener(velYears);
    }

    /**
     * Displays an {@link AlertDialog} to prompt the user to enter a new academic year.
     * The year is expected to be the year when the summer vacation occurs (e.g., 2024 for 2023-2024).
     * <p>
     * If the entered year doesn't already exist, it's added to Firebase.
     * For new users, after adding the first year, it automatically calls {@link #addNewClass(View)}
     * to prompt for adding the first class.
     * If adding a new year, the {@code velYears} listener is attached here if not already.
     *
     * @param view The view that triggered this action (can be null if called programmatically).
     */
    public void addNewYear(View view) {
        AlertDialog.Builder adb =new AlertDialog.Builder(YearsActivity.this);
        adb.setTitle("Add new year");
        adb.setMessage("Enter the new year number:\n(when the summer vacation occur)");
        final EditText eT = new EditText(this.getApplicationContext());
        eT.setInputType(InputType.TYPE_CLASS_NUMBER);
        Calendar calNow = Calendar.getInstance();
        eT.setHint(String.valueOf(1+calNow.get(Calendar.YEAR)));
        adb.setView(eT);
        adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newYear = Integer.parseInt(eT.getText().toString());
                if (years.contains(newYear)){
                    Toast.makeText(YearsActivity.this, "Year already exist!\nTry again", Toast.LENGTH_SHORT).show();
                } else {
                    refYears.child(String.valueOf(newYear)).setValue("Null");
                    dialog.dismiss();
                    if (isNewUser){
                        activeYear = newYear;
                        addNewClass(null);
                    }
                }
            }
        });
        adb.setNeutralButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        adb.setCancelable(false);
        adb.create().show();
    }

    /**
     * Displays an {@link AlertDialog} to prompt the user to enter a new class name
     * for the currently {@code activeYear}.
     * <p>
     * If the class doesn't already exist for that year, it's added to the list of classes
     * in Firebase for the {@code activeYear}.
     * If adding the first class for a year, the {@code velClass} listener is attached here if not already.
     *
     * @param view The view that triggered this action (can be null if called programmatically).
     */
    public void addNewClass(View view) {
        if (activeYear != 1970){
            AlertDialog.Builder adb =new AlertDialog.Builder(YearsActivity.this);
            adb.setTitle("Add new class");
            adb.setMessage("Enter the new class you teach:");
            final EditText eT = new EditText(this.getApplicationContext());
//            eT.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            adb.setView(eT);
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newClass = eT.getText().toString();
                    if (classes.contains(newClass)){
                        Toast.makeText(YearsActivity.this, "Class already exist!\nTry again", Toast.LENGTH_SHORT).show();
                    } else {
                        classes.add(newClass);
                        refYears.child(String.valueOf(activeYear)).setValue(classes);
                        dialog.dismiss();
                    }
                }
            });
            adb.setNeutralButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            adb.setCancelable(false);
            adb.create().show();
        }
    }

    /**
     * Finalizes the year and class selection.
     * <p>
     * For new users, it returns the {@code activeYear} as a result to the calling activity
     * (e.g., {@link LoginActivity}) and finishes this activity.
     * For existing users, it saves the {@code activeYear} to SharedPreferences and navigates
     * to {@link MainActivity}.
     * <p>
     * It prevents proceeding if no {@code activeYear} is set or (for new users) if no classes
     * have been added for that year.
     *
     * @param view The view that was clicked (the "Done" button).
     */
    public void done(View view) {
        if (activeYear != 1970) {
            if (isNewUser) {
                gi.putExtra("activeYear", activeYear);
                setResult(RESULT_OK,gi);
                finish();
            } else {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("activeYear",activeYear);
                editor.commit();
                Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(YearsActivity.this);
            adb.setTitle("Warning !!!");
            adb.setMessage("You must set the Active Year");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adb.setCancelable(false);
            adb.create().show();
        }
    }

    /**
     * Callback method to be invoked when an item in the years {@link Spinner} has been selected.
     * Updates the {@code activeYear} to the selected year.
     * Removes any existing Firebase listener for classes of the previously selected year
     * and attaches a new listener for the classes of the newly selected {@code activeYear}.
     * This method is not called for the initial selection if {@code isNewUser} is true,
     * as the flow is controlled by {@code addNewYear} and {@code addNewClass} initially.
     *
     * @param parent The AdapterView where the selection happened.
     * @param view   The view within the AdapterView that was clicked.
     * @param pos    The position of the view in the adapter.
     * @param id     The row id of the item that was selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (!isNewUser){
            activeYear = years.get(pos);
            refYears.child(String.valueOf(activeYear)).addValueEventListener(velClass);
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from the years {@link Spinner}.
     * Currently, this method has no specific implementation.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    /**
     * Callback method to be invoked when an item in the classes {@link ListView} has been clicked.
     * This is used as a context menu trigger (though not a standard long-press context menu).
     * It displays an {@link AlertDialog} to confirm deletion of the selected class.
     * If confirmed, the class is removed from Firebase for the current {@code activeYear}.
     *
     * @param parent The AdapterView where the click happened (the ListView).
     * @param view   The view within the AdapterView that was clicked.
     * @param pos    The position of the view in the adapter.
     * @param id     The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.lVClasses) {
            String choosen = classes.get(pos);
            AlertDialog.Builder adb =new AlertDialog.Builder(YearsActivity.this);
            adb.setTitle("Delete class");
            adb.setMessage("Are you sure you want to\ndelete task '"+choosen+"' ?");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    classes.remove(pos);
                    refYears.child(String.valueOf(activeYear)).setValue(classes);
                    dialog.dismiss();
                }
            });
            adb.setNeutralButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            adb.setCancelable(false);
            adb.create().show();
        }
    }

}