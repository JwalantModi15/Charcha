package com.techinnovator.jmcharcha;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.techinnovator.jmcharcha.common.Util;
import com.techinnovator.jmcharcha.login.LoginActivity;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Util.updateDeviceToken(this, s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Uri notiSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("chat_app_1", "chat_app_noti", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("chat app notifications");
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, "chat_app_1");
        }
        else{
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.drawable.ic_chat);
        builder.setColor(getResources().getColor(R.color.purple_500));
        builder.setContentTitle(title);
        builder.setAutoCancel(true);
        builder.setSound(notiSound);
        builder.setContentIntent(pendingIntent);

        if(message.startsWith("https://firebasestorage.")){
            try{
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                Glide.with(this).asBitmap().load(message).into(new CustomTarget<Bitmap>(200,115) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bigPictureStyle.bigPicture(resource);
                        builder.setStyle(bigPictureStyle);
                        notificationManager.notify(999, builder.build());
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            }
            catch (Exception e){
                builder.setContentText("New File Received");
            }
        }
        else{
            builder.setContentText(message);
            notificationManager.notify(999, builder.build());
        }
    }
}