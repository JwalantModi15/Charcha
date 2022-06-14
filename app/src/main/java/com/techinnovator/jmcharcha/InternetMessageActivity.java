package com.techinnovator.jmcharcha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.techinnovator.jmcharcha.common.Util;

public class InternetMessageActivity extends AppCompatActivity {

    TextView txtCheckInternet;

    Button btnClose, btnRetry;

    ProgressBar internetCheckProgressBar;

    ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_message);

        txtCheckInternet = findViewById(R.id.txtCheckInternet);
        btnRetry = findViewById(R.id.btnRetry);
        btnClose = findViewById(R.id.btnClose);
        internetCheckProgressBar = findViewById(R.id.internetCheckProgressBar);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                }
            };
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), networkCallback);
        }
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internetCheckProgressBar.setVisibility(View.VISIBLE);

                if(Util.isConnectionAvailable(InternetMessageActivity.this)){
                    finish();
                }
                else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            internetCheckProgressBar.setVisibility(View.GONE);
                        }
                    }, 1000);
                }
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
    }
}