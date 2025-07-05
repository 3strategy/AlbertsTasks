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

/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Adapter for displaying a list of completed {@link Task} objects in a {@link android.widget.ListView}.
 * <p>
 * This adapter is responsible for creating and binding views for each completed task item.
 * It displays details of a done task such as its original due date, class name,
 * serial number (task identifier), and the date it was marked as checked/completed.
 * It uses a {@link ViewHolderDone} pattern for efficient view recycling.
 *
 * @see BaseAdapter
 * @see Task
 */
public class DoneTaskAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Task> tasks;
    private LayoutInflater inflater;

    /**
     * Constructs a new {@code DoneTaskAdapter}.
     *
     * @param context The current context.
     * @param tasks   An ArrayList of completed {@link Task} objects to be displayed.
     */
    public DoneTaskAdapter(Context context, ArrayList<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items (completed tasks).
     */
    @Override
    public int getCount() {
        return tasks.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param pos Position of the item whose data we want within the adapter's
     *            data set.
     * @return The completed {@link Task} at the specified position.
     */
    @Override
    public Object getItem(int pos) {
        return tasks.get(pos);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param pos The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int pos) {
        return pos;
    }

    /**
     * Get a View that displays the data for a completed task at the specified position in the data set.
     * <p>
     * This method inflates the layout for each completed task item (if necessary) and populates
     * it with the data from the {@link Task} object at the given position, including
     * due date, class name, task serial number, and checked date.
     *
     * @param pos     The position of the item within the adapter's data set of the item whose view
     *                we want.
     * @param view    The old view to reuse, if possible. Note: You should check that this view
     *                is non-null and of an appropriate type before using. If it is not possible to convert
     *                this view to display the correct data, this method can create a new view.
     * @param parent  The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
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

    /**
     * ViewHolder pattern class to efficiently store and reuse views for completed task list items.
     * <p>
     * This class holds references to the {@link TextView}s within each row of the ListView
     * that display the details of a completed task.
     */
    private class ViewHolderDone {
        TextView itemDue, itemClass, itemTask, itemChecked;

        /**
         * Constructs a new ViewHolderDone.
         * Initializes all the {@link TextView}s within the item layout by finding them by their ID.
         *
         * @param view The root view of the item layout (e.g., a row in the ListView).
         */
        public ViewHolderDone(View view) {
            itemDue = (TextView)view.findViewById(R.id.tVDoneDue);
            itemClass = (TextView) view.findViewById(R.id.tVDoneClass);
            itemTask = (TextView) view.findViewById(R.id.tVDoneTask);
            itemChecked = (TextView) view.findViewById(R.id.tVDoneChecked);
        }
    }
}
