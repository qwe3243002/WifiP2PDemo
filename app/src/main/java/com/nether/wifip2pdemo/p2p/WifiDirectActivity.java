package com.nether.wifip2pdemo.p2p;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.nether.wifip2pdemo.MainActivity;
import com.nether.wifip2pdemo.R;
import com.nether.wifip2pdemo.databinding.ActivityWifiDirectBinding;
import com.nether.wifip2pdemo.p2p.client.P2pClient;
import com.nether.wifip2pdemo.p2p.server.P2pServer;

public class WifiDirectActivity extends AppCompatActivity {

  private ActivityWifiDirectBinding binding;
  private P2pServer p2pServer = P2pServer.INSTANCE;
  private P2pClient p2pClient = P2pClient.INSTANCE;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityWifiDirectBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.btnStartHost.setOnClickListener(view -> {
      WifiDirectActivity.this.getLifecycle().addObserver(p2pServer);
      p2pServer.init(WifiDirectActivity.this);
      p2pServer.startServer();
    });

    binding.btnStartClient.setOnClickListener(view -> {
      p2pClient.init(WifiDirectActivity.this);
      WifiDirectActivity.this.getLifecycle().addObserver(p2pClient);
    });
  }
}