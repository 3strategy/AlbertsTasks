package com.example.tasks.Adapters;

import static com.example.tasks.Utilities.db2Dsiplay;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.tasks.Obj.Task;
import com.example.tasks.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.Date;
public class TaskAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Task> tasks;
    private LayoutInflater inflater;
    private Calendar calNow;

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        this.inflater = LayoutInflater.from(context);
        calNow = Calendar.getInstance();
    }

    @Override
    public int getCount() {
        return tasks.size();

    }

    @Override
    public Object getItem(int pos) {
        return tasks.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.task_layout, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        Task task = tasks.get(pos);
        viewHolder.itemDue.setText(db2Dsiplay(task.getDateEnd()));
        viewHolder.itemClass.setText(task.getClassName());
        viewHolder.itemTask.setText(task.getSerNum());
        long diff = daysBetween(task.getDateEnd(),calNow);
        if (diff > 0 && task.isFullClass()){
            viewHolder.itemDue.setTextColor(Color.RED);
            viewHolder.itemDue.setTypeface(null, Typeface.BOLD);
            viewHolder.itemClass.setTextColor(Color.RED);
            viewHolder.itemClass.setTypeface(null, Typeface.BOLD);
            viewHolder.itemTask.setTextColor(Color.RED);
            viewHolder.itemTask.setTypeface(null, Typeface.BOLD);
            if (diff > 5) {
                viewHolder.llTask.setBackgroundColor(Color.YELLOW);
            } else {
                viewHolder.llTask.setBackgroundColor(Color.WHITE);
            }
        } else {
            viewHolder.itemDue.setTextColor(viewHolder.oldColor);
            viewHolder.itemDue.setTypeface(null, Typeface.NORMAL);
            viewHolder.itemClass.setTextColor(viewHolder.oldColor);
            viewHolder.itemClass.setTypeface(null, Typeface.NORMAL);
            viewHolder.itemTask.setTextColor(viewHolder.oldColor);
            viewHolder.itemTask.setTypeface(null, Typeface.NORMAL);
            viewHolder.llTask.setBackgroundColor(Color.WHITE);
        }
        viewHolder.itemFull.setChecked(task.isFullClass());
        return view;
    }

    public long daysBetween(String startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = sdf.parse(startDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        long start = calStart.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(end - start);
    }

    private class ViewHolder {
        LinearLayout llTask;
        TextView itemDue, itemClass, itemTask;
        ToggleButton itemFull;
        ColorStateList oldColor;
        public ViewHolder(View view) {
            llTask = view.findViewById(R.id.llTask);
            itemDue = view.findViewById(R.id.tVDue);
            itemClass = view.findViewById(R.id.tVClass);
            itemTask = view.findViewById(R.id.tVTask);
            itemFull = view.findViewById(R.id.tBFull);
            oldColor = itemDue.getTextColors();
        }
    }

}
