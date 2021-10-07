package com.nether.wifip2pdemo;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.nether.wifip2pdemo.databinding.ActivityMainBinding;
import com.nether.wifip2pdemo.p2p.WIFIP2PBroadcastReceiver;
import com.nether.wifip2pdemo.p2p.client.P2pClient;
import com.nether.wifip2pdemo.p2p.server.P2pServer;

public class MainActivity extends AppCompatActivity {

  private ActivityMainBinding binding;
  private P2pServer p2pServer = P2pServer.INSTANCE;
  private P2pClient p2pClient = P2pClient.INSTANCE;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.btnStartHost.setOnClickListener(view -> {
      MainActivity.this.getLifecycle().addObserver(p2pServer);
      p2pServer.init(MainActivity.this);
      p2pServer.startServer();
    });

    binding.btnStartClient.setOnClickListener(view -> {
      p2pClient.init(MainActivity.this);
      MainActivity.this.getLifecycle().addObserver(p2pClient);
    });
  }

}