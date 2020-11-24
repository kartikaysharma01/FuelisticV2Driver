package com.example.fuelisticv2driver.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fuelisticv2driver.Adapter.MyShippingOrderAdapter;
import com.example.fuelisticv2driver.Common.Common;
import com.example.fuelisticv2driver.Model.Eventbus.UpdateShippingOrderEvent;
import com.example.fuelisticv2driver.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {


    Unbinder unbinder;

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    MyShippingOrderAdapter adapter;

//    AlertDialog dialog;
//    MyDriverAdapter adapter;
//    List<DriverModel> driverModelList;

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(root);
        homeViewModel.getMessageError().observe(getViewLifecycleOwner() , s -> {
            Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
        });
        homeViewModel.getShippingOrderMutableData(Common.currentDriverUser.getPhoneNo()).observe(getViewLifecycleOwner(), shippingOrderModels ->{
            adapter = new MyShippingOrderAdapter(getContext(), shippingOrderModels);
            recycler_order.setAdapter(adapter);
        });
        return root;
    }

    private void initViews(View root){
        unbinder = ButterKnife.bind(this, root);

        recycler_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_order.setLayoutManager(layoutManager);
        recycler_order.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateShippingOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateShippingOrderEvent.class);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateShippingOrder(UpdateShippingOrderEvent event){
        homeViewModel.getShippingOrderMutableData(Common.currentDriverUser.getPhoneNo());           //update data
    }

}