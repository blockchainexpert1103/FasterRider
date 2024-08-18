package repartidor.faster.com.ec.Adapter;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
import repartidor.faster.com.ec.motorizado.DeliveryOrderDetail;
import repartidor.faster.com.ec.motorizado.DeliveryOrderHistory;
import repartidor.faster.com.ec.utils.MyCustomTimer;

public class DeliveryRecyclerViewAdapter extends RecyclerView.Adapter<DeliveryRecyclerViewAdapter.ViewHolder>{
    private final ArrayList<DeliveryGetSet> listdata;
    private final Context context;

    private MyCustomTimer myTimer;

    private static final String MY_PREFS_ACTIVITY = "DeliveryActivity";
    private static final String MY_PREFS_NAME = "Fooddelivery";

    // RecyclerView recyclerView;
    public DeliveryRecyclerViewAdapter(Context context, ArrayList<DeliveryGetSet> dat) {
        this.listdata = dat;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.celldelivery, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DeliveryGetSet myListData = listdata.get(position);

        ImageView img_user = holder.img_user;
        TextView txt_order_address = holder.txt_order_address;
        TextView txt_deliveryTime = holder.txt_deliveryTime;
        TextView txt_orderNo = holder.txt_orderNo;
        TextView txt_orderAmount = holder.txt_orderAmount;
        TextView txt_orderTime = holder.txt_orderTime;
        RelativeLayout delivery_order_card = holder.delivery_order_card;

        switch (myListData.getStatus()) {
            case "0":
                if (myListData.getRiderResponse().equals("46")) {
                    img_user.setImageDrawable(context.getDrawable(R.drawable.self_acceptance));
                } else {
                    img_user.setImageDrawable(context.getDrawable(R.drawable.img_cus_request));
                }

                if (!myListData.getDeliveryBoyLevelId().equals("3")){
                    txt_deliveryTime.setText("Creado: " + myListData.getOrderDate());
                } else {
                    txt_deliveryTime.setText("Creado: " + myListData.getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + myListData.getOrderDelivery());
                }
                txt_order_address.setText("Dir.: " + myListData.getOrderAddress());
                break;
            case "5":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderprocess));
                if (myListData.getDeliveryBoyLevelId().equals("1")){
                    txt_deliveryTime.setText("Creado: " + myListData.getOrderDate());
                } else {
                    txt_deliveryTime.setText("Creado: " + myListData.getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + myListData.getOrderDelivery());
                }
                txt_order_address.setText("Dir.: " + myListData.getOrderAddress());
                break;
            case "1":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderprepare));
                if (myListData.getDeliveryBoyLevelId().equals("1")){
                    txt_deliveryTime.setText("Creado: " + myListData.getOrderDate());
                } else {
                    txt_deliveryTime.setText("Creado: " + myListData.getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + myListData.getOrderDelivery());
                }
                txt_order_address.setText("Dir.: " + myListData.getOrderAddress());
                break;
            case "3":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderpicked));
                txt_deliveryTime.setText("Creado: " + myListData.getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + myListData.getOrderDelivery());
                txt_order_address.setText("Dir.: " + myListData.getOrderAddress());
                break;
            case "4":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderdelivered));
                txt_order_address.setText("Dir.: " + myListData.getOrderAddress());
                txt_deliveryTime.setText("Delivery: " + context.getString(R.string.currency) + myListData.getOrderDelivery() + " | Producto $" + myListData.getOrderAmount());
                break;
            case "2":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_res_rejeact));
                txt_order_address.setText("Dir.: " + myListData.getOrderAddress());
                txt_deliveryTime.setText("Delivery: " + context.getString(R.string.currency) + myListData.getOrderDelivery() + " | Producto $" + myListData.getOrderAmount());
                break;
            /*case "6":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_res_rejeact));
                txt_deliveryTime.setText("Carrera: " + context.getString(R.string.currency) + myListData.getOrderDelivery() + " | Producto $" + myListData.getOrderAmount());
                break;*/
        }

        String resName = myListData.getResName();
        txt_orderNo.setText(resName);

        String orderAmount = "#"+myListData.getOrderNo()+" | "+"Pedido: "+context.getString(R.string.currency)+myListData.getOrderAmount();
        txt_orderAmount.setText(orderAmount);

        if(myListData.getOrderTime().equals("0")){
            if (myListData.getStatus().equals("4")|| myListData.getStatus().equals("2") || myListData.getStatus().equals("6") || myListData.getStatus().equals("7"))
            {
                txt_orderAmount.setText("#"+ myListData.getOrderNo() + " | " + myListData.getOrderDate());
            }

        } else{
            myTimer.setTimer(Integer.parseInt(myListData.getOrderTime()),txt_orderTime);
        }

        if(!listdata.isEmpty()){
            delivery_order_card.setOnClickListener(v -> {
                if(myListData.getStatus().equals("4")) {
                    Toast.makeText(context, "Orden #"+ myListData.getOrderNo() + " entregada con éxito.", Toast.LENGTH_LONG).show();
                } else if(myListData.getStatus().equals("2")){
                    Toast.makeText(context, "Orden #"+ myListData.getOrderNo() + " anulada.", Toast.LENGTH_LONG).show();
                }

                SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_ACTIVITY, MODE_PRIVATE).edit();
                editor.putBoolean("Main", false);
                editor.putString("Activity", "DeliveryOrderDetail");
                editor.apply();

                Intent i = new Intent(context, DeliveryOrderDetail.class);
                i.putExtra("DeliveryBoyId", context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).getString("DeliveryUserId", ""));
                i.putExtra("OrderNo", myListData.getOrderNo());
                i.putExtra("OrderAmount", myListData.getOrderAmount());
                i.putExtra("status", myListData.getStatus());
                i.putExtra("OrderTime", myListData.getOrderTime());
                i.putExtra("DeliveryAmount", myListData.getOrderDelivery());
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
        public TextView txt_order_address;
        public TextView txt_deliveryTime;
        public TextView txt_orderNo;
        public TextView txt_orderAmount;
        public TextView txt_orderTime;
        public RelativeLayout delivery_order_card;
        public ViewHolder(View itemView) {
            super(itemView);
            this.img_user = itemView.findViewById(R.id.img_status);
            this.txt_order_address = itemView.findViewById(R.id.txt_order_address);
            this.txt_deliveryTime = itemView.findViewById(R.id.txt_deliveryAmount);
            this.txt_orderNo = itemView.findViewById(R.id.txt_resName);
            this.txt_orderAmount = itemView.findViewById(R.id.txt_orderAmount);
            this.txt_orderAmount = itemView.findViewById(R.id.txt_orderAmount);
            this.txt_orderTime = itemView.findViewById(R.id.txt_orderTime);
            this.delivery_order_card = itemView.findViewById(R.id.delivery_order_card);
        }

    }
}