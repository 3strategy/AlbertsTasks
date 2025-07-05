package com.example.tasks.Activities;

import static com.example.tasks.FBRef.refDoneTasks;
import static com.example.tasks.FBRef.refTasks;
import static com.example.tasks.FBRef.refUsers;
import static com.example.tasks.Utilities.db2Dsiplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.ProgressDialog;
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

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * The main activity of the application, displaying a list of active tasks for the current user.
 * <p>
 * This activity extends {@link MasterActivity} to inherit its common options menu.
 * It retrieves and displays tasks from Firebase Realtime Database for the currently
 * selected "active year". Users can interact with tasks through a context menu
 * to edit, delete, or mark them as done.
 * <p>
 * Features:
 * <ul>
 *     <li>Displays a personalized welcome message with the user's name.</li>
 *     <li>Fetches and lists active tasks for the current {@code activeYear} using a {@link TaskAdapter}.</li>
 *     <li>Provides an "Add Task" button to navigate to {@link TaskActivity} for creating new tasks.</li>
 *     <li>Implements a context menu on task items for actions: Edit, Delete, Done.</li>
 *     <li>Handles task completion by moving tasks from the active tasks list to a "done tasks" list in Firebase.</li>
 *     <li>Shows a {@link ProgressDialog} while initially fetching data.</li>
 * </ul>
 * The {@code activeYear} is retrieved from {@link SharedPreferences}.
 *
 * @see MasterActivity
 * @see TaskAdapter
 * @see Task
 * @see User
 * @see FBRef
 */
public class MainActivity extends MasterActivity implements AdapterView.OnItemClickListener,
        View.OnCreateContextMenuListener {
    private TextView tVMainHeader;
    private ListView lVMain;
    private ProgressDialog pd;
    private User user;
    /**
     * Static list holding the current tasks displayed in the ListView.
     * Making this static can have implications if multiple instances of MainActivity
     * could potentially exist or if data persistence across activity instances is desired
     * without relying on standard Android lifecycle methods like onSaveInstanceState.
     * Consider if a ViewModel or non-static field with proper state handling is more appropriate.
     */
    public static ArrayList<Task> tasksList;
    private TaskAdapter taskAdp;
    private int choose;
    private int activeYear;
    private ValueEventListener vel;
    SharedPreferences settings;

    /**
     * Called when the activity is first created.
     * Initializes views, SharedPreferences, and fetches initial user and task data.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
    }

    /**
     * Initializes UI components, sets up the ListView with its adapter,
     * fetches the current user's details, and loads tasks for the active year.
     * <p>
     * A {@link ProgressDialog} is shown while data is being fetched from Firebase.
     * The {@code activeYear} is read from SharedPreferences.
     * A {@link ValueEventListener} is attached to Firebase to listen for real-time
     * updates to the tasks list.
     */
    private void initViews() {
        tVMainHeader = findViewById(R.id.tVMainHeader);
        lVMain = findViewById(R.id.lVMain);

        tasksList = new ArrayList<Task>();
        taskAdp = new TaskAdapter(MainActivity.this, tasksList);
        lVMain.setAdapter(taskAdp);
        lVMain.setOnItemClickListener(this);
        registerForContextMenu(lVMain);
        pd=ProgressDialog.show(this,"Connecting Database","Gathering data...",true);


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
                if (pd != null) {
                    pd.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        refTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    /**
     * Called when the activity will start interacting with the user.
     * Re-attaches the Firebase ValueEventListener to ensure data is fresh if
     * the activity was paused and resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refTasks.child(String.valueOf(activeYear)).addValueEventListener(vel);
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     * Removes the Firebase ValueEventListener to prevent unnecessary background updates
     * and potential memory leaks.
     */
    @Override
    protected void onStop() {
        super.onStop();
        refTasks.child(String.valueOf(activeYear)).removeEventListener(vel);
    }

    /**
     * Handles the "Add Task" button click.
     * Navigates to {@link TaskActivity} to allow the user to create a new task.
     * Passes the current {@code activeYear} and a flag indicating it's a new task.
     *
     * @param view The view that was clicked (the "Add Task" button).
     */
    public void addTask(View view) {
        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("isNewTask",true);
        intent.putExtra("activeYear",activeYear);
        startActivity(intent);
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     * Displays a {@link Toast} message showing the start date of the clicked task.
     *
     * @param parent The AdapterView where the click happened.
     * @param view   The view within the AdapterView that was clicked (this
     *               will be a view provided by the adapter).
     * @param pos    The position of the view in the adapter.
     * @param id     The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        String date = db2Dsiplay(tasksList.get(pos).getDateStart());
        Toast.makeText(this, "Given in "+date, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the context menu for a view is about to be shown.
     * This method inflates the context menu with options: "Edit", "Delete", and "Done"
     * for the selected task item in the ListView.
     *
     * @param menu     The context menu that is being built.
     * @param v        The view for which the context menu is being built.
     * @param menuInfo Extra information about the item for which the context menu should be shown.
     *                 This object will vary depending on the class of v.
     */
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

    /**
     * This hook is called whenever an item in a context menu is selected.
     * Handles actions for "Edit", "Delete", and "Done".
     * <p>
     * "Edit": Navigates to {@link TaskActivity} with task details to allow editing.
     * "Delete": Removes the task from Firebase.
     * "Done": Moves the task to the "Done_Tasks" node in Firebase. A confirmation dialog
     * is shown if the task is not for the 'full class'.
     *
     * @param item The context menu item that was selected.
     * @return boolean Return false to allow normal context menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Log.i("MainActivity", "onContextItemSelected");
        String action = item.getTitle().toString();
        if (action.equals("Edit")) {
            Intent intent = new Intent(this, TaskActivity.class);
            intent.putExtra("isNewTask",false);
            intent.putExtra("activeYear",activeYear);
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