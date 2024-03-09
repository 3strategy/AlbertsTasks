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

import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.Obj.Task;
import com.example.tasks.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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

    private void initViews() {
        tVTaskHeader = findViewById(R.id.tVTaskHeader);
        spClass = findViewById(R.id.spClass);
        tVStartDate = findViewById(R.id.tVStartDate);
        tVDueDate = findViewById(R.id.tVDueDate);
        eTTaskNum = findViewById(R.id.eTTaskNum);
        cbFullClass = findViewById(R.id.cbFullClass);
        btnTask = findViewById(R.id.btnTask);
        gi = getIntent();
        isNewTask = gi.getBooleanExtra("isNewTask",true);
        activeYear = gi.getIntExtra("activeYear",1970);
        if (activeYear == 1970) {
            Intent si = new Intent(TaskActivity.this,YearsActivity.class);
            startActivity(si);
        }
        if (!isNewTask){
            choose = gi.getIntExtra("choose",-1);
            task = MainActivity.tasksList.get(choose);
        }
        if (isNewTask) {
            tVTaskHeader.setText("Add new Task");
            btnTask.setText("Add Task");
        } else {
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

    public void datePick(View view) {
        if (view.getId() == R.id.tVDueDate){
            setDueDate = true;
        } else {
            setDueDate = false;
        }
        openDatePickerDialog();
    }

    private void openDatePickerDialog() {
        Calendar calNow = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                calNow.get(Calendar.YEAR),
                calNow.get(Calendar.MONTH),
                calNow.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Choose date");
        datePickerDialog.show();
    }
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (pos != 0) {
            className = classList.get(pos);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Nothing selected...", Toast.LENGTH_SHORT).show();
    }
}