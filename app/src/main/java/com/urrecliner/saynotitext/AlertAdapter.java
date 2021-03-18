package com.urrecliner.saynotitext;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.urrecliner.saynotitext.EditActivity.dupView;
import static com.urrecliner.saynotitext.EditActivity.removeView;
import static com.urrecliner.saynotitext.Vars.linePos;
import static com.urrecliner.saynotitext.Vars.oneLines;
import static com.urrecliner.saynotitext.Vars.utils;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    @Override
    public int getItemCount() { return oneLines.size(); }

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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_edit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final OneLine oneLine = oneLines.get(position);
        holder.tSelect.setText(oneLine.isSelect() ? "☑":"☐");
        holder.tGroup.setText(oneLine.getGroup());
        holder.tWho.setText(oneLine.getWho());
        holder.tKey1.setText(oneLine.getKey1());
        holder.tKey2.setText(oneLine.getKey2());
        holder.tTalk.setText(oneLine.getTalk());
        holder.tComment.setText(oneLine.getComment());

        holder.tSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine1 = oneLines.get(linePos);
                boolean select = !oneLine1.isSelect();
                oneLine1.setSelect(select);
                oneLines.set(linePos, oneLine1);
                holder.tSelect.setText((select) ? "☑":"☐");
            }
        });

        holder.tGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos); oneLine.setGroup(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });

        holder.tGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos); oneLine.setGroup(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });

        holder.tWho.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos); oneLine.setWho(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });

        holder.tKey1.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos); oneLine.setKey1(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });

        holder.tKey2.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos); oneLine.setKey2(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });

        holder.tTalk.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos); oneLine.setTalk(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });

        holder.tComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {
                linePos = holder.getAdapterPosition();
                OneLine oneLine = oneLines.get(linePos);
                oneLine.setComment(s.toString().trim());
                oneLines.set(linePos, oneLine);
            }
        });
    }
}
