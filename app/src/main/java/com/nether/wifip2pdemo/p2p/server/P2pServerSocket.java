package com.nether.wifip2pdemo.p2p.server;

import android.util.Log;
import com.nether.wifip2pdemo.p2p.P2pThreadPool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class P2pServerSocket {

  private ServerSocket serverSocket;
  private Socket clientSocket;

  public P2pServerSocket(int port) {
    try {
      serverSocket = new ServerSocket(port);
      P2pThreadPool.work(new ConnectionRunnable());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private class ConnectionRunnable implements Runnable {

    @Override
    public void run() {
      try {
        clientSocket = serverSocket.accept();
        P2pThreadPool.work(new ReceiveRunnable(clientSocket));
        send("Hello from Server");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void send(String msg) {
    P2pThreadPool.work(new SendRunnable(clientSocket, msg));
  }

  private class SendRunnable implements Runnable {

    private Socket socket;
    private String msg;

    public SendRunnable(Socket socket, String msg) {
      this.socket = socket;
      this.msg = msg;
    }

    @Override
    public void run() {
      try {
        socket.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private class ReceiveRunnable implements Runnable {

    private Socket socket;

    public ReceiveRunnable(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        while ((count = inputStream.read(buffer)) != -1) {
          String msg = new String(buffer, 0, count, StandardCharsets.UTF_8);
          Log.d("P2pServerSocket", msg);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


}
