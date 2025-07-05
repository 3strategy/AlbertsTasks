package com.example.tasks.Activities;

import static com.example.tasks.FBRef.refDoneTasks;
import static com.example.tasks.FBRef.refTasks;
import static com.example.tasks.FBRef.refYears;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.tasks.Adapters.DoneTaskAdapter;
import com.example.tasks.Adapters.TaskAdapter;
import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.Obj.Task;
import com.example.tasks.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Activity for displaying tasks that have been marked as "Done".
 * <p>
 * This activity extends {@link MasterActivity} to inherit its common options menu.
 * It fetches and displays a list of completed tasks from the Firebase Realtime Database
 * node "Done_Tasks". Users can filter the displayed tasks by academic year using a
 * {@link Spinner}. The list of done tasks can also be sorted by class name or by the
 * date they were marked as done.
 * <p>
 * Features:
 * <ul>
 *     <li>Displays a {@link Spinner} to select an academic year to view its done tasks.</li>
 *     <li>Fetches and lists done tasks using a {@link DoneTaskAdapter}.</li>
 *     <li>Allows sorting of the displayed tasks by "Class Name" (ascending/descending).</li>
 *     <li>Allows sorting of the displayed tasks by "Date Checked" (ascending/descending).</li>
 *     <li>Shows a {@link ProgressDialog} while initially fetching data for a selected year.</li>
 *     <li>Retrieves the initially active year from {@link SharedPreferences}.</li>
 * </ul>
 *
 * @see MasterActivity
 * @see DoneTaskAdapter
 * @see Task
 * @see SharedPreferences
 * @see com.example.tasks.FBRef
 */
public class DoneTasksActivity extends MasterActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spYear;
    private TextView tVClass, tVChecked;
    private ListView lVDone;
    private ProgressDialog pd;
    private ArrayList<Integer> years;
    private ArrayAdapter<Integer> yearsAdp;
    private ArrayList<Task> doneTasksList;
    private DoneTaskAdapter doneTaskAdp;
    private SharedPreferences settings;
    private int activeYear;
    private ValueEventListener vel;
    private boolean orderChecked = false;
    private boolean orderClass = false;
    /**
     * Listener for fetching the list of available academic years from Firebase.
     * Populates the years spinner.
     */
    private ValueEventListener velYears = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dS) {
            years.clear();
            for(DataSnapshot data : dS.getChildren()) {
                years.add(Integer.parseInt(data.getKey()));
            }
            yearsAdp.notifyDataSetChanged();
            if (activeYear != 1970) {
                spYear.setSelection(years.indexOf(activeYear));
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    };

    /**
     * Called when the activity is first created.
     * Initializes views, SharedPreferences, and sets up adapters.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_tasks);

        settings=getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
    }


    /**
     * Called when the activity is becoming visible to the user.
     * If a valid {@code activeYear} is set, it attaches a Firebase ValueEventListener
     * to listen for changes in the done tasks for that year.
     */
    @Override
    protected void onStart() {
        super.onStart();
        refDoneTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    /**
     * Called when the activity is no longer visible to the user.
     * Removes the Firebase ValueEventListener for done tasks to prevent memory leaks
     * and unnecessary background updates.
     */
    @Override
    protected void onStop() {
        super.onStop();
        refDoneTasks.child(String.valueOf(activeYear)).removeEventListener(vel);
    }

    /**
     * Initializes UI components, adapters, and the primary ValueEventListener for done tasks.
     * Retrieves the {@code activeYear} from SharedPreferences and fetches the list of
     * available years for the spinner.
     */
    private void initViews() {
        spYear = findViewById(R.id.spYears);
        tVClass = findViewById(R.id.tVClass);
        tVChecked = findViewById(R.id.tVChecked);
        lVDone = findViewById(R.id.lVDone);
        years = new ArrayList<>();
        years.clear();
        yearsAdp = new ArrayAdapter<>(DoneTasksActivity.this,
                android.R.layout.simple_spinner_dropdown_item, years);
        spYear.setAdapter(yearsAdp);
        spYear.setOnItemSelectedListener(this);
        activeYear = settings.getInt("activeYear",1970);
        refYears.addListenerForSingleValueEvent(velYears);
        pd= ProgressDialog.show(this,"Connecting Database","Gathering data...",true);

        doneTasksList = new ArrayList<Task>();
        doneTaskAdp = new DoneTaskAdapter(DoneTasksActivity.this, doneTasksList);
        lVDone.setAdapter(doneTaskAdp);
        vel = new ValueEventListener() {
            /**
             * Called when data for done tasks for the {@code activeYear} changes in Firebase.
             * Clears the current list and repopulates it with the fetched tasks.
             * Notifies the {@code doneTaskAdp} to refresh the ListView.
             * Dismisses the {@link ProgressDialog} after data is loaded.
             *
             * @param dS The DataSnapshot containing the done tasks.
             *           The expected structure is: Year -> DateEnded -> TaskObject.
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                doneTasksList.clear();
                for(DataSnapshot dataDate : dS.getChildren()) {
                    for(DataSnapshot data : dataDate.getChildren()) {
                        doneTasksList.add(data.getValue(Task.class));
                    }
                }
                doneTaskAdp.notifyDataSetChanged();
                if (pd != null) {
                    pd.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    /**
     * Sorts the {@code doneTasksList} by class name.
     * Toggles between ascending and descending order on subsequent clicks.
     * Updates the text of the {@code tVClass} header to indicate the current sort order.
     *
     * @param view The TextView (tVClass) that was clicked.
     */
    public void orderByClass(View view) {
        if (orderClass) {
            doneTasksList.sort((o1, o2)
                    -> o1.getClassName().compareTo(
                    o2.getClassName()));
            tVClass.setText("Class ↑");
        } else {
            doneTasksList.sort((o1, o2)
                    -> o2.getClassName().compareTo(
                    o1.getClassName()));
            tVClass.setText("Class ↓");
        }
        orderClass = !orderClass;
        doneTaskAdp.notifyDataSetChanged();
    }

    /**
     * Sorts the {@code doneTasksList} by the date they were marked as done (checked date).
     * Toggles between ascending and descending order on subsequent clicks.
     * Updates the text of the {@code tVChecked} header to indicate the current sort order.
     *
     * @param view The TextView (tVChecked) that was clicked.
     */
    public void orderByChecked(View view) {
        if (orderChecked) {
            doneTasksList.sort((o1, o2)
                    -> o1.getDateChecked().compareTo(
                    o2.getDateChecked()));
            tVChecked.setText("Checked ↑");
        } else {
            doneTasksList.sort((o1, o2)
                    -> o2.getDateChecked().compareTo(
                    o1.getDateChecked()));
            tVChecked.setText("Checked ↓");
        }
        orderChecked = !orderChecked;
        doneTaskAdp.notifyDataSetChanged();
    }

    /**
     * Callback method to be invoked when an item in the year {@link Spinner} has been selected.
     * Updates the {@code activeYear} to the newly selected year.
     * Removes the Firebase listener from the previously selected year's done tasks (if any)
     * and attaches a new listener for the done tasks of the newly selected year.
     * Shows a {@link ProgressDialog} while new data is being fetched.
     *
     * @param adapterView The AdapterView where the selection happened.
     * @param view        The view within the AdapterView that was clicked.
     * @param pos         The position of the view in the adapter.
     * @param l           The row id of the item that was selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        activeYear = years.get(pos);
        refDoneTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    /**
     * Callback method to be invoked when the selection disappears from the year {@link Spinner}.
     * Currently, this method has no specific implementation.
     *
     * @param adapterView The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}