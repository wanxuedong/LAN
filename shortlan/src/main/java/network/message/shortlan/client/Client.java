package network.message.shortlan.client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import network.message.shortlan.constant.Port;
import network.message.shortlan.utils.threadxutil.AbstractLife;
import network.message.shortlan.utils.threadxutil.ThreadX;

import static network.message.shortlan.constant.Identification.HEART_CHECK_CODE;
import static network.message.shortlan.constant.Identification.JOIN_END;
import static network.message.shortlan.constant.Identification.JOIN_START;
import static network.message.shortlan.constant.Identification.MESSAGE_END;

/**
 * @author mac
 * Socket客户端  2021/3/8
 * 这里用于局域网通信，也可以用于广域网通信
 */
public class Client {
    private static final String TAG = "Client";
    /**
     * 收到消息
     **/
    public static final int WHAT_RECEIVE = 6;
    /**
     * 加入房间成功
     **/
    public static final int WHAT_CLIENT_CREATE = 5;
    /**
     * 加入房间失败
     **/
    public static final int WHAT_CLIENT_NOT_CREATE = 4;
    /**
     * 与房间连接断开
     **/
    public static final int WHAT_SOCKET_CLOSE = 3;
    /**
     * 消息发送成功
     **/
    public static final int WHAT_SEND_SUCCESS = 2;
    /**
     * 消息发送失败
     **/
    public static final int WHAT_SEND_FAIL = 1;
    private Socket clientSocket = null;
    private DataInputStream dis = null;
    private BufferedWriter bw = null;

    private DataOutputStream dos = null;
    private ReceiveMsg receiveMsg = null;
    private boolean isRunning = false;
    private boolean isConnecting = true;
    /**
     * 检查心跳状态定时器
     **/
    private ScheduledExecutorService executorService;
    /**
     * 维持心跳线程,每隔几秒发送心跳信息
     **/
    private Runnable heart = null;
    /**
     * 服务器ip地址
     **/
    private String serverIp = "";
    /**
     * 服务器名称，非必要参数
     **/
    private String userName = "";
    /**
     * 连接服务器线程，读写数据
     **/
    private ConnectThread connect = null;
    /**
     * 消息分发集合
     **/
    private List<SoftReference<Handler>> mHandlers = new ArrayList<>();

    private static Client instance;

    private Client() {

    }

    /**
     * 单例处理，方便全局使用
     **/
    public static Client getInstance() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    /**
     * 设置服务器ip地址
     **/
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    /**
     * 设置用户名
     **/
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 连接服务器
     **/
    public void connect() {
        if (serverIp == null) {
            Log.d(TAG, "请先设置连接设备的ip");
            return;
        }
        if (userName == null) {
            Log.d(TAG, "请先设置连接设备的用户名");
            return;
        }
        connect = new ConnectThread(serverIp, userName);
        connect.start();  //开始连接服务器
    }

    /**
     * 添加客户端消息监听
     **/
    public void setOnListener(Handler handler) {
        mHandlers.add(new SoftReference<Handler>(handler));
    }

    /**
     * 客户端连接线程
     * 创建客户端对象时运行一次，连接失败则 创建Client失败
     */
    class ConnectThread extends Thread {
        private String ip;
        private String name;

        public ConnectThread(String ip, String userName) {
            this.ip = ip;
            this.name = userName;
        }

