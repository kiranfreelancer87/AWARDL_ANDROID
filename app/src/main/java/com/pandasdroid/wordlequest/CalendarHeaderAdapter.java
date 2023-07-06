package com.pandasdroid.wordlequest;

import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.pandasdroid.wordlequest.databinding.CalendarItemBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarHeaderAdapter extends RecyclerView.Adapter<CalendarHeaderAdapter.ViewHolder> {


    String[] weekDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public CalendarHeaderAdapter() {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CalendarItemBinding calendarItemBinding = CalendarItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(calendarItemBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        holder.calendarItemBinding.dayTextView.setText(weekDays[pos]);
        holder.calendarItemBinding.cvDate.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CalendarItemBinding calendarItemBinding;

        public ViewHolder(CalendarItemBinding itemView) {
            super(itemView.getRoot());
            this.calendarItemBinding = itemView;
        }
    }

}
