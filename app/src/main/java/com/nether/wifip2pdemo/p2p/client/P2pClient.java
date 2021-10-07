package com.nether.wifip2pdemo.p2p.client;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.nether.wifip2pdemo.R;
import com.nether.wifip2pdemo.p2p.WIFIP2PBroadcastReceiver;
import com.nether.wifip2pdemo.p2p.WIFIP2pListener;
import java.util.ArrayList;
import java.util.List;

public class P2pClient implements LifecycleObserver, WIFIP2pListener {

  private static final int SERVER_PORT = 10086;

  private Context context;
  private WifiP2pManager manager;
  private Channel channel;
  private WIFIP2PBroadcastReceiver wifip2PBroadcastReceiver;
  private static final String TAG = "P2pClient";
  public static final P2pClient INSTANCE = new P2pClient();
  private P2pClientSocket clientSocket;

  private P2pClient() {
  }

  public void init(@NonNull Context context) {
    this.context = context;
    manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
    channel = manager.initialize(context, context.getMainLooper(), null);
    wifip2PBroadcastReceiver = new WIFIP2PBroadcastReceiver(context, manager, channel);
    wifip2PBroadcastReceiver.setWifip2pListener(this);
    wifip2PBroadcastReceiver.register();
  }


  public void send(String msg) {
    if (clientSocket != null) {
      clientSocket.send(msg);
    }
  }

  @Override
  public void onCreateGroup(boolean success) {

  }

  @Override
  public void onRemoveGroup(boolean success) {

  }

  @Override
  public void onWifiP2pEnabled(boolean enabled) {
    Log.d(TAG, "onWifiP2pEnabled:" + enabled);
    if (!enabled) {
      return;
    }
    if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    manager.discoverPeers(channel, new ActionListener() {
      @Override
      public void onSuccess() {
        Log.d(TAG, "discoverPeers onSuccess:");
      }

      @Override
      public void onFailure(int code) {
        Log.d(TAG, "discoverPeers onFailure:" + code);
      }
    });
  }

  @Override
  public void onDiscovery(boolean isDiscovery) {
  }

  @Override
  public void onPeerListChanged(WifiP2pDeviceList peerList) {
    Log.d(TAG, "onPeerListChanged:" + peerList);
    if (peerList == null || peerList.getDeviceList().size() == 0) {
      return;
    }
    if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    List<WifiP2pDevice> wifiP2pDeviceList = new ArrayList<>(peerList.getDeviceList());
    WifiP2pDevice selectDevice = wifiP2pDeviceList.get(0);
    if (selectDevice.status == WifiP2pDevice.AVAILABLE) {
      WifiP2pConfig config = new WifiP2pConfig();
      config.deviceAddress = selectDevice.deviceAddress;
      config.wps.setup = WpsInfo.PBC;
      if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      manager.cancelConnect(channel, null);
      ((Activity) context).findViewById(R.id.btn_start_client).postDelayed(() -> {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
          @Override
          public void onSuccess() {
            Log.d(TAG, "p2p connect action success");
          }

          @Override
          public void onFailure(int reason) {
            Log.d(TAG, "p2p connect action failure");
          }
        });
      }, 1000);
    }
  }

  @Override
  public void onP2pConnectionChanged(WifiP2pInfo wifiP2pInfo, NetworkInfo networkInfo) {
    Log.d(TAG, "WifiP2pInfo:" + wifiP2pInfo + "NetworkInfo:" + networkInfo);
    if (networkInfo.isConnected()) {
      String serverIp = wifiP2pInfo.groupOwnerAddress.getHostAddress();
      clientSocket = new P2pClientSocket(serverIp, SERVER_PORT);
    }
  }

  @OnLifecycleEvent(Event.ON_DESTROY)
  public void disconnectP2p() {
    Log.d(TAG, "disconnectP2p");
    manager.cancelConnect(channel, null);
    manager.removeGroup(channel, null);
  }
}
