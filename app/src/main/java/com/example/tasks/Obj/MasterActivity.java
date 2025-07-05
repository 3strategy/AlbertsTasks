package com.example.tasks.Obj;

import static com.example.tasks.FBRef.refYears; // It's generally better to import specific members if not all are used.

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.tasks.FBRef.*; // Avoid wildcard imports if specific members are known.
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tasks.Activities.DoneTasksActivity;
import com.example.tasks.Activities.MainActivity;
import com.example.tasks.Activities.YearsActivity;
import com.example.tasks.R;

import java.util.List;

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * MasterActivity serves as a base class for activities within the application,
 * providing common functionality such as an options menu for navigation,
 * account disconnection, and application exit.
 *
 * <p>Activities extending MasterActivity will inherit its options menu,
 * allowing users to navigate to different sections of the app (e.g., Main,
 * Done Tasks, Years), disconnect their current account, or exit the
 * application entirely.
 *
 * <p>The class handles menu item selections to perform the corresponding actions,
 * such as starting new activities or displaying confirmation dialogs for
 * sensitive operations like account disconnection or exiting the app.
 * It also prevents redundant navigation by checking if the current activity
 * is already the one the user is trying to navigate to.
 */
public class MasterActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * This method is a standard part of the Android Activity lifecycle.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called when the activity is becoming visible to the user.
     * This method is a standard part of the Android Activity lifecycle.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate). You can use this method for any item
     * selection handling that you wish to implement.
     *
     * <p>This implementation handles navigation to different activities based
     * on the selected menu item. It also provides options to disconnect
     * the current user or exit the application, with confirmation dialogs.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ActivityManager am = (ActivityManager) this .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String Actvity_Name = taskInfo.get(0).topActivity.getClassName(); // Be cautious with getRunningTasks, it's deprecated for third-party apps.
        int itemId = item.getItemId();
        if (itemId == R.id.idMain) {
            if (!Actvity_Name.equals("com.example.tasks.Activities.MainActivity")) {
                Log.i("MasterActivity","Changing to MainActivity");
                Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.idTasksDone) {
            if (!Actvity_Name.equals("com.example.tasks.Activities.DoneTasksActivity")) {
                Log.i("MasterActivity","Changing to DoneTasksActivity");
                Intent intent = new Intent(this.getApplicationContext(), DoneTasksActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.idYears) {
            if (!Actvity_Name.equals("com.example.tasks.Activities.YearsActivity")) {
                Log.i("MasterActivity","Changing to YearsActivity");
                Intent intent = new Intent(this.getApplicationContext(), YearsActivity.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.idDisconnect) {
            AlertDialog.Builder adb =new AlertDialog.Builder(this);
            adb.setTitle("Disconnect Account");
            adb.setMessage("Are you sure yo want to\n Disconnect account & Exit?"); // Typo: "you"
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    refAuth.signOut(); // Ensure refAuth is initialized and accessible
                    finishAffinity(); // Closes all activities in this task
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
        } else if (itemId == R.id.idExit) {
            AlertDialog.Builder adb =new AlertDialog.Builder(this);
            adb.setTitle("Quit Application");
            adb.setMessage("Are you sure?");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity(); // Closes all activities in this task
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
        return super.onOptionsItemSelected(item);
    }
}
