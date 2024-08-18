package repartidor.faster.com.ec.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import repartidor.faster.com.ec.Getset.paymentGetSet;
import repartidor.faster.com.ec.R;


public class PaymentAdapter extends BaseAdapter {
    private final ArrayList<paymentGetSet> dat;
    private final Context context;
    private LayoutInflater inflater = null;
    private String orderAmount;

    public PaymentAdapter(ArrayList<paymentGetSet> dat, Context context) {
        this.dat = dat;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
            vi = inflater.inflate(R.layout.cell_payment, parent,false);
        }
        ImageView img_user = vi.findViewById(R.id.img_user);

        TextView txt_orderNo = vi.findViewById(R.id.txt_orderNo);
        TextView txt_free_delivery = vi.findViewById(R.id.txt_free_delivery);
        TextView txt_paymentDates = vi.findViewById(R.id.txt_PaymentDates);
        String orderNo = "";
        String itemdates;
        String time = dat.get(position).getPaymentMaxDate();

        switch (dat.get(position).getComplete()) {
            case "24": //Pendiente
                orderNo = "Pago Pendiente #" + dat.get(position).getPaymentNo();
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_payment_pending));
                orderAmount = "Monto a Pagar: " + context.getString(R.string.currency) + dat.get(position).getPaymentAmount();
                itemdates = "PENDIENTE";
                txt_paymentDates.setTextColor(context.getResources().getColor(R.color.green2));
                txt_paymentDates.setText(itemdates);
                break;
            case "25": // Completdo
                orderNo = "Pago Realizado #" + dat.get(position).getPaymentNo();
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_payment_completed));
                itemdates = "PAGADO";
                txt_paymentDates.setText(itemdates);
                txt_paymentDates.setTextColor(context.getResources().getColor(R.color.black));
                orderAmount = "Monto pagado: " + context.getString(R.string.currency) + dat.get(position).getPaymentAmount();
                break;
            case "26": //Rechazado
                orderNo = "Pago Rechazado #" + dat.get(position).getPaymentNo();
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_payment_pending));
                orderAmount = "Monto a Pagar: " + context.getString(R.string.currency) + dat.get(position).getPaymentAmount();
                itemdates = "RECHAZADO";
                txt_paymentDates.setTextColor(context.getResources().getColor(R.color.red));
                txt_paymentDates.setText(itemdates);
                break;
            case "27": // Impago
                orderNo = "Pago Pendiente #" + dat.get(position).getPaymentNo();
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_payment_pending));
                orderAmount = "Monto a Pagar: " + context.getString(R.string.currency) + dat.get(position).getPaymentAmount();
                itemdates = "IMPAGO";
                txt_paymentDates.setTextColor(context.getResources().getColor(R.color.red));
                txt_paymentDates.setText(itemdates);
                break;
            case "30": // En Revisión
                orderNo = "Pago en Revisión #" + dat.get(position).getPaymentNo();
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_payment_pending));
                orderAmount = "Monto Pagado: " + context.getString(R.string.currency) + dat.get(position).getPaymentAmount();
                itemdates = "PROCESANDO";
                txt_paymentDates.setTextColor(context.getResources().getColor(R.color.pago_revision));
                txt_paymentDates.setText(itemdates);
                break;
            case "defeated": //Vencido
                orderNo = "Pago Vencido #" + dat.get(position).getPaymentNo();
                img_user.setImageDrawable(context.getDrawable(R.drawable.img_payment_defeated));
                itemdates = "VENCIDO";
                txt_paymentDates.setTextColor(Color.RED);
                txt_paymentDates.setText(itemdates);
                orderAmount = "Monto a pagar: " + context.getString(R.string.currency) + dat.get(position).getPaymentAmount();
                break;
        }

        txt_free_delivery.setText("Envío gratis: $" + dat.get(position).getFreeDelivery()
                + " | % Comisión: $" + dat.get(position).getCommFreeDelivery());

        txt_orderNo.setText(orderNo);
        TextView txt_orderAmount = vi.findViewById(R.id.txt_orderAmount);
        txt_orderAmount.setText(orderAmount);

        TextView txt_orderQuantity = vi.findViewById(R.id.txt_orderQuantity);
        String itemNum = "Delivery (" + dat.get(position).getOrderQuantity()+")";
        txt_orderQuantity.setText(itemNum);

        TextView txt_orderDateTime = vi.findViewById(R.id.txt_orderDateTime);
        String paymentDate;
        if(dat.get(position).getPaymentDate().equals("null")){
            paymentDate="Vence: "+dat.get(position).getPaymentMaxDate();
        } else {
            paymentDate="Pagado: "+dat.get(position).getPaymentDate();
        }
        txt_orderDateTime.setText(paymentDate);

        return vi;
    }

    /*private void flipImage(Boolean ifTrue, ImageView imageView){
        if(ifTrue){
            imageView.setImageDrawable(context.getDrawable(R.drawable.img_orderdelivered));
        } else imageView.setImageDrawable(context.getDrawable(R.drawable.img_orderprocess));

    }*/
}
