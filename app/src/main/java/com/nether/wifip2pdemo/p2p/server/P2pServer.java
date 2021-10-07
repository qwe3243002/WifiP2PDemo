package com.nether.wifip2pdemo.p2p.server;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.nether.wifip2pdemo.p2p.WIFIP2PBroadcastReceiver;
import com.nether.wifip2pdemo.p2p.WIFIP2pListener;

public class P2pServer implements LifecycleObserver, WIFIP2pListener {

  private static final int SERVER_PORT = 10086;
  private Context context;
  private WifiP2pManager manager;
  private Channel channel;
  private WIFIP2PBroadcastReceiver wifip2PBroadcastReceiver;
  private static final String TAG = "P2pServer";
  public static final P2pServer INSTANCE = new P2pServer();
  private P2pServerSocket p2pServerSocket;

  private P2pServer() {
  }

  public void init(@NonNull Context context) {
    this.context = context;
    manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
    channel = manager.initialize(context, context.getMainLooper(), null);
    wifip2PBroadcastReceiver = new WIFIP2PBroadcastReceiver(context, manager, channel);
    wifip2PBroadcastReceiver.setWifip2pListener(this);
  }

  public void startServer() {
    if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(context, "Not grant permission!!!", Toast.LENGTH_SHORT).show();
      return;
    }
    manager.cancelConnect(channel, null);
    manager.removeGroup(channel, null);
    wifip2PBroadcastReceiver.register();
  }

  @OnLifecycleEvent(Event.ON_DESTROY)
  protected void stopServer() {
    manager.cancelConnect(channel, null);
    manager.removeGroup(channel, new ActionListener() {
      @Override
      public void onSuccess() {
        P2pServer.this.onRemoveGroup(true);
      }

      @Override
      public void onFailure(int i) {
        P2pServer.this.onRemoveGroup(false);
      }
    });
  }

  public void send(String msg) {
    if (p2pServerSocket != null) {
      p2pServerSocket.send(msg);
    }
  }

  @Override
  public void onCreateGroup(boolean success) {
    String content = success ? "createGroup success" : "createGroup failure";
    Log.d(TAG, content);
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    if (success) {
      p2pServerSocket = new P2pServerSocket(SERVER_PORT);
    }
  }

  @Override
  public void onRemoveGroup(boolean success) {
    Toast.makeText(context, "remove Group", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onWifiP2pEnabled(boolean enabled) {
    if (enabled) {
      if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        return;
      }
      manager.createGroup(channel, new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
          P2pServer.this.onCreateGroup(true);
        }

        @Override
        public void onFailure(int reason) {
          P2pServer.this.onCreateGroup(false);
        }
      });
    }
  }

  @Override
  public void onDiscovery(boolean isDiscovery) {

  }

  @Override
  public void onPeerListChanged(WifiP2pDeviceList peerList) {

  }

  @Override
  public void onP2pConnectionChanged(WifiP2pInfo wifiP2pInfo, NetworkInfo networkInfo) {

  }
}
