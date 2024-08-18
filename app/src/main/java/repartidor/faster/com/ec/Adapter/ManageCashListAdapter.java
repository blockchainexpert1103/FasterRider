package repartidor.faster.com.ec.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import repartidor.faster.com.ec.Getset.TravelHistorySet;
import repartidor.faster.com.ec.R;

public class ManageCashListAdapter extends BaseAdapter {
    private final ArrayList<TravelHistorySet> dat;
    private final Context context;
    private LayoutInflater inflater = null;

    public ManageCashListAdapter(ArrayList<TravelHistorySet> dat, Context context) {
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
            vi = inflater.inflate(R.layout.cash_list, parent,false);
        }

        TextView txt_details = vi.findViewById(R.id.txt_details);
        TextView txt_traveltime = vi.findViewById(R.id.txt_date);
        TextView txt_voucher = vi.findViewById(R.id.txt_voucher);
        TextView txt_amount = vi.findViewById(R.id.txt_amount);
        ImageView img = vi.findViewById(R.id.image);

        txt_voucher.setText(dat.get(position).getCashVoucher());
        txt_amount.setText(dat.get(position).getCashAmount());
        if (!dat.get(position).getCashDetail().isEmpty() && !dat.get(position).getCashDetail().equals("null")){
            txt_details.setText(dat.get(position).getCashDetail());
        } else{
            txt_details.setText("Sin descripción");
        }
        txt_traveltime.setText(dat.get(position).getHistoryDate());
        Picasso.get().load(dat.get(position).getPhoto()).into(img);

        img.setOnClickListener(v -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
            View mView = inflater.inflate(R.layout.dialog_custom_layout, null);
            PhotoView photoView = mView.findViewById(R.id.photophoto);
            Picasso.get()
                    .load(dat.get(position).getPhoto())
                    .into(photoView);
            mBuilder.setView(mView);
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });
        return vi;
    }

}
