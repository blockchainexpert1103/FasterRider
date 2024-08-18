package repartidor.faster.com.ec.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Objects;

import repartidor.faster.com.ec.Getset.DeliveryGetSet;
import repartidor.faster.com.ec.R;
import repartidor.faster.com.ec.utils.MyCustomTimer;


public class DeliveryAdapter extends BaseAdapter {
    private final ArrayList<DeliveryGetSet> dat;
    private final Context context;
    private LayoutInflater inflater = null;
    private MyCustomTimer myTimer;
    private String DeliveryBoyLevel;

    public DeliveryAdapter(ArrayList<DeliveryGetSet> dat, Context context) {
        this.dat = dat;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myTimer= new MyCustomTimer();
    }

    @Override
    public int getCount() {
        return dat.size();
    }

    @Override
    public Object getItem(int position) {
        return dat.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;

        if(convertView==null) {
            vi = inflater.inflate(R.layout.celldelivery, parent,false);
        }
        ImageView img_user = vi.findViewById(R.id.img_status);
        TextView txt_order_address = vi.findViewById(R.id.txt_order_address);
        TextView txt_deliveryTime = vi.findViewById(R.id.txt_deliveryAmount);


        switch (dat.get(position).getStatus()) {
            case "0":
                if (dat.get(position).getRiderResponse().equals("48") && Objects.equals(dat.get(position).getRiderId(), dat.get(position).getRiderIsAssigned())) {
                    // Log.e("myListData", dat.get(position).getRiderResponse());
                    img_user.setImageDrawable(context.getDrawable(R.drawable.self_acceptance));
                } else {
                    img_user.setImageDrawable(context.getDrawable(R.drawable.img_cus_request));
                }

                //img_user.setImageDrawable(context.getDrawable(R.drawable.img_cus_request));
                if (!dat.get(position).getDeliveryBoyLevelId().equals("3")){
                    txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate());
                } else {
                    txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + dat.get(position).getOrderDelivery());
                }
                txt_order_address.setText("Dir.: " + dat.get(position).getOrderAddress());
                break;
            case "5":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderprocess));
                if (dat.get(position).getDeliveryBoyLevelId().equals("1")){
                    txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate());
                } else {
                    txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + dat.get(position).getOrderDelivery());
                }
                txt_order_address.setText("Dir.: " + dat.get(position).getOrderAddress());
                break;
            case "1":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderprepare));
                if (dat.get(position).getDeliveryBoyLevelId().equals("1")){
                    txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate());
                } else {
                    txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + dat.get(position).getOrderDelivery());
                }
                txt_order_address.setText("Dir.: " + dat.get(position).getOrderAddress());
                break;
            case "3":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderpicked));
                txt_deliveryTime.setText("Creado: " + dat.get(position).getOrderDate() + " | " + "Delivery: " + context.getString(R.string.currency) + dat.get(position).getOrderDelivery());
                txt_order_address.setText("Dir.: " + dat.get(position).getOrderAddress());
                break;
            case "4":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_orderdelivered));
                txt_order_address.setText("Dir.: " + dat.get(position).getOrderAddress());
                txt_deliveryTime.setText("Delivery: " + context.getString(R.string.currency) + dat.get(position).getOrderDelivery() + " | Producto $" + dat.get(position).getOrderAmount());
                break;
            case "2":
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_res_rejeact));
                txt_order_address.setText("Dir.: " + dat.get(position).getOrderAddress());
                txt_deliveryTime.setText("Delivery: " + context.getString(R.string.currency) + dat.get(position).getOrderDelivery() + " | Producto $" + dat.get(position).getOrderAmount());
                break;
        }

        TextView txt_orderNo = vi.findViewById(R.id.txt_resName);
        String resName = dat.get(position).getResName();
        if (dat.get(position).getFreeDelivery().equals("1")) {
            txt_orderNo.setText(resName);
            txt_orderNo.setTextColor(context.getResources().getColor(R.color.green2));
        } else {
            txt_orderNo.setText(resName);
        }

        TextView txt_orderAmount = vi.findViewById(R.id.txt_orderAmount);

        String orderAmount = "#"+dat.get(position).getOrderNo()+" | "+"Producto: "+context.getString(R.string.currency)+dat.get(position).getOrderAmount();
        txt_orderAmount.setText(orderAmount);

        TextView txt_orderTime = vi.findViewById(R.id.txt_orderTime);
        if(dat.get(position).getOrderTime().equals("0")){
            if (dat.get(position).getStatus().equals("4")|| dat.get(position).getStatus().equals("2") || dat.get(position).getStatus().equals("6") ||dat.get(position).getStatus().equals("7"))
            {
                txt_orderAmount.setText("#"+dat.get(position).getOrderNo() +" | "+dat.get(position).getOrderDate());
            }

        } else{
            myTimer.setTimer(Integer.parseInt(dat.get(position).getOrderTime()),txt_orderTime);
        }

        return vi;
    }

}
