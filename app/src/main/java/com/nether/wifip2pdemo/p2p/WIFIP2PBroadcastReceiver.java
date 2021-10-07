package com.nether.wifip2pdemo.p2p;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import java.util.ArrayList;
import java.util.List;

public class WIFIP2PBroadcastReceiver extends BroadcastReceiver implements
    LifecycleObserver {

  private static final String TAG = "WIFIP2PBroadcastReceiver";
  private WifiP2pManager manager;
  private Channel channel;
  private Context context;
  private WIFIP2pListener wifip2pListener;

  public WIFIP2PBroadcastReceiver(Context context, WifiP2pManager manager, Channel channel) {
    this.context = context;
    this.manager = manager;
    this.channel = channel;
  }

  public WIFIP2pListener getWifip2pListener() {
    return wifip2pListener;
  }

  public void setWifip2pListener(WIFIP2pListener wifip2pListener) {
    this.wifip2pListener = wifip2pListener;
  }

  public void register() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    intentFilter.addAction(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
    context.registerReceiver(this, intentFilter);
  }

  @OnLifecycleEvent(Event.ON_DESTROY)
  public void unregister(Context context) {
    context.unregisterReceiver(this);
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    switch (action) {
      case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        boolean isEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
        if (wifip2pListener != null) {
          wifip2pListener.onWifiP2pEnabled(isEnabled);
        }
        break;
      }
      case WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION: {
        int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,
            WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
        boolean isDiscover = discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED;
        Log.d(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION:" + isDiscover);
        if (wifip2pListener != null) {
          wifip2pListener.onDiscovery(isDiscover);
        }
        break;
      }
      case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
        WifiP2pDeviceList wifiP2pDeviceList = intent
            .getParcelableExtra(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION:" + wifiP2pDeviceList);
        if (wifip2pListener != null) {
          wifip2pListener.onPeerListChanged(wifiP2pDeviceList);
        }
        manager.requestPeers(channel, wifiP2pDeviceList1 -> {
          if (wifip2pListener != null) {
            wifip2pListener.onPeerListChanged(wifiP2pDeviceList1);
          }
        });
        break;
      }
      case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        WifiP2pInfo WifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        Log.d(TAG,
            "WIFI_P2P_CONNECTION_CHANGED_ACTION:" + networkInfo + ",wifi info:" + WifiP2pInfo);
        if (wifip2pListener != null) {
          wifip2pListener.onP2pConnectionChanged(WifiP2pInfo, networkInfo);
        }
        break;
      }
      case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
        WifiP2pDevice wifiP2pDevice = intent
            .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:" + wifiP2pDevice);
        break;
      }
    }
  }

  private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

  private PeerListListener peerListListener = new PeerListListener() {
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
      Log.d(TAG, "PeerListListener:" + peerList);
//      connect();
      List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());

      if (!refreshedPeers.equals(peers)) {
        peers.clear();
        peers.addAll(refreshedPeers);

        // If an AdapterView is backed by this data, notify it
        // of the change. For instance, if you have a ListView of
        // available peers, trigger an update.
        connect();
//        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

        // Perform any other updates needed based on the new list of
        // peers connected to the Wi-Fi P2P network.
      }
//
//      if (peers.size() == 0) {
//        Log.d(WiFiDirectActivity.TAG, "No devices found");
//        return;
//      }
    }
  };

  @SuppressLint("MissingPermission")
  public void connect() {
    // Picking the first device found on the network.
    WifiP2pDevice device = peers.get(0);

    WifiP2pConfig config = new WifiP2pConfig();
    config.deviceAddress = device.deviceAddress;
    config.wps.setup = WpsInfo.PBC;

    manager.connect(channel, config, new ActionListener() {

      @Override
      public void onSuccess() {
        // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
        Log.d(TAG, "connect onSuccess");

      }

      @Override
      public void onFailure(int reason) {
//        Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
//            Toast.LENGTH_SHORT).show();
        Log.d(TAG, "connect onFailure:" + reason);

      }
    });
  }
}
