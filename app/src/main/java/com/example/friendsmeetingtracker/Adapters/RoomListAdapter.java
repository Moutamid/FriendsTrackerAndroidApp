package com.example.friendsmeetingtracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendsmeetingtracker.Constants.ItemClickListener;
import com.example.friendsmeetingtracker.MapsActivity;
import com.example.friendsmeetingtracker.Model.Room;
import com.example.friendsmeetingtracker.R;

import java.util.ArrayList;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Room> userArrayList;
    private ItemClickListener itemClickListener;

    public RoomListAdapter(Context context, ArrayList<Room> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.room_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room model = userArrayList.get(position);
        holder.nameTxt.setText(model.getName());
        Glide.with(context)
                .load(R.drawable.profile)
                .into(holder.profileImg);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("loc","location");
                intent.putExtra("list",model.getUsers());
                intent.putExtra("roomId",model.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView profileImg;
        private TextView nameTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImg = itemView.findViewById(R.id.imgProfile);
            nameTxt = itemView.findViewById(R.id.userNames);

        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

}
