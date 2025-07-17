package com.example.tasks.Obj;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tasks.Activities.PresenceActivity;
import com.example.tasks.Activities.ReportsActivity;
import com.example.tasks.Activities.ProfileActivity;
import com.example.tasks.Activities.MaakavActivity;
import com.example.tasks.R;

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
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.idPresence) {
            intent = new Intent(this, PresenceActivity.class);
        } else if (id == R.id.idReports) {
            intent = new Intent(this, ReportsActivity.class);
        } else if (id == R.id.idProfile) {
            intent = new Intent(this, ProfileActivity.class);
        } else if (id == R.id.idMaakav) {
            intent = new Intent(this, MaakavActivity.class);
        } else if (id == R.id.idDisconnect) {
            showDisconnectDialog();
            return true;
        } else if (id == R.id.idExit) {
            showExitDialog();
            return true;
        }
        if (intent != null) {
            startActivity(intent);
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
