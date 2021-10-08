package com.nether.wifip2pdemo;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.nether.wifip2pdemo.databinding.ActivityMainBinding;
import com.nether.wifip2pdemo.nearby.NearbyActivity;
import com.nether.wifip2pdemo.p2p.WIFIP2PBroadcastReceiver;
import com.nether.wifip2pdemo.p2p.WifiDirectActivity;
import com.nether.wifip2pdemo.p2p.client.P2pClient;
import com.nether.wifip2pdemo.p2p.server.P2pServer;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.btnWifiDirect.setOnClickListener(view -> {
      Intent intent = new Intent(MainActivity.this, WifiDirectActivity.class);
      startActivity(intent);
    });

    binding.btnNearby.setOnClickListener(view -> {
      Intent intent = new Intent(MainActivity.this, NearbyActivity.class);
      startActivity(intent);
    });
  }

}