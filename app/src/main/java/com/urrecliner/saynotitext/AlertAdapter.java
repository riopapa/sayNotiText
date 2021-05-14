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
import static com.urrecliner.saynotitext.Vars.alertOneLines;
import static com.urrecliner.saynotitext.Vars.utils;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    @Override
    public int getItemCount() { return alertOneLines.size(); }

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
        final AlertOneLine alertOneLine = alertOneLines.get(position);
        holder.tSelect.setText(alertOneLine.isSelect() ? "▣":"▢");
        holder.tGroup.setText(alertOneLine.getGroup());
        holder.tWho.setText(alertOneLine.getWho());
        holder.tKey1.setText(alertOneLine.getKey1());
        holder.tKey2.setText(alertOneLine.getKey2());
        holder.tTalk.setText(alertOneLine.getTalk());
        holder.tComment.setText(alertOneLine.getComment());

        holder.tSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linePos = holder.getAdapterPosition();
                AlertOneLine alertOneLine1 = alertOneLines.get(linePos);
                boolean select = !alertOneLine1.isSelect();
                alertOneLine1.setSelect(select);
                alertOneLines.set(linePos, alertOneLine1);
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
                AlertOneLine alertOneLine = alertOneLines.get(linePos); alertOneLine.setGroup(s.toString().trim());
                alertOneLines.set(linePos, alertOneLine);
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
                AlertOneLine alertOneLine = alertOneLines.get(linePos); alertOneLine.setWho(s.toString().trim());
                alertOneLines.set(linePos, alertOneLine);
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
                AlertOneLine alertOneLine = alertOneLines.get(linePos); alertOneLine.setKey1(s.toString().trim());
                alertOneLines.set(linePos, alertOneLine);
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
                AlertOneLine alertOneLine = alertOneLines.get(linePos); alertOneLine.setKey2(s.toString().trim());
                alertOneLines.set(linePos, alertOneLine);
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
                AlertOneLine alertOneLine = alertOneLines.get(linePos); alertOneLine.setTalk(s.toString().trim());
                alertOneLines.set(linePos, alertOneLine);
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
                AlertOneLine alertOneLine = alertOneLines.get(linePos); alertOneLine.setComment(s.toString().trim());
                alertOneLines.set(linePos, alertOneLine);
            }

        });
    }

}
