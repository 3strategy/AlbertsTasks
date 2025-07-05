package com.example.tasks.Activities;

import static com.example.tasks.FBRef.refTasks;
import static com.example.tasks.FBRef.refYears;
import static com.example.tasks.Utilities.db2Dsiplay;

import androidx.annotation.NonNull;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tasks.FBRef;
import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.Obj.Task;
import com.example.tasks.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Activity for creating a new task or editing an existing one.
 * <p>
 * This activity extends {@link MasterActivity} to inherit its common options menu.
 * It provides a form for users to input task details such as class name,
 * task number (serial number), start date, due date, and whether the task
 * applies to the full class.
 * <p>
 * Features:
 * <ul>
 *     <li>Supports both creating new tasks and editing existing tasks.</li>
 *     <li>Uses {@link DatePickerDialog} for selecting start and due dates.</li>
 *     <li>Populates a {@link Spinner} with class names fetched from Firebase for the active year.</li>
 *     <li>Validates that required data (dates, class name, task number) is provided.</li>
 *     <li>Saves new tasks or updates existing tasks in Firebase Realtime Database
 *         under the appropriate path based on the active year and task details.</li>
 *     <li>Prevents creation of duplicate tasks (based on current implementation of {@code Task.isIn()}).</li>
 * </ul>
 * The UI and button text ("Add Task" vs. "Set Task") change dynamically based on
 * whether the activity is in "new task" or "edit task" mode.
 *
 * @see MasterActivity
 * @see Task
 * @see DatePickerDialog
 * @see ArrayAdapter
 * @see FBRef
 */
public class TaskActivity extends MasterActivity implements AdapterView.OnItemSelectedListener {

    private TextView tVTaskHeader, tVStartDate, tVDueDate;
    private Spinner spClass;
    private EditText eTTaskNum;
    private CheckBox cbFullClass;
    private Button btnTask;
    private Intent gi;
    private boolean isNewTask, setDueDate;
    private String className, startDate, dueDate, serNum;
    private int activeYear;
    private int choose;
    private Task task;
    private ArrayList<String> classList;
    private ArrayAdapter<String> adp;

    /**
     * Called when the activity is first created.
     * Initializes views, retrieves intent extras, and fetches class data for the spinner.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        initViews();
        refYears.child(String.valueOf(activeYear)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DataSnapshot> tsk) {
                if (tsk.isSuccessful()) {
                    DataSnapshot dS = tsk.getResult();
                    classList.clear();
                    classList.add("Choose class:");
                    for(DataSnapshot data : dS.getChildren()) {
                        classList.add(data.getValue(String.class));
                    }
                    adp.notifyDataSetChanged();
                    if (!isNewTask){
                        spClass.setSelection(classList.indexOf(task.getClassName()));
                    }
                }
                else {
                    Log.e("firebase", "Error getting data", tsk.getException());
                }
            }
        });
    }

    /**
     * Initializes UI components, retrieves data from the calling intent,
     * and sets up the UI elements based on whether a new task is being created
     * or an existing one is being edited.
     * <p>
     * For new tasks, fields are empty. For editing tasks, fields are pre-populated
     * with the existing task's details. Some fields like start date and task number
     * are made non-editable when editing an existing task.
     */
    private void initViews() {
        tVTaskHeader = findViewById(R.id.tVTaskHeader);
        spClass = findViewById(R.id.spClass);
        tVStartDate = findViewById(R.id.tVStartDate);
        tVDueDate = findViewById(R.id.tVDueDate);
        eTTaskNum = findViewById(R.id.eTTaskNum);
        cbFullClass = findViewById(R.id.cbFullClass);
        btnTask = findViewById(R.id.btnTask);
        gi = getIntent();
        activeYear = gi.getIntExtra("activeYear",1970);
        if (activeYear == 1970) {
            Toast.makeText(this, "Wrong data sent", Toast.LENGTH_LONG).show();
            finish();
        }
        isNewTask = gi.getBooleanExtra("isNewTask",true);
//        if (!isNewTask){
//            choose = gi.getIntExtra("choose",-1);
//            task = MainActivity.tasksList.get(choose);
//        }
        if (isNewTask) {
            tVTaskHeader.setText("Add new Task");
            btnTask.setText("Add Task");
        } else {
            choose = gi.getIntExtra("choose",-1);
            task = MainActivity.tasksList.get(choose);
            tVTaskHeader.setText("Edit Task");
            startDate = task.getDateStart();
            tVStartDate.setText(db2Dsiplay(task.getDateStart()));
            tVStartDate.setClickable(false);
            dueDate = task.getDateEnd();
            tVDueDate.setText(db2Dsiplay(task.getDateEnd()));
            eTTaskNum.setText(task.getSerNum());
            eTTaskNum.setEnabled(false);
            eTTaskNum.setInputType(InputType.TYPE_NULL);
            cbFullClass.setChecked(task.isFullClass());
            btnTask.setText("Set Task");
        }
        classList = new ArrayList<String>();
        classList.add("Choose class:");
        adp = new ArrayAdapter<>(TaskActivity.this,
                android.R.layout.simple_spinner_dropdown_item, classList);
        spClass.setAdapter(adp);
        spClass.setOnItemSelectedListener(this);
    }


