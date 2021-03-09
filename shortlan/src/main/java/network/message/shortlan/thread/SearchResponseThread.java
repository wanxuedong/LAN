package network.message.shortlan.thread;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import network.message.shortlan.constant.Port;

/**
 * @author mac
 * 用于响应客户端搜索响应的服务器线程
 * 核心是利用多点广播技术，不断接受其他设备数据并给出响应数据
 * 基于UDP协议，保证高效性，消息遗漏无所谓
 */
public class SearchResponseThread extends Thread {

    private static final String TAG = "SearchResponseThread";
    /**
     * 用于实现多点广播
     **/
    private MulticastSocket receive = null;
    /**
     * 向发出组播的客户端发送UDP数据
     **/
    private DatagramSocket sendSocket = null;
    /**
     * 接受组播的客户端的UDP数据
     **/
    private DatagramPacket receivePacket = null;
    /**
     * 向组播中发送的实际数据
     **/
    private DatagramPacket sendPacket = null;
    /**
     * 服务器的ip地址 + 服务器主昵称
     **/
    private byte[] serverIPAndName = null;
    private boolean isRunning = true;

    public SearchResponseThread() {
        try {
            //使用本机默认地址、指定端口来创建对象
            receive = new MulticastSocket(Port.PORT_MULTICAST_SOCKET);
            //将该MulticastSocket加入指定的多点广播地址,组播地址的范围在224.0.0.0--- 239.255.255.255之间
            receive.joinGroup(InetAddress.getByName(SearchServerThread.HOST_MULTICAST_LISTEN));
            sendSocket = new DatagramSocket(Port.PORT_SERVER_RESPOND);
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    /**
     * 每隔一秒检查响应同一局域网下其他设备(即客户端)发送的（必须得发送在上面指定的组播组中）特定udp数据（即客户端的ip地址），并及时响应返回数据（即服务器的名称+ip地址），
     * 以便客户端能够及时搜索到同一局域网下的其他设备（即服务器）
     **/
    @Override
    public void run() {
        int i = 0;
        byte[] receiveBytes = new byte[256];
        receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
        try {
            while (isRunning) {
                Log.d(TAG, "run: 等待客户端搜索");
                receive.receive(receivePacket);
                String s = new String(receiveBytes, 0, receivePacket.getLength());
                Log.d(TAG, "run: 收到客户端的ip：" + s);
                //向客户端回复服务器的ip地址，使用DatagramSocket，即基于UDP协议实现传输数据
                sendPacket = new DatagramPacket(serverIPAndName, serverIPAndName.length, InetAddress.getByName(s), Port.PORT_SERVER_RESPOND);
                if (sendSocket == null) {
                    Log.d(TAG, "run: sendSocket为空 ");
                }
                //发送两次
                for (int a = 0; a < 2; a++) {
                    sendSocket.send(sendPacket);
                }
                Log.d(TAG, "run: 收到第" + (i++) + "次请求");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sendSocket != null) {
                sendSocket.close();
            }
            if (receive != null) {
                receive.close();
            }
            Log.d(TAG, "run: searchResponseThread已结束！！");
        }
    }

    public void stopNow() {
        this.isRunning = false;
        try {
            receive.leaveGroup(InetAddress.getByName(SearchServerThread.HOST_MULTICAST_LISTEN));
        } catch (IOException e) {
            e.printStackTrace();
        }
        receive.close();
        sendSocket.close();
        interrupt();
    }

    public void setIpAndName(String ipAndName) {
        this.serverIPAndName = ipAndName.getBytes();
    }

}
