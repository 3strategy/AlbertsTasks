package com.example.tasks.Adapters;

import static com.example.tasks.Utilities.db2Dsiplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tasks.Obj.Task;
import com.example.tasks.R;

import java.util.ArrayList;

public class DoneTaskAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Task> tasks;
    private LayoutInflater inflater;

    public DoneTaskAdapter(Context context, ArrayList<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        this.inflater = LayoutInflater.from(context);
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
        DoneTaskAdapter.ViewHolderDone viewHolderDone;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.donetask_layout, parent, false);
            viewHolderDone = new DoneTaskAdapter.ViewHolderDone(view);
            view.setTag(viewHolderDone);
        } else {
            viewHolderDone = (DoneTaskAdapter.ViewHolderDone) view.getTag();
        }
        Task task = tasks.get(pos);
        viewHolderDone.itemDue.setText(db2Dsiplay(task.getDateEnd()));
        viewHolderDone.itemClass.setText(task.getClassName());
        viewHolderDone.itemTask.setText(task.getSerNum());
        viewHolderDone.itemChecked.setText(db2Dsiplay(task.getDateChecked()));
        return view;
    }

    private class ViewHolderDone {
        TextView itemDue, itemClass, itemTask, itemChecked;

        public ViewHolderDone(View view) {
            itemDue = (TextView)view.findViewById(R.id.tVDoneDue);
            itemClass = (TextView) view.findViewById(R.id.tVDoneClass);
            itemTask = (TextView) view.findViewById(R.id.tVDoneTask);
            itemChecked = (TextView) view.findViewById(R.id.tVDoneChecked);
        }
    }
}
