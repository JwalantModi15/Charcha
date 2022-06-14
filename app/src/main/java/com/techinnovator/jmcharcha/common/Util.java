package com.techinnovator.jmcharcha.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Util {

    public  static boolean isConnectionAvailable(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager!=null && connectivityManager.getActiveNetworkInfo()!=null){
            return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    public static void updateDeviceToken(Context context, String token){

        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currUser!=null){
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference databaseReference = rootRef.child("Tokens").child(currUser.getUid());

            HashMap<String, String>map = new HashMap<>();
            map.put("device_token", token);

            databaseReference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(context, "Failed to update device token", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    public static String getTimeAgo(long time){
        System.out.println("yes");
        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60*SECOND_MILLIS;
        final int HOUR_MILLIS = 60*MINUTE_MILLIS;
        final int DAY_MILLIS = 24*HOUR_MILLIS;

        long now = System.currentTimeMillis();

        long diff = now-time;
        Date dateObj = new Date(time);
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("hh:mm aa");
        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("dd-MM-yyyy");
        String res;
        if(diff > DAY_MILLIS){
            System.out.println("date");
            res = simpleDateFormatDate.format(dateObj);
        }
        else{
            System.out.println("time");
            res = simpleDateFormatTime.format(dateObj);
        }
        System.out.println(res);
        return res;
    }
    public static void updateChatDetails(Context context, String currUserId, String chatUserId, String lastMsg){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference chatRef = rootRef.child("Chats").child(chatUserId).child(currUserId);
        DatabaseReference currUserRef = rootRef.child("Chats").child(currUserId).child(chatUserId);

        currUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap hashMap = new HashMap();
                hashMap.put("timestamp", ServerValue.TIMESTAMP);
                hashMap.put("unread_count", "0");
                hashMap.put("last_message", lastMsg);
                hashMap.put("last_message_time", ServerValue.TIMESTAMP);

                HashMap main = new HashMap();
                main.put("Chats"+"/"+currUserId+"/"+chatUserId, hashMap);

                rootRef.updateChildren(main, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error!=null)
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String count="0";
                if(snapshot.child("unread_count").getValue()!=null){
                    count = snapshot.child("unread_count").getValue().toString();
                    System.out.println("not null");
                }
                System.out.println(count);
                HashMap hashMap = new HashMap();
                hashMap.put("timestamp", ServerValue.TIMESTAMP);
                hashMap.put("unread_count", Integer.valueOf(count)+1);
                hashMap.put("last_message", lastMsg);
                hashMap.put("last_message_time", ServerValue.TIMESTAMP);

                HashMap main = new HashMap();
                main.put("Chats"+"/"+chatUserId+"/"+currUserId, hashMap);

                rootRef.updateChildren(main, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if(error!=null)
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(error!=null)
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void sendNotification(Context context, String title, String mes, String userId){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = rootRef.child("Tokens").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("device_token").getValue()!=null){
                    String deviceToken = snapshot.child("device_token").getValue().toString();

                    JSONObject noti = new JSONObject();
                    JSONObject notiData = new JSONObject();
                    try {
                        notiData.put("title", title);
                        notiData.put("message", mes);
                        noti.put("to", deviceToken);
                        noti.put("data", notiData);

                        String url = "https://fcm.googleapis.com/fcm/send";
                        String contentType = "application/json";

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, noti, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println("sent");
                                Toast.makeText(context, "Send notification successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("error "+error);
                                Toast.makeText(context, "Failed to send notification "+error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();
                                map.put("Authorization", "key=AAAA_Cc8CO0:APA91bEbxVXJoY4abhoJxSgG-tMRytXibwZkM_2J4qWMf8Ay_Vx5VHntpT6CMy--ClwnDVQ5ZLMpQSdeHM9aVw947lYKlkj2VqcUrJKXwXgLxLB1aZiRDO4frTBPZW9gGFd-PchB1fQQ");
                                map.put("Sender", "id=1082990004461");
                                map.put("Content-Type", contentType);
                                return map;
                            }
                        };

                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
