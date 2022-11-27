package com.iacademy.tulongsulong.adapters;
import com.iacademy.tulongsulong.models.ContactsModel;
import com.iacademy.tulongsulong.utils.RecyclerOnItemClickListener;
import com.iacademy.tulongsulong.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    //declare variables
    private ArrayList<ContactsModel> listModels;
    private Context context;
    private RecyclerOnItemClickListener mItemClickListener;


    //constructor
    public ContactsAdapter(ArrayList<ContactsModel> listModels, Context context, RecyclerOnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
        this.listModels = listModels;
        this.context = context;
    }

    public ContactsAdapter(ArrayList<ContactsModel> listModels, Context context) {
        this.listModels = listModels;
        this.context = context;
    }

    //create view each time
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contactlist, parent, false));
    }

    //add information
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(listModels.get(position).getName());
        holder.tvNumber.setText(listModels.get(position).getNumber());
        holder.tvEmail.setText(listModels.get(position).getEmail());
    }

    //returns how many items on the list
    @Override
    public int getItemCount() {
        return listModels.size();
    }

    //declare and instantiate variables
    public class ViewHolder extends RecyclerView.ViewHolder{

        //declare variables
        public TextView tvName;
        public TextView tvNumber;
        public TextView tvEmail;

        //instantiate variable
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_name);
            tvNumber = itemView.findViewById(R.id.tv_num);
            tvEmail = itemView.findViewById(R.id.tv_email);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick (View v){
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });
        }
    }
}