        @Override
        public void run() {
            try {
                isRunning = true;
                clientSocket = new Socket(ip, Port.PORT_SERVER_SOCKET);
                dis = new DataInputStream(clientSocket.getInputStream());
                bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                dos = new DataOutputStream(clientSocket.getOutputStream());

                receiveMsg = new ReceiveMsg();
                receiveMsg.start();
                heart = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (isRunning) {
                                sendHeartMsg(HEART_CHECK_CODE);
                                sleep(2500);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                ThreadX.x().run(new AbstractLife(heart));

                //成功连接后向服务器发送自己的昵称，服务器将提示“ 某某某 加入了聊天室”
                sendMsg(JOIN_START + name + JOIN_END);
                dispatchMessage(WHAT_CLIENT_CREATE, null);
                executorService = Executors.newScheduledThreadPool(1);
                executorService.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        if (isRunning) {
                            if (isConnecting) {
                                isConnecting = false;
                            } else {
                                stopNow();
                            }
                        }
                    }
                }, 0, 4000, TimeUnit.SECONDS);
                Log.d(TAG, "client已连接" + name);
            } catch (Exception e) {
                //连接过程中出现异常说明默认连接失败
                e.printStackTrace();
                Log.d(TAG, "run: 连接异常，客户端创建失败");
                //停止发送与接收线程，释放资源
                stopNow();
                //通知activity
                dispatchMessage(WHAT_CLIENT_NOT_CREATE, null);
            }
        }
    }

    /**
     * 释放Client资源方法，在client创建失败或关闭client时调用
     */
    private void releaseRec() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
            executorService.shutdownNow();
            mHandlers.clear();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ReleaseRec: 资源释放异常");
        }
    }

    /**
     * 向服务器发送数据方法
     *
     * @param data
     */
    public void sendMsg(String data) {
        new Thread(new SendMsg(data + "\n" + MESSAGE_END + "\n", false)).start();
    }

    public void sendHeartMsg(String data) {
        new Thread(new SendMsg(data + "\n" + MESSAGE_END + "\n", true)).start();
    }

    /**
     * 发送数据线程，每次点击发送创建一个新线程
     */
    class SendMsg implements Runnable {
        private String message;
        private boolean isHeart;

        public SendMsg(String message, boolean isHeart) {
            this.message = message;
            this.isHeart = isHeart;
        }

        @Override
        public void run() {
            if (isRunning) {
                if (!clientSocket.isClosed() && clientSocket.isConnected()) {
                    try {
                        Log.d(TAG, "run: 客户端要发送的数据 = " + message);
                        bw.write(message);
                        bw.flush();
                        if (!isHeart) {
                            dispatchMessage(WHAT_SEND_SUCCESS, null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "run: 发送数据异常");
                        if (!isHeart) {
                            dispatchMessage(WHAT_SEND_FAIL, null);
                        }
                    }
                } else {
                    if (!isHeart) {
                        dispatchMessage(WHAT_SEND_FAIL, null);
                    }
                }
            } else {
                Log.d(TAG, "run: client.isRunning = " + isRunning + ",已无法发送");
            }

        }
    }

    class ReceiveMsg extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                if (!clientSocket.isClosed() && clientSocket.isConnected()) {
                    Log.d(TAG, "receviceMsg：准备从服务器获取数据");
                    try {
                        String msg = dis.readUTF();
                        Log.d(TAG, "run: msg = " + msg);
                        if (msg.equals(HEART_CHECK_CODE)) {
                            Log.d(TAG, "run: " + userName + "收到心跳回应");
                            isConnecting = true;
                        } else if (msg != null && !msg.equals("")) {
                            Log.d(TAG, "run: 获取到服务器发回的数据:" + msg);
                            String[] s = msg.split("/", 2);
                            Message message = new Message();
                            message.what = WHAT_RECEIVE;
                            Bundle bundle = new Bundle();
                            bundle.putString("msgSender", s[0]);
                            if (s.length > 1) {
                                bundle.putString("recevieMsg", s[1]);
                            }
                            message.setData(bundle);
                            dispatchMessage(-1, message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "receviceMsg：服务器获取数据异常 ");
                        dispatchMessage(WHAT_SOCKET_CLOSE, null);
                        stopNow();
                    }
                }
            }
            Log.d(TAG, "client接收线程结束");
        }
    }

    /**
     * 分发消息
     **/
    private void dispatchMessage(int messageType, Message message) {
        if (mHandlers.size() == 0) {
            return;
        }
        Iterator<SoftReference<Handler>> iterator = mHandlers.iterator();
        while (iterator.hasNext()) {
            Handler handler = iterator.next().get();
            if (handler == null) {
                iterator.remove();
                return;
            }
            if (message == null) {
                handler.sendEmptyMessage(messageType);
            } else {
                try {
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopNow() {
        Log.d(TAG, "stopNow: client" + userName);
        isRunning = false;
        releaseRec();
    }

}
