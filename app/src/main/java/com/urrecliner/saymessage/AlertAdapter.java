package com.urrecliner.saymessage;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.urrecliner.saymessage.Vars.linePos;
import static com.urrecliner.saymessage.Vars.alertLines;
import static com.urrecliner.saymessage.Vars.mActivity;
import static com.urrecliner.saymessage.Vars.mContext;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    @Override
    public int getItemCount() { return alertLines.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tGroup, tWho, tKey1, tKey2, tTalk, tMemo;
        View tLine;
        ViewHolder(final View itemView) {
            super(itemView);
            tLine = itemView.findViewById(R.id.one_line);
            tGroup = itemView.findViewById(R.id.one_group);
            tWho = itemView.findViewById(R.id.one_who);
            tKey1 = itemView.findViewById(R.id.one_key1);
            tKey2 = itemView.findViewById(R.id.one_key2);
            tTalk = itemView.findViewById(R.id.one_talk);
            tMemo = itemView.findViewById(R.id.one_memo);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alert_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final AlertLine alertLine = alertLines.get(position);
        holder.tGroup.setText(alertLine.getGroup());
        holder.tWho.setText(alertLine.getWho());
        holder.tKey1.setText(alertLine.getKey1());
        holder.tKey2.setText(alertLine.getKey2());
        holder.tTalk.setText(alertLine.getTalk());
        holder.tMemo.setText(alertLine.getMemo());

        int grayed = (position % 4) * 10;
        int backColor = ContextCompat.getColor(mActivity, R.color.colorLine)
                + grayed + grayed *256 + grayed *256*256;
        holder.tLine.setBackgroundColor(backColor);

        holder.tLine.setOnClickListener(v -> {
            linePos = holder.getAdapterPosition();
            Intent intent = new Intent(mContext, EditAlertActivity.class);
            mActivity.startActivity(intent);
        });
    }

}
