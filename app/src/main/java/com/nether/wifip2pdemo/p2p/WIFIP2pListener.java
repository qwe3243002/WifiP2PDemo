package com.nether.wifip2pdemo.p2p;

import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;

public interface WIFIP2pListener {

  void onCreateGroup(boolean success);

  void onRemoveGroup(boolean success);

  void onWifiP2pEnabled(boolean enabled);

  void onDiscovery(boolean isDiscovery);

  void onPeerListChanged(WifiP2pDeviceList peerList);

  void onP2pConnectionChanged(WifiP2pInfo wifiP2pInfo, NetworkInfo networkInfo);

}
