package com.urrecliner.saynotitext;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.urrecliner.saynotitext.Vars.linePos;
import static com.urrecliner.saynotitext.Vars.alertLines;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    @Override
    public int getItemCount() { return alertLines.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tSelect;
        EditText tGroup, tWho, tKey1, tKey2, tTalk, tComment;

        ViewHolder(final View itemView) {
            super(itemView);
            tSelect = itemView.findViewById(R.id.one_Select);
            tGroup = itemView.findViewById(R.id.one_group);
            tWho = itemView.findViewById(R.id.one_who);
            tKey1 = itemView.findViewById(R.id.one_key1);
            tKey2 = itemView.findViewById(R.id.one_key2);
            tTalk = itemView.findViewById(R.id.one_talk);
            tComment = itemView.findViewById(R.id.one_comment);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final AlertLine alertLine = alertLines.get(position);
        holder.tSelect.setText(alertLine.isSelect() ? "▣":"▢");
        holder.tGroup.setText(alertLine.getGroup());
        holder.tWho.setText(alertLine.getWho());
        holder.tKey1.setText(alertLine.getKey1());
        holder.tKey2.setText(alertLine.getKey2());
        holder.tTalk.setText(alertLine.getTalk());
        holder.tComment.setText(alertLine.getComment());

        holder.tSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linePos = holder.getAdapterPosition();
                AlertLine alertLine1 = alertLines.get(linePos);
                boolean select = !alertLine1.isSelect();
                alertLine1.setSelect(select);
                alertLines.set(linePos, alertLine1);
                holder.tSelect.setText((select) ? "▣":"▢");
            }
        });

        holder.tGroup.addTextChangedListener(new TextWatcher() {
            String sv = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { sv = s.toString(); }
            @Override
            public void afterTextChanged(Editable s) {
                if (sv.equals(s.toString()))
                    return;
                linePos = holder.getAdapterPosition();
                AlertLine alertLine = alertLines.get(linePos); alertLine.setGroup(s.toString().trim());
                alertLines.set(linePos, alertLine);
            }
        });

        holder.tWho.addTextChangedListener(new TextWatcher() {
            String sv = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { sv = s.toString(); }
            @Override
            public void afterTextChanged(Editable s) {
//                if (sv.equals(s.toString()))
//                    return;
                linePos = holder.getAdapterPosition();
                AlertLine alertLine = alertLines.get(linePos); alertLine.setWho(s.toString().trim());
                alertLines.set(linePos, alertLine);
            }
        });

        holder.tKey1.addTextChangedListener(new TextWatcher() {
            String sv = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { sv = s.toString(); }
            @Override
            public void afterTextChanged(Editable s) {
                if (sv.equals(s.toString()))
                    return;
                linePos = holder.getAdapterPosition();
                AlertLine alertLine = alertLines.get(linePos); alertLine.setKey1(s.toString().trim());
                alertLines.set(linePos, alertLine);
            }
        });

        holder.tKey2.addTextChangedListener(new TextWatcher() {
            String sv = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { sv = s.toString(); }
            @Override
            public void afterTextChanged(Editable s) {
                if (sv.equals(s.toString()))
                    return;
                linePos = holder.getAdapterPosition();
                AlertLine alertLine = alertLines.get(linePos); alertLine.setKey2(s.toString().trim());
                alertLines.set(linePos, alertLine);
            }
        });

        holder.tTalk.addTextChangedListener(new TextWatcher() {
            String sv = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { sv = s.toString(); }
            @Override
            public void afterTextChanged(Editable s) {
                if (sv.equals(s.toString()))
                    return;
                linePos = holder.getAdapterPosition();
                AlertLine alertLine = alertLines.get(linePos); alertLine.setTalk(s.toString().trim());
                alertLines.set(linePos, alertLine);
            }
        });

        holder.tComment.addTextChangedListener(new TextWatcher() {
            String sv = null;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { sv = s.toString(); }
            @Override
            public void afterTextChanged(Editable s) {
                if (sv.equals(s.toString()))
                    return;
                linePos = holder.getAdapterPosition();
                AlertLine alertLine = alertLines.get(linePos); alertLine.setComment(s.toString().trim());
                alertLines.set(linePos, alertLine);
            }

        });
    }

}
