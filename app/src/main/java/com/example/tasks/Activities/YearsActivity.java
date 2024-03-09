package com.example.tasks.Activities;

import static com.example.tasks.FBRef.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tasks.Obj.MasterActivity;
import com.example.tasks.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class YearsActivity extends MasterActivity implements AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {
    private Spinner spYears;
    private ListView lVClasses;
    private Intent gi;
    private boolean isNewUser;
    private int activeYear, newYear;
    private String newClass;
    private ArrayList<Integer> years;
    private ArrayList<String> classes;
    private ArrayAdapter<Integer> yearsAdp;
    private ArrayAdapter<String> classesAdp;
    private ValueEventListener velYears = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dS) {
            years.clear();
            for(DataSnapshot data : dS.getChildren()) {
                years.add(Integer.parseInt(data.getKey()));
            }
            yearsAdp.notifyDataSetChanged();
            if (activeYear != 1970) {
                spYears.setSelection(years.indexOf(activeYear));
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    };
    private ValueEventListener velClass = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dS) {
            classes.clear();
            for(DataSnapshot data : dS.getChildren()) {
                classes.add(data.getValue(String.class));
            }
            classesAdp.notifyDataSetChanged();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {}
    };

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_years);

        settings = getSharedPreferences("PREFS_NAME",MODE_PRIVATE);
        initViews();
    }

    private void initViews() {
        spYears = findViewById(R.id.spYears);
        lVClasses = findViewById(R.id.lVClasses);
        years = new ArrayList<>();
        years.clear();
        classes = new ArrayList<>();
        classes.clear();
        yearsAdp = new ArrayAdapter<>(YearsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, years);
        spYears.setAdapter(yearsAdp);
        spYears.setOnItemSelectedListener(this);
        classesAdp = new ArrayAdapter<>(YearsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, classes);
        lVClasses.setAdapter(classesAdp);
        lVClasses.setOnItemClickListener(this);
        gi = getIntent();
        isNewUser = gi.getBooleanExtra("isNewUser", false);
        if (isNewUser) {
            activeYear = 1970;
            addNewYear(null);
        } else {
            activeYear = settings.getInt("activeYear",1970);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isNewUser && activeYear != 1970) {
            refYears.addValueEventListener(velYears);
            refYears.child(String.valueOf(activeYear)).addValueEventListener(velClass);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        refYears.removeEventListener(velYears);
    }

    public void addNewYear(View view) {
        AlertDialog.Builder adb =new AlertDialog.Builder(YearsActivity.this);
        adb.setTitle("Add new year");
        adb.setMessage("Enter the new year number:\n(when the summer vacation occur)");
        final EditText eT = new EditText(this.getApplicationContext());
        eT.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);
        Calendar calNow = Calendar.getInstance();
        eT.setHint(String.valueOf(1+calNow.get(Calendar.YEAR)));
        adb.setView(eT);
        adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newYear = Integer.parseInt(eT.getText().toString());
                if (years.contains(newYear)){
                    Toast.makeText(YearsActivity.this, "Year already exist!\nTry again", Toast.LENGTH_SHORT).show();
                } else {
                    refYears.child(String.valueOf(newYear)).setValue("Null");
                    dialog.dismiss();
                    if (isNewUser){
                        activeYear = newYear;
                        addNewClass();
                    }
                }
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

    public void addNewClass() {
        if (activeYear != 1970){
            AlertDialog.Builder adb =new AlertDialog.Builder(YearsActivity.this);
            adb.setTitle("Add new class");
            adb.setMessage("Enter the new class you teach:");
            final EditText eT = new EditText(this.getApplicationContext());
            eT.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            adb.setView(eT);
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newClass = eT.getText().toString();
                    if (classes.contains(newClass)){
                        Toast.makeText(YearsActivity.this, "Class already exist!\nTry again", Toast.LENGTH_SHORT).show();
                    } else {
                        classes.add(newClass);
                        refYears.child(String.valueOf(activeYear)).setValue(classes);
                        dialog.dismiss();
                    }
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
    }

    public void done(View view) {
        if (activeYear != 1970) {
            if (isNewUser) {
                gi.putExtra("activeYear", activeYear);
                setResult(RESULT_OK,gi);
                finish();
            } else {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("activeYear",activeYear);
                editor.commit();
                Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(YearsActivity.this);
            adb.setTitle("Warning !!!");
            adb.setMessage("You must set the Active Year");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            adb.setCancelable(false);
            adb.create().show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (!isNewUser){
            activeYear = years.get(pos);
            refYears.child(String.valueOf(activeYear)).addValueEventListener(velClass);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getId() == R.id.lVClasses) {
            String choosen = classes.get(pos);
            AlertDialog.Builder adb =new AlertDialog.Builder(YearsActivity.this);
            adb.setTitle("Delete class");
            adb.setMessage("Are you sure you want to\ndelete task '"+choosen+"' ?");
            adb.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    classes.remove(pos);
                    refYears.child(String.valueOf(activeYear)).setValue(classes);
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
        }
    }

}