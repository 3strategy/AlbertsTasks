package com.example.tasks.Obj;

import static com.example.tasks.FBRef.refYears;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.tasks.FBRef.*;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tasks.Activities.DoneTasksActivity;
import com.example.tasks.Activities.LoginActivity;
import com.example.tasks.Activities.MainActivity;
import com.example.tasks.Activities.YearsActivity;
import com.example.tasks.R;

import java.util.Calendar;
import java.util.List;

public class MasterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_master);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ActivityManager am = (ActivityManager) this .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        String Actvity_Name = taskInfo.get(0).topActivity.getClassName();
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
            Context context = this.getApplicationContext();
            AlertDialog.Builder adb =new AlertDialog.Builder(this);
            adb.setTitle("Disconnect Account");
            adb.setMessage("Are you sure yo want to\n Disconnect account & Exit?");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    refAuth.signOut();
                    finishAffinity();
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
            Context context = this.getApplicationContext();
            AlertDialog.Builder adb =new AlertDialog.Builder(this);
            adb.setTitle("Quit Application");
            adb.setMessage("Are you sure?");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
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