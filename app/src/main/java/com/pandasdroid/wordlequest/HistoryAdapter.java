package com.pandasdroid.wordlequest;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pandasdroid.wordlequest.room.HistoryEntity;
import com.pandasdroid.wordlequest.databinding.ItemHistoryBinding;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryEntity> historyList;

    public HistoryAdapter(List<HistoryEntity> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryEntity history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ItemHistoryBinding binding;

        public ViewHolder(@NonNull ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(HistoryEntity history) {
            binding.textViewId.setText(String.valueOf(history.id));
            binding.textViewGameTime.setText(String.valueOf(history.game_time));
            binding.textViewWordLength.setText(String.valueOf(history.word_len));
            binding.textViewTimeConsumed.setText(String.valueOf(history.time_consumed));
            binding.textViewAttempts.setText(String.valueOf(history.attempts));
            binding.textViewWord.setText(history.word);
            binding.textViewHintCount.setText(String.valueOf(history.hint_count));
        }
    }
}
