package com.example.tasks.Obj;

import android.app.ActivityManager;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tasks.Activities.MainActivity;
import com.example.tasks.Activities.DoneTasksActivity;
import com.example.tasks.Activities.TaskActivity;
import com.example.tasks.Activities.YearsActivity;
import com.example.tasks.Activities.PresenceActivity;
import com.example.tasks.Activities.ReportsActivity;
import com.example.tasks.Activities.ProfileActivity;
import com.example.tasks.Activities.MaakavActivity;
import com.example.tasks.R;

import java.util.List;

/**
 * Base activity providing common menu handling and dynamic title update.
 * All feature Activities should extend this class instead of AppCompatActivity.
 */
public abstract class MasterActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String activityName = taskInfo.get(0).topActivity.getClassName();
        int itemId = item.getItemId();
        if (itemId == R.id.idMain) {
            if (!activityName.equals(MainActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to MainActivity");
                startActivity(new Intent(this, MainActivity.class));
            }
        } else if (itemId == R.id.idTasksDone) {
            if (!activityName.equals(DoneTasksActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to DoneTasksActivity");
                startActivity(new Intent(this, DoneTasksActivity.class));
            }
        } else if (itemId == R.id.idYears) {
            if (!activityName.equals(YearsActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to YearsActivity");
                startActivity(new Intent(this, YearsActivity.class));
            }
        } else if (itemId == R.id.idReports) {
            if (!activityName.equals(ReportsActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to ReportsActivity");
                startActivity(new Intent(this, ReportsActivity.class));
            }
        } else if (itemId == R.id.idProfile) {
            if (!activityName.equals(ProfileActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to ProfileActivity");
                startActivity(new Intent(this, ProfileActivity.class));
            }
        } else if (itemId == R.id.idPresence) {
            if (!activityName.equals(PresenceActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to PresenceActivity");
                startActivity(new Intent(this, PresenceActivity.class));
            }
        } else if (itemId == R.id.idMaakav) {
            if (!activityName.equals(MaakavActivity.class.getName())) {
                Log.i("MasterActivity", "Changing to MaakavActivity");
                startActivity(new Intent(this, MaakavActivity.class));
            }
        } else if (itemId == R.id.idDisconnect) {
            showDisconnectDialog();
            return true;
        } else if (itemId == R.id.idExit) {
            showExitDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTitle();
    }

    /**
     * Sets the ActionBar title based on the concrete subclass.
     */
    private void updateTitle() {
        String title = "Presence";
        Class<?> cls = getClass();
        if (cls.equals(PresenceActivity.class)) {
            title += "/נוכחות";
        } else if (cls.equals(ReportsActivity.class)) {
            title += "/דיווחים";
        } else if (cls.equals(ProfileActivity.class)) {
            title += "/פרופיל";
        } else if (cls.equals(MaakavActivity.class)) {
            title += "/מעקב";
        } else if (cls.equals(MainActivity.class)) {
            title += "/משימות";
        } else if (cls.equals(TaskActivity.class)) {
            title += "/משימה";
        } else if (cls.equals(YearsActivity.class)) {
            title += "/שנה";
        } else if (cls.equals(DoneTasksActivity.class)) {
            title += "/משימות שהושלמו";
        }
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(title);
        } else {
            setTitle(title);
        }
    }

    private void showDisconnectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Disconnect Account")
                .setMessage("Are you sure you want to disconnect account & exit?")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // perform sign out logic
                    finishAffinity();
                })
                .setNeutralButton("Cancel", (dialog, which) -> dialog.cancel())
                .setCancelable(false)
                .show();
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Application")
                .setMessage("Are you sure?")
                .setPositiveButton("Ok", (dialog, which) -> finishAffinity())
                .setNeutralButton("Cancel", (dialog, which) -> dialog.cancel())
                .setCancelable(false)
                .show();
    }
}
