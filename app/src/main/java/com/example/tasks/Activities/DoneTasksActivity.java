package com.example.tasks.Activities;

import static com.example.tasks.FBRef.refDoneTasks;
import static com.example.tasks.FBRef.refTasks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
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

public class DoneTasksActivity extends MasterActivity {

    private TextView tVClass, tVChecked;
    private ListView lVDone;
    private ProgressDialog pd;
    private ArrayList<Task> doneTasksList;
    private DoneTaskAdapter doneTaskAdp;
    private SharedPreferences settings;

    int activeYear;
    private ValueEventListener vel;
    private boolean orderChecked = false;
    private boolean orderClass = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_tasks);

        settings=getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refDoneTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    @Override
    protected void onStop() {
        super.onStop();
        refDoneTasks.child(String.valueOf(activeYear)).removeEventListener(vel);
    }

    private void initViews() {
        tVClass = findViewById(R.id.tVClass);
        tVChecked = findViewById(R.id.tVChecked);
        lVDone = findViewById(R.id.lVDone);
        activeYear = settings.getInt("activeYear",1970);
        pd= ProgressDialog.show(this,"Connecting Database","Gathering data...",true);

        doneTasksList = new ArrayList<Task>();
        doneTaskAdp = new DoneTaskAdapter(DoneTasksActivity.this, doneTasksList);
        lVDone.setAdapter(doneTaskAdp);
        vel = new ValueEventListener() {
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
}