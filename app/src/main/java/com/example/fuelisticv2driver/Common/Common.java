package com.example.fuelisticv2driver.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.fuelisticv2driver.Model.DriverUserModel;
import com.example.fuelisticv2driver.Model.TokenModel;
import com.example.fuelisticv2driver.R;
import com.google.firebase.database.FirebaseDatabase;

public class Common {
    public static final String DRIVER_REF = "Drivers";
    public static final String ORDER_REF = "Orders";
    public static final String NOTI_TITLE = "title";
    public static final String NOTI_CONTENT = "content";
    public static final String TOKEN_REF = "Tokens";
    public static final String SHIPPING_ORDER_REF = "ShippingOrder";

    public static DriverUserModel currentDriverUser ;

    public static void setSpanString(String welcome, String fullName, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(fullName);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0,fullName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static void setSpanStringColor(String welcome, String fullName, TextView textView, int parseColor) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(fullName);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0,fullName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(parseColor), 0,fullName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Placed";
            case 1:
                return "Confirmed";
            case 2:
                return "Completed";
            case -1:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "fuelistic_v2";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Fuelistic V2", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Fuelistic V2");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_local_shipping_24));

        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);



    }

    public static void updateToken(Context context, String newToken, boolean isSeller, boolean isDriver) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentDriverUser.getPhoneNo())
                .setValue(new TokenModel(Common.currentDriverUser.getPhoneNo(), newToken, isSeller , isDriver))
                .addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}
