package network.message.shortlan.thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

import network.message.shortlan.constant.Port;


/**
 * @author mac
 * 用于搜索所在局域网下给予响应的服务器设备，通过服务器返回的数据中获取到名称和IP
 * 核心为使用点播广播发送消息，并基于UDP协议监听回调获取服务器数据
 */
public class SearchServerThread extends Thread {
    private static final String TAG = "SearchServerThread";
    private MulticastSocket mSocket = null;
    private DatagramSocket udpSocket = null;
    private DatagramPacket searchPacket = null;
    private DatagramPacket receivePacket = null;
    private Handler mHandler = null;
    /**
     * 搜索服务器结束
     **/
    public static final int WHAT_END = 1;
    /**
     * 搜索超时或强制结束
     **/
    public static final int WHAT_EXCEPT = 2;
    /**
     * 收到服务器响应
     **/
    public static final int WHAT_RECEIVE = 3;
    /**
     * 最大尝试搜索服务器次数
     **/
    private final int MAX_TRY = 5;
    private byte[] ipByte;
    /**
     * 多点广播地址
     * 224.0.0.0--- 239.255.255.255
     **/
    public final static String HOST_MULTICAST_LISTEN = "233.233.233.233";
    /**
     * 最多支持展示的服务器数量
     **/
    private final int MAX_COUNT = 200;

    public SearchServerThread(String ip, Handler handler) {
        ipByte = ip.getBytes();
        mHandler = handler;
    }

    @Override
    public void run() {
        List<String> receiveIps = new ArrayList<>();
        try {
            searchPacket = new DatagramPacket(ipByte, ipByte.length,
                    InetAddress.getByName(HOST_MULTICAST_LISTEN), Port.PORT_MULTICAST_SOCKET);
            mSocket = new MulticastSocket();
            // 设置该ttl参数设置数据报最多可以跨过多少个网络
            // 当ttl为0时，指定数据报应停留在本地主机；
            // 当ttl的值为1时，指定数据报发送到本地局域网；
            // 当ttl的值为32时，意味着只能发送到本站点的网络上；当ttl为64时，意味着数据报应保留在本地区；
            // 当ttl的值为128时，意味着数据报应保留在本大洲；当ttl为255时，意味着数据报可发送到所有地方；
            // 默认情况下，该ttl的值为1
            mSocket.setTimeToLive(1);
            udpSocket = new DatagramSocket(Port.PORT_SERVER_RESPOND);
            udpSocket.setSoTimeout(3000);
            byte[] buf = new byte[512];
            receivePacket = new DatagramPacket(buf, 0, buf.length);
            int count = 0;
            while (receiveIps.size() < MAX_COUNT) {
                if (count <= MAX_TRY) {
                    count++;
                    mSocket.send(searchPacket);
                }
                Log.d(TAG, "run: 开始接受等待接受服务器相应");
                udpSocket.receive(receivePacket);
                Log.d(TAG, "run: 接收到服务器响应");
                byte[] data = receivePacket.getData();
                String[] s = new String(data, 0, receivePacket.getLength()).split("/", 2);
                //如果ip没有获取过
                if (!isReceived(receiveIps, s[0])) {
                    receiveIps.add(s[0]);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", s[0]);
                    bundle.putString("name", s[1]);
                    msg.setData(bundle);
                    msg.what = WHAT_RECEIVE;
                    mHandler.sendMessage(msg);
                }
            }
        } catch (IOException e) {
            mHandler.sendEmptyMessage(WHAT_EXCEPT);
        } finally {
            mSocket.close();
            udpSocket.close();
            mHandler.sendEmptyMessage(WHAT_END);
        }
    }

    public void stopNow() {
        if (mSocket != null) {
            mSocket.close();
        }
        if (udpSocket != null) {
            udpSocket.close();
        }
    }

    /**
     * 判断该ip地址是否已经获取过了
     *
     * @param list 存放获取过的所有ip
     * @param ip   要判断的ip
     * @return 是否获取过
     */
    private boolean isReceived(List list, String ip) {
        boolean isExist = false;
        for (int a = 0; a < list.size(); a++) {
            if (list.get(a).equals(ip)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }
}
