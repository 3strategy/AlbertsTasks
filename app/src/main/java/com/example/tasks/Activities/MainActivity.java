package com.example.tasks.Activities;

import static com.example.tasks.FBRef.refDoneTasks;
import static com.example.tasks.FBRef.refTasks;
import static com.example.tasks.FBRef.refUsers;
import static com.example.tasks.Utilities.db2Dsiplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tasks.FBRef;
import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.Obj.Task;
import com.example.tasks.Obj.User;
import com.example.tasks.R;
import com.example.tasks.Adapters.TaskAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends MasterActivity implements AdapterView.OnItemClickListener,
        View.OnCreateContextMenuListener {
    private TextView tVMainHeader;
    private ListView lVMain;
    private User user;
    public static ArrayList<Task> tasksList;
    private TaskAdapter taskAdp;
    private int choose;
    private int activeYear;
    private ValueEventListener vel;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
    }

    private void initViews() {
        tVMainHeader = findViewById(R.id.tVMainHeader);
        lVMain = findViewById(R.id.lVMain);

        tasksList = new ArrayList<Task>();
        taskAdp = new TaskAdapter(MainActivity.this, tasksList);
        lVMain.setAdapter(taskAdp);
        lVMain.setOnItemClickListener(this);
        registerForContextMenu(lVMain);

        activeYear = settings.getInt("activeYear",1970);
        refUsers.child(FBRef.uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DataSnapshot> tsk) {
                if (tsk.isSuccessful()) {
                    user = tsk.getResult().getValue(User.class);
                    tVMainHeader.setText("Hi "+user.getUsername()+",\nYour active tasks:");
                }
                else {
                    Log.e("firebase", "Error getting data", tsk.getException());
                }
            }
        });
        vel = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                tasksList.clear();
                for(DataSnapshot dataFull : dS.getChildren()) {
                    for(DataSnapshot dataDate : dataFull.getChildren()) {
                        for(DataSnapshot data : dataDate.getChildren()) {
                            tasksList.add(data.getValue(Task.class));
                        }
                    }
                }
                taskAdp.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        refTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    @Override
    protected void onStop() {
        super.onStop();
        refTasks.child(String.valueOf(activeYear)).removeEventListener(vel);
    }

    public void addTask(View view) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("isNewTask",true);
        intent.putExtra("currentYear",activeYear);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        String date = db2Dsiplay(tasksList.get(pos).getDateStart());
        Toast.makeText(this, "Given in "+date, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.lVMain) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            choose = acmi.position;
            menu.setHeaderTitle("Choose Action:");
            menu.add("Edit");
            menu.add("Delete");
            menu.add("Done");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Log.i("MainActivity", "onContextItemSelected");
        String action = item.getTitle().toString();
        if (action.equals("Edit")) {
            Intent intent = new Intent(this, TaskActivity.class);
            intent.putExtra("isNewTask",false);
            intent.putExtra("currentYear",activeYear);
            intent.putExtra("choose",choose);
            startActivity(intent);
        } else if (action.equals("Delete")) {
            Task task = tasksList.get(choose);
            refTasks.child(String.valueOf(activeYear))
                    .child(String.valueOf(!task.isFullClass()))
                    .child(task.getDateEnd())
                    .child(task.getClassName()+task.getSerNum())
                    .removeValue();
        } else if (action.equals("Done")) {
            Task task = tasksList.get(choose);
            if (!task.isFullClass()){
                AlertDialog.Builder adb =new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("Mark as Done");
                adb.setMessage("The task is only for part of the class.\nAre you sure you want to mark it as Done ?");
                adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat sdfSave = new SimpleDateFormat("yyyyMMdd");
                        String dateSave = sdfSave.format(Calendar.getInstance().getTime());
                        task.setDateChecked(dateSave);
                        refDoneTasks.child(String.valueOf(activeYear)).child(task.getDateEnd()).child(task.getClassName()+task.getSerNum()).setValue(task);
                        refTasks.child(String.valueOf(activeYear))
                                .child(String.valueOf(!task.isFullClass()))
                                .child(task.getDateEnd())
                                .child(task.getClassName()+task.getSerNum())
                                .removeValue();
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
            } else {
                SimpleDateFormat sdfSave = new SimpleDateFormat("yyyyMMdd");
                String dateSave = sdfSave.format(Calendar.getInstance().getTime());
                task.setDateChecked(dateSave);
                refDoneTasks.child(String.valueOf(activeYear)).child(task.getDateEnd()).child(task.getClassName()+task.getSerNum()).setValue(task);
                refTasks.child(String.valueOf(activeYear))
                        .child(String.valueOf(!task.isFullClass()))
                        .child(task.getDateEnd())
                        .child(task.getClassName()+task.getSerNum())
                        .removeValue();
                taskAdp.notifyDataSetChanged();
            }
        }
        return super.onContextItemSelected(item);
    }
}