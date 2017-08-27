package com.masrepus.timesheet;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.masrepus.timesheet.database.Timerecord;

import org.joda.time.format.DateTimeFormat;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by samuel on 09.08.17.
 */

public class TimesheetAdapter extends RecyclerView.Adapter<TimesheetAdapter.ViewHolder> {

    private static final int TIMERECORD = 0;
    private static final int SEPARATOR = 1;
    private List<Item> items;
    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TIMERECORD) {
            ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timerecord, parent, false);
            return new ViewHolder(layout, TIMERECORD);
        } else {
            ConstraintLayout layout  = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_separator, parent, false);
            return new ViewHolder(layout, SEPARATOR);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof TimerecordItem ? TIMERECORD : SEPARATOR;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);

        if (item instanceof TimerecordItem) {
            Timerecord current = ((TimerecordItem) item).getTimerecord();
            String start = DateTimeFormat.shortTime().print(current.getStart());
            String end = DateTimeFormat.shortTime().print(current.getEnd());
            holder.time.setText(context.getString(R.string.timerecord_start_end, start, end));

            double totalSalary = current.getSalaryPerHour() * (current.getEnd() - current.getStart()) / 3600000;
            holder.salary.setText(context.getString(R.string.timerecord_salary, totalSalary, current.getSalaryPerHour()));

            holder.comment.setText(current.getComment());
        } else {
            holder.date.setText(((SeparatorItem) item).getDate());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView time, salary, comment, date;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == TIMERECORD) {
                time = (TextView) itemView.findViewById(R.id.tv_start_end);
                salary = (TextView) itemView.findViewById(R.id.tv_salary);
                comment = (TextView) itemView.findViewById(R.id.tv_comment);
            } else {
                date = (TextView) itemView.findViewById(R.id.tv_date);
            }
        }
    }

    public TimesheetAdapter(Context context, List<Timerecord> timerecords) {
        this.context = context;
        Collections.sort(timerecords, (t1, t2) -> Long.compare(t1.getStart(), t2.getStart()));
        addDateSeparators(timerecords);
    }

    private void addDateSeparators(List<Timerecord> timerecords) {
        items = new LinkedList<>();
        String currDate = "";

        for (Timerecord timerecord : timerecords) {
            String date = DateTimeFormat.longDate().print(timerecord.getStart());
            if (!currDate.equals(date)) {
                items.add(new SeparatorItem(date));
                currDate = date;
            }
            items.add(new TimerecordItem(timerecord));
        }
    }

    private class Item {
    }

    private class TimerecordItem extends Item {
        private Timerecord timerecord;

        public TimerecordItem(Timerecord timerecord) {
            this.timerecord = timerecord;
        }

        public Timerecord getTimerecord() {
            return timerecord;
        }
    }

    private class SeparatorItem extends Item {
        private String date;

        public SeparatorItem(String date) {
            this.date = date;
        }

        public String getDate() {
            return date;
        }
    }
}
