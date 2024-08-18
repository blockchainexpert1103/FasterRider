package repartidor.faster.com.ec.Adapter;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import repartidor.faster.com.ec.Getset.DeliveryGetSet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.motorizado.ManageCash;

public class DeliveryRecyclerViewCashAdapter extends RecyclerView.Adapter<DeliveryRecyclerViewCashAdapter.ViewHolder>{
    private final ArrayList<DeliveryGetSet> listdata;
    private final Context context;
    private static final String MY_PREFS_ACTIVITY = "DeliveryActivity";
    private static final String MY_PREFS_NAME = "Fooddelivery";

    // RecyclerView recyclerView;
    public DeliveryRecyclerViewCashAdapter(Context context, ArrayList<DeliveryGetSet> dat) {
        this.listdata = dat;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.cell_manage_cash, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DeliveryGetSet myListData = listdata.get(position);

        ImageView img_user = holder.img_user;
        TextView txt_id = holder.txt_id;
        TextView txt_opening_date = holder.txt_opening_date;
        TextView txt_closing_amount = holder.txt_closing_amount;
        TextView txt_total_income = holder.txt_total_income;
        TextView txt_total_expense = holder.txt_total_expense;
        TextView txt_total_cash = holder.txt_total_cash;
        RelativeLayout delivery_order_card = holder.delivery_order_card;

        if (myListData.getClosingDate().equals("Pendiente")) {
            img_user.setImageDrawable(context.getDrawable(R.drawable.cash_open));
        } else {
            img_user.setImageDrawable(context.getDrawable(R.drawable.cash_close));
        }

        txt_id.setText("ID caja #" + myListData.getCashId());
        txt_opening_date.setText("Inicia: $" + myListData.getOpeningAmount() + " | " + myListData.getOpeningDate());
        txt_closing_amount.setText("Cierra: $" + myListData.getClosingAmount() + " | " + myListData.getClosingDate());
        txt_total_income.setText("Total generado: $" + myListData.getTotalIncome());
        txt_total_expense.setText("Total gasto: $" + myListData.getTotalExpense());
        txt_total_cash.setText("Total Caja Cerrada: $" + myListData.getClosingAmount());

        if(!listdata.isEmpty()){
            delivery_order_card.setOnClickListener(v -> {
                SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
                editor.putBoolean("Main", false);
                editor.putString("Activity", "ManageCash");
                editor.apply();

                Intent i = new Intent(context, ManageCash.class);
                i.putExtra("DeliveryBoyId", context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", ""));
                i.putExtra("cash_id", myListData.getCashId());
                context.startActivity(i);
            });
        }
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView img_user;
        public TextView txt_id;
        public TextView txt_opening_date;
        public TextView txt_closing_amount;
        public TextView txt_total_income;
        public TextView txt_total_expense;
        public TextView txt_total_cash;
        public RelativeLayout delivery_order_card;
        public ViewHolder(View itemView) {
            super(itemView);
            this.img_user = itemView.findViewById(R.id.img_status);
            this.txt_id = itemView.findViewById(R.id.txt_id);
            this.txt_opening_date = itemView.findViewById(R.id.txt_opening_date);
            this.txt_closing_amount = itemView.findViewById(R.id.txt_closing_amount);
            this.txt_total_income = itemView.findViewById(R.id.txt_total_income);
            this.txt_total_expense = itemView.findViewById(R.id.txt_total_expense);
            this.txt_total_cash = itemView.findViewById(R.id.txt_total_cash);
            this.delivery_order_card = itemView.findViewById(R.id.delivery_order_card);
        }

    }
}