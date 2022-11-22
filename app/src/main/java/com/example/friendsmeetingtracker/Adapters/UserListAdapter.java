package com.example.friendsmeetingtracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendsmeetingtracker.Constants.ItemClickListener;
import com.example.friendsmeetingtracker.Model.User;
import com.example.friendsmeetingtracker.R;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder>{

    private Context context;
    private ArrayList<User> userArrayList;
    private boolean isCreated;
    private ItemClickListener itemClickListener;

    public UserListAdapter(Context context, ArrayList<User> userArrayList,boolean isCreated) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.isCreated = isCreated;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User model = userArrayList.get(position);
        holder.nameTxt.setText(model.getFullname());
        Glide.with(context)
                .load(model.getImageUrl())
                .into(holder.profileImg);
        if (isCreated){
            holder.checkBox.setVisibility(View.VISIBLE);
        }else {
            holder.checkBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView profileImg;
        private TextView nameTxt;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.imgProfile);
            nameTxt = itemView.findViewById(R.id.userNames);
            checkBox = itemView.findViewById(R.id.user_check);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (itemClickListener != null){
                        itemClickListener.onItemClick(getAdapterPosition(),b);
                    }
                }
            });
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

}
