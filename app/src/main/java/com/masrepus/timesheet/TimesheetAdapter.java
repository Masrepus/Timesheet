package com.masrepus.timesheet;

import android.content.Context;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.masrepus.timesheet.database.Timerecord;

import org.joda.time.format.DateTimeFormat;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by samuel on 09.08.17.
 */

public class TimesheetAdapter extends RecyclerView.Adapter<TimesheetAdapter.ViewHolder> {

    private List<Timerecord> timerecords;
    private Context context;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timerecord, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Timerecord current = timerecords.get(position);

        String start = DateTimeFormat.shortTime().print(current.getStart());
        String end = DateTimeFormat.shortTime().print(current.getEnd());
        holder.time.setText(context.getString(R.string.timerecord_start_end, start, end));

        double totalSalary = current.getSalaryPerHour() * (current.getEnd() - current.getStart()) / 3600000;
        holder.salary.setText(context.getString(R.string.timerecord_salary, totalSalary, current.getSalaryPerHour()));

        holder.comment.setText(current.getComment());
    }

    @Override
    public int getItemCount() {
        return timerecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView time, salary, comment;

        public ViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.tv_start_end);
            salary = (TextView) itemView.findViewById(R.id.tv_salary);
            comment = (TextView) itemView.findViewById(R.id.tv_comment);
        }
    }

    public TimesheetAdapter(Context context, List<Timerecord> timerecords) {
        this.context = context;
        this.timerecords = timerecords;
    }


}