    /**
     * Handles clicks on the start date or due date TextViews to open a {@link DatePickerDialog}.
     * Sets a flag {@code setDueDate} to indicate which date field is being selected.
     *
     * @param view The TextView (start date or due date) that was clicked.
     */
    public void datePick(View view) {
        if (view.getId() == R.id.tVDueDate){
            setDueDate = true;
        } else {
            setDueDate = false;
        }
        openDatePickerDialog();
    }

    /**
     * Opens a {@link DatePickerDialog} allowing the user to select a date.
     * The dialog is initialized with the current system date.
     * The selected date is handled by {@link #onDateSetListener}.
     */
    private void openDatePickerDialog() {
        Calendar calNow = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                calNow.get(Calendar.YEAR),
                calNow.get(Calendar.MONTH),
                calNow.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Choose date");
        datePickerDialog.show();
    }
    /**
     * Listener for handling date selection from the {@link DatePickerDialog}.
     * <p>
     * When a date is set, it updates either the {@code startDate} or {@code dueDate}
     * field and the corresponding TextView, based on the {@code setDueDate} flag.
     * Dates are stored in "yyyyMMdd" format and displayed in "dd-MM-yyyy" format.
     */
    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();

            calSet.set(Calendar.YEAR, year);
            calSet.set(Calendar.MONTH, month);
            calSet.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdfSave = new SimpleDateFormat("yyyyMMdd");
            String dateSave = sdfSave.format(calSet.getTime());
            if (setDueDate) {
                dueDate = dateSave;
                tVDueDate.setText(db2Dsiplay(dateSave));
            } else {
                startDate = dateSave;
                tVStartDate.setText(db2Dsiplay(dateSave));
            }
        }
    };

    /**
     * Handles the confirmation (Add/Set Task button click).
     * Validates input fields. If all required data is present:
     * <ul>
     *     <li>For new tasks: Creates a new {@link Task} object and saves it to Firebase,
     *         checking for duplicates first.</li>
     *     <li>For existing tasks: Updates the existing task in Firebase. This involves
     *         removing the old task entry and adding the updated one, especially if
     *         key details like due date or 'full class' status (which affect path) change.</li>
     * </ul>
     * Finishes the activity upon successful save/update.
     *
     * @param view The view that was clicked (the confirmation button).
     */
    public void confirmation(View view) {
        int taskNum = Integer.parseInt(eTTaskNum.getText().toString());
        serNum = String.format("%02d",taskNum);
        if (startDate == null || dueDate == null || className == null || serNum == null) {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
        } else {
            Task newTask = new Task(startDate, dueDate,className, serNum, activeYear, cbFullClass.isChecked());
            if (!isNewTask) {
                refTasks.child(String.valueOf(activeYear))
                        .child(String.valueOf(!task.isFullClass()))
                        .child(task.getDateEnd())
                        .child(task.getClassName()+task.getSerNum())
                        .removeValue();
                refTasks.child(String.valueOf(activeYear))
                        .child(String.valueOf(!cbFullClass.isChecked()))
                        .child(dueDate)
                        .child(className+serNum)
                        .setValue(newTask);
                finish();
            } else if (newTask.isIn(MainActivity.tasksList)) {
                Toast.makeText(this, "Task already exist!", Toast.LENGTH_SHORT).show();
            } else {
                refTasks.child(String.valueOf(activeYear))
                        .child(String.valueOf(!cbFullClass.isChecked()))
                        .child(dueDate)
                        .child(className+serNum)
                        .setValue(newTask);
                finish();
            }
        }
    }


    /**
     * Callback method to be invoked when an item in the class {@link Spinner} has been selected.
     * Updates the {@code className} field if a valid class (not the default prompt) is selected.
     *
     * @param parent   The AdapterView where the selection happened.
     * @param view     The view within the AdapterView that was clicked.
     * @param pos      The position of the view in the adapter.
     * @param id       The row id of the item that was selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (pos != 0) {
            className = classList.get(pos);
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from the class {@link Spinner}.
     * This might happen when there are no items to select, or the adapter becomes empty.
     * Shows a {@link Toast} message.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Nothing selected...", Toast.LENGTH_SHORT).show();
    }
}