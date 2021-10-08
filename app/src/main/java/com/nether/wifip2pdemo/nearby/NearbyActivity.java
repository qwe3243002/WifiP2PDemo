package com.nether.wifip2pdemo.nearby;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.nether.wifip2pdemo.R;
import com.nether.wifip2pdemo.databinding.ActivityNearbyBinding;
import java.nio.charset.StandardCharsets;

public class NearbyActivity extends AppCompatActivity {

  private ActivityNearbyBinding binding;
  private static final String TAG = "NearbyActivity";
  private ConnectionsClient connectionsClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityNearbyBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    connectionsClient = Nearby.getConnectionsClient(this);
    binding.btnHost.setOnClickListener(view -> {
      startHost();
    });
    binding.btnClient.setOnClickListener(view -> {
      startDiscovery();
    });
  }

  private void startHost() {
    AdvertisingOptions options = new AdvertisingOptions.Builder()
        .setStrategy(Strategy.P2P_POINT_TO_POINT).build();
    connectionsClient
        .startAdvertising("HOST", "HostServer", new ConnectionLifecycleCallback() {
          @Override
          public void onConnectionInitiated(@NonNull String endpointId,
              @NonNull ConnectionInfo connectionInfo) {
            String res = String.format(
                "server onConnectionInitiated --> endpointId:%s,EndpointName:%s,AuthenticationToken:%s",
                endpointId, connectionInfo.getEndpointName(),
                connectionInfo.getAuthenticationToken());
            Log.d(TAG, res);
            connectionsClient.acceptConnection(endpointId, new PayloadCallback() {
              @Override
              public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                String msg = new String(payload.asBytes(), StandardCharsets.UTF_8);
                binding.tvContent.setText(msg);
              }

              @Override
              public void onPayloadTransferUpdate(@NonNull String endpointId,
                  @NonNull PayloadTransferUpdate payloadTransferUpdate) {

              }
            });
          }

          @Override
          public void onConnectionResult(@NonNull String endpointId,
              @NonNull ConnectionResolution result) {
            String res = "";
            switch (result.getStatus().getStatusCode()) {
              case ConnectionsStatusCodes.STATUS_OK:
                // We're connected! Can now start sending and receiving data.
                res = "STATUS_OK";
                Payload payload = Payload.fromBytes("Hello from Server".getBytes(
                    StandardCharsets.UTF_8));
                connectionsClient.sendPayload(endpointId, payload);
                break;
              case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                // The connection was rejected by one or both sides.
                res = "STATUS_CONNECTION_REJECTED";

                break;
              case ConnectionsStatusCodes.STATUS_ERROR:
                // The connection broke before it was able to be accepted.
                res = "STATUS_ERROR";

                break;
              default:
                // Unknown status code
                res = "Unknown";

            }
            Log.d(TAG, String.format("Server onConnectionResult --> res"));
          }

          @Override
          public void onDisconnected(@NonNull String s) {

          }
        }, options);
  }

  private void startDiscovery() {
    DiscoveryOptions options = new DiscoveryOptions.Builder()
        .setStrategy(Strategy.P2P_POINT_TO_POINT).build();
    connectionsClient
        .startDiscovery("HostServer", new EndpointDiscoveryCallback() {
          @Override
          public void onEndpointFound(@NonNull String endpointId,
              @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d(TAG, String
                .format("endpointId --> %s,discoveredEndpointInfo --> endpointName:%s,serviceId:%s",
                    endpointId,
                    discoveredEndpointInfo.getEndpointName(),
                    discoveredEndpointInfo.getServiceId()));

            connectionsClient.requestConnection(discoveredEndpointInfo.getEndpointName(),
                endpointId, new ConnectionLifecycleCallback() {
                  @Override
                  public void onConnectionInitiated(@NonNull String endpointId,
                      @NonNull ConnectionInfo connectionInfo) {
                    String res = String.format(
                        "client onConnectionInitiated --> endpointId:%s,EndpointName:%s,AuthenticationToken:%s",
                        endpointId, connectionInfo.getEndpointName(),
                        connectionInfo.getAuthenticationToken());
                    Log.d(TAG, res);
                    connectionsClient.acceptConnection(endpointId, new PayloadCallback() {
                      @Override
                      public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                        String msg = new String(payload.asBytes(), StandardCharsets.UTF_8);
                        binding.tvContent.setText(msg);
                      }

                      @Override
                      public void onPayloadTransferUpdate(@NonNull String s,
                          @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                      }
                    });
                  }

                  @Override
                  public void onConnectionResult(@NonNull String endpointId,
                      @NonNull ConnectionResolution result) {
                    String res = "";
                    switch (result.getStatus().getStatusCode()) {
                      case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        res = "STATUS_OK";
                        Payload payload = Payload.fromBytes("Hello from client".getBytes(
                            StandardCharsets.UTF_8));
                        connectionsClient.sendPayload(endpointId, payload);
                        break;
                      case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        res = "STATUS_CONNECTION_REJECTED";

                        break;
                      case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        res = "STATUS_ERROR";

                        break;
                      default:
                        // Unknown status code
                        res = "Unknown";

                    }
                    Log.d(TAG, String.format("Client onConnectionResult --> %s",res));
                  }

                  @Override
                  public void onDisconnected(@NonNull String s) {

                  }
                });
          }

          @Override
          public void onEndpointLost(@NonNull String s) {

          }
        }, options);
  }
}