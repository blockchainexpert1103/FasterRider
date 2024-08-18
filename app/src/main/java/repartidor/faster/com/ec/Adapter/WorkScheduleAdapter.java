package repartidor.faster.com.ec.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.model.WorkScheduleModel;
import repartidor.faster.com.ec.utils.GlobalVariable;

/**
 * Created by Eric on 02-Nov-17.
 */

public class WorkScheduleAdapter extends RecyclerView.Adapter<WorkScheduleAdapter.ViewHolder>  {

    private final Context context;

    private final List<WorkScheduleModel> dataAdapters;

    public WorkScheduleAdapter(List<WorkScheduleModel> getDataAdapter, Context context){

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workschedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder Viewholder, int position) {

        final WorkScheduleModel dataAdapterOBJ = dataAdapters.get(position);
        Viewholder.week_workschedule.setText(dataAdapterOBJ.get_week_workschedule());
        Viewholder.date_workschedule.setText(dataAdapterOBJ.get_date_workschedule());
        Viewholder.time_workschedule.setText(dataAdapterOBJ.get_time_workschedule());

        if (GlobalVariable.nowDate.equals(dataAdapterOBJ.get_date_workschedule())){
            Viewholder.week_workschedule.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            Viewholder.date_workschedule.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

    }

    @Override
    public int getItemCount() {
        return dataAdapters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        final TextView week_workschedule;
        final TextView date_workschedule;
        final TextView time_workschedule;

        ViewHolder(View itemView) {

            super(itemView);
            week_workschedule = itemView.findViewById(R.id.week_workschedule);
            date_workschedule = itemView.findViewById(R.id.date_workschedule);
            time_workschedule = itemView.findViewById(R.id.time_workschedule);
        }
    }

    public void addList(List<WorkScheduleModel> dataAdapter){
        dataAdapters.addAll(dataAdapter);
        notifyItemRangeChanged(0,dataAdapters.size());
    }
    public void refreshList(){
        notifyItemRangeChanged(0,dataAdapters.size());
    }
    public List<WorkScheduleModel> getListData(){
        return dataAdapters;
    }

}
