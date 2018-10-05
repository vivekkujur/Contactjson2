package com.example.uchiha.contactjson2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.myViewHolder>{

    private ArrayList<String> data=new ArrayList<>();
    Context context;
   public RecyclerAdapter(){

}
    public RecyclerAdapter(ArrayList<String> data, Context context) {
        this.data = data;
        this.context=context;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item_layout,parent,false);

        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        holder.detail.setText(data.get(position).toString());

    }

    @Override
    public int getItemCount() {
        return  data.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView detail;

        public myViewHolder(View itemView) {
            super(itemView);
            mView=itemView;

            detail=mView.findViewById(R.id.contact_detail);
        }


    }

    public void adddetails(ArrayList<String> detail) {

        data.addAll(detail);
        notifyDataSetChanged();
    }

}



