package com.example.tasks.Adapters;

import static com.example.tasks.Utilities.db2Dsiplay;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
/**
 * @author		Albert Levy albert.school2015@gmail.com
 * @version     2.1
 * @since		9/3/2024
 * <p>
 * Adapter for displaying a list of {@link Task} objects in a {@link android.widget.ListView}.
 * <p>
 * This adapter is responsible for creating and binding views for each task item.
 * It displays task details such as the due date, class name, and serial number.
 * The adapter also highlights tasks based on their due date and completion status:
 * <ul>
 *     <li>Tasks that are overdue and marked as 'full class' will have their text displayed in red and bold.</li>
 *     <li>Tasks that are significantly overdue (more than 5 days) and 'full class' will also have a yellow background.</li>
 * </ul>
 * It uses a {@link ViewHolder} pattern for efficient view recycling.
 *
 * @see BaseAdapter
 * @see Task
 */
public class TaskAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Task> tasks;
    private LayoutInflater inflater;
    private Calendar calNow;

    /**
     * Constructs a new {@code TaskAdapter}.
     *
     * @param context The current context.
     * @param tasks   An ArrayList of {@link Task} objects to be displayed.
     */
    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
        this.inflater = LayoutInflater.from(context);
        calNow = Calendar.getInstance();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
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
     * @return The {@link Task} at the specified position.
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
     * Get a View that displays the data at the specified position in the data set.
     * <p>
     * This method inflates the layout for each task item (if necessary) and populates
     * it with the data from the {@link Task} object at the given position.
     * It applies conditional formatting (text color, style, background color)
     * based on the task's due date and whether it's a 'full class' task.
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

    /**
     * Calculates the number of days between a given start date string and an end calendar date.
     * <p>
     * The start date string is expected in "yyyyMMdd" format.
     * A positive result means the {@code endDate} is after the {@code startDate}.
     * A negative result means the {@code endDate} is before the {@code startDate}.
     *
     * @param startDate     The start date in "yyyyMMdd" format.
     * @param endDate       The calendar instance representing the end date.
     * @return The number of days between the start date and end date.
     * @throws RuntimeException if the {@code startDateString} cannot be parsed.
     */
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

    /**
     * ViewHolder pattern class to efficiently store and reuse views for list items.
     * <p>
     * This class holds references to the UI components within each row of the ListView,
     * such as {@link TextView}s for displaying task details and a {@link ToggleButton}
     * for the 'full class' status.
     */
    private class ViewHolder {
        LinearLayout llTask;
        TextView itemDue, itemClass, itemTask;
        ToggleButton itemFull;
        ColorStateList oldColor;
        /**
         * Constructs a new ViewHolder.
         * Initializes all the views within the item layout.
         *
         * @param view The root view of the item layout.
         */
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
