package com.pandasdroid.wordlequest;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.pandasdroid.wordlequest.databinding.CalendarItemBinding;
import com.pandasdroid.wordlequest.room.HistoryDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<Integer> data;
    private MonthAdapterListener listener;
    private int month = -1;
    private int year = -1;
    private int selectedPos = -1;

    public CalendarAdapter(List<Integer> data, int month, int year, MonthAdapterListener monthAdapterListener) {
        this.data = data;
        this.listener = monthAdapterListener;
        this.month = month;
        this.year = year;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CalendarItemBinding calendarItemBinding = CalendarItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(calendarItemBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final int position = pos;

        int day = data.get(position) == null ? -1 : data.get(position);

        if (day == -1) {
            holder.itemView.setVisibility(View.GONE);
        }

        holder.calendarItemBinding.dayTextView.setText(String.valueOf(day));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day_ = calendar.get(Calendar.DAY_OF_MONTH);
        if (day_ < day && calendar.get(Calendar.MONTH) == month) {
            holder.calendarItemBinding.dayTextView.setTextColor(Color.parseColor("#D3D3D3"));
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int prevPos = selectedPos;
                    selectedPos = position;
                    if (prevPos != -1) {
                        notifyItemChanged(prevPos);
                    }
                    listener.onSelectDate(year, month, data.get(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemChanged(position);
                        }
                    }, 50);
                }
            });

            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(holder.itemView.getContext());

            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(year, month, day);

            // Get the date in the format "yyyy-MM-dd"
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar1.getTime());
            if (sharedPrefHelper.getSharedPrefHelper().contains(formattedDate)) {
                if (position == selectedPos) {
                    holder.calendarItemBinding.dayTextView.setTextColor(Color.WHITE);
                    holder.calendarItemBinding.cvDate.setCardBackgroundColor(Color.parseColor("#FF00796B"));
                } else {
                    holder.calendarItemBinding.dayTextView.setTextColor(Color.parseColor("#FF00796B"));
                }
            } else {
                if (position == selectedPos) {
                    holder.calendarItemBinding.dayTextView.setTextColor(Color.WHITE);
                    holder.calendarItemBinding.cvDate.setCardBackgroundColor(Color.parseColor("#D32F2F"));
                } else {
                    holder.calendarItemBinding.dayTextView.setTextColor(Color.parseColor("#D32F2F"));
                }
            }
        }
        // Add any additional customization for each calendar item here
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CalendarItemBinding calendarItemBinding;

        public ViewHolder(CalendarItemBinding itemView) {
            super(itemView.getRoot());
            this.calendarItemBinding = itemView;
        }
    }

}
