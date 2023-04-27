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
import model.lostlist;


public class lostadapter extends RecyclerView.Adapter<lostadapter.ViewHolder>{
    private ArrayList<lostlist> myList;
    int count = 0;

    // RecyclerView recyclerView;
    public lostadapter(ArrayList<lostlist> listdata) {
        this.myList = myList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.lost_row_putway_data, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        count ++;
        holder.snumberrack.setText(myList.get(position).getRacknumber());
        holder.srTvlf.setText(myList.get(position).getSerialnumber());
        holder.countlf.setText(String.valueOf(count));




    }


    @Override
    public int getItemCount() {
        if (myList != null) {
            return myList.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView snumberrack;
        public TextView srTvlf;
        public TextView countlf;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);

           this.snumberrack = (TextView) itemView.findViewById(R.id.rackoTvlf);
            this.srTvlf = (TextView) itemView.findViewById(R.id.numbersTvlf);

            this.countlf = (TextView) itemView.findViewById(R.id.srNolf);

        }
    }
}