package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.test.R;


import java.util.ArrayList;

import model.MyListData;


public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
    private ArrayList<MyListData> listdata;
    int count = 0;

    // RecyclerView recyclerView;
    public MyListAdapter(ArrayList<MyListData> listdata) {
        this.listdata = listdata;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.singlr_row_putway_data, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        count ++;
        holder.number.setText(listdata.get(position).getDescription());
        holder.qty.setText(listdata.get(position).getQty());
        holder.rack.setText(listdata.get(position).getRack());
        holder.srNoTvPutway.setText(String.valueOf(count));



    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView number,qty,rack;
        public TextView srNoTvPutway;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);

            this.number = (TextView) itemView.findViewById(R.id.numbersTvputway);
            this.qty = (TextView) itemView.findViewById(R.id.qtyTvPutway);
            this.rack = (TextView) itemView.findViewById(R.id.rackoTvPutway);

            this.srNoTvPutway = (TextView) itemView.findViewById(R.id.srNoTvPutway);

        }
    }
}