package com.example.fuelisticv2driver.Callback;

import com.example.fuelisticv2driver.Model.ShippingOrderModel;

import java.util.List;

public interface IShippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList);
    void onShippingOrderLoadFailure(String message);
}
