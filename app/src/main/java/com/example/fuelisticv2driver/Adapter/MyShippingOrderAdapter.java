package com.example.fuelisticv2driver.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelisticv2driver.Callback.IRecyclerClickListener;
import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.Model.ShippingOrderModel;
import com.example.fuelisticv2driver.R;
import com.example.fuelisticv2driver.ShippingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;

public class MyShippingOrderAdapter extends RecyclerView.Adapter<MyShippingOrderAdapter.MyViewHolder> {

    Context context;
    List<ShippingOrderModel> shippingOrderModelList;
    SimpleDateFormat simpleDateFormat;
    Calendar calendar;


    public MyShippingOrderAdapter(Context context, List<ShippingOrderModel> shippingOrderModelList) {
        this.context = context;
        this.shippingOrderModelList = shippingOrderModelList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Paper.init(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_order_driver, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Common.setSpanString("Delivery on: ", shippingOrderModelList.get(position).getOrderModel().getDeliveryDate(),
                holder.txt_delivery_date );
        Common.setSpanStringColor("No. : ", shippingOrderModelList.get(position).getOrderModel().getKey(),
                holder.txt_order_num, Color.parseColor("#BA454A"));
        Common.setSpanStringColor("Address: ", shippingOrderModelList.get(position).getOrderModel().getShippingAddress(),
                holder.txt_order_address, Color.parseColor("#BA454A"));
        Common.setSpanStringColor("Payment : ", shippingOrderModelList.get(position).getOrderModel().getTransactionId(),
                holder.txt_payment, Color.parseColor("#BA454A"));

        //disable button if already start trip
        if(shippingOrderModelList.get(position).isStartTrip())
        {
            holder.btn_deliver_now.setEnabled(false);
        }

        // Event
        holder.btn_deliver_now.setOnClickListener(view -> {
            Paper.book().write(Common.SHIPPING_ORDER_DATA, new Gson().toJson(shippingOrderModelList.get(position)));
            context.startActivity(new Intent(context, ShippingActivity.class));
        });

        holder.setRecyclerClickListener((view, position1) -> showDialog(position1));

    }

    private void showDialog(int position1) {
        View layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_order_details, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout_dialog);

        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);
        TextInputEditText order_details_order_num= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_order_num);
        TextInputEditText order_details_cust_name= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_cust_name);
        TextInputEditText order_details_cust_phone= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_cust_phone);
        TextInputEditText order_details_transaction_id= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_transaction_id);
        TextInputEditText order_details_delivery_address= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_delivery_address);
        TextInputEditText order_details_delivery_charge= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_delivery_charge);
        TextInputEditText order_details_total_order_amt= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_total_order_amt);
        TextInputEditText order_details_order_date= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_order_date);
        TextInputEditText order_details_order_quantity= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_order_quantity);
        TextInputEditText order_details_delivery_mode= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_delivery_mode);
        TextInputEditText order_details_delivery_cmt= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_delivery_cmt);
        TextInputEditText order_details_delivery_date= (TextInputEditText) layout_dialog.findViewById(R.id.order_details_delivery_date);

        order_details_order_num.setText(shippingOrderModelList.get(position1).getKey());
        order_details_cust_name.setText(shippingOrderModelList.get(position1).getOrderModel().getUserName());
        order_details_cust_phone.setText(shippingOrderModelList.get(position1).getOrderModel().getUserPhone());
        order_details_transaction_id.setText(shippingOrderModelList.get(position1).getOrderModel().getTransactionId());
        order_details_delivery_address.setText(shippingOrderModelList.get(position1).getOrderModel().getShippingAddress());
        order_details_delivery_charge.setText(Integer.toString(shippingOrderModelList.get(position1).getOrderModel().getDeliveryCharge()));

        calendar.setTimeInMillis(shippingOrderModelList.get(position1).getOrderModel().getOrderDate());
        Date date = new Date(shippingOrderModelList.get(position1).getOrderModel().getOrderDate());
        order_details_order_date.setText(new StringBuilder(Common.getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
                .append(", ")
                .append(simpleDateFormat.format(date)));
        order_details_total_order_amt.setText(String.valueOf(shippingOrderModelList.get(position1).getOrderModel().getTotalPayment()));
        order_details_order_quantity.setText(shippingOrderModelList.get(position1).getOrderModel().getQuantity());
        order_details_delivery_mode.setText(shippingOrderModelList.get(position1).getOrderModel().getDeliveryMode());
        order_details_delivery_cmt.setText(shippingOrderModelList.get(position1).getOrderModel().getComment());
        order_details_delivery_date.setText(shippingOrderModelList.get(position1).getOrderModel().getDeliveryDate());


        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_ok.setOnClickListener(view -> dialog.dismiss());
    }

    @Override
    public int getItemCount() {
        return shippingOrderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;

        @BindView(R.id.txt_delivery_date)
        TextView txt_delivery_date;

        @BindView(R.id.txt_order_address)
        TextView txt_order_address;

        @BindView(R.id.txt_order_num)
        TextView txt_order_num;

        @BindView(R.id.txt_payment)
        TextView txt_payment;

        @BindView(R.id.btn_deliver_now)
        MaterialButton btn_deliver_now;

        IRecyclerClickListener recyclerClickListener;

        public void setRecyclerClickListener(IRecyclerClickListener recyclerClickListener) {
            this.recyclerClickListener = recyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

        }
        @Override
        public void onClick(View view) {
            recyclerClickListener.onItemClickListener(view, getAdapterPosition());
        }

    }
}
