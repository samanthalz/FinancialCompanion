package com.example.financialcompanion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ProfileAccountAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> accountDrawables;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int drawableRes);
    }

    public ProfileAccountAdapter(Context context, List<Integer> accountDrawables, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.accountDrawables = accountDrawables;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
        return accountDrawables.size();
    }

    @Override
    public Object getItem(int position) {
        return accountDrawables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_profile_account, parent, false);
        }

        ImageView accountIcon = convertView.findViewById(R.id.account_icon);
        accountIcon.setImageResource(accountDrawables.get(position));

        // Set click listener
        convertView.setOnClickListener(v -> onItemClickListener.onItemClick(accountDrawables.get(position)));

        return convertView;
    }
}




