package com.example.fuelisticv2driver.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.Model.ShippingOrderModel;
import com.example.fuelisticv2driver.R;
import com.example.fuelisticv2driver.ShippingActivity;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;

public class MyShippingOrderAdapter extends RecyclerView.Adapter<MyShippingOrderAdapter.MyViewHolder> {

    Context context;
    List<ShippingOrderModel> shippingOrderModelList;
    SimpleDateFormat simpleDateFormat;

    public MyShippingOrderAdapter(Context context, List<ShippingOrderModel> shippingOrderModelList) {
        this.context = context;
        this.shippingOrderModelList = shippingOrderModelList;
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

    }

    @Override
    public int getItemCount() {
        return shippingOrderModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }

    }
}
