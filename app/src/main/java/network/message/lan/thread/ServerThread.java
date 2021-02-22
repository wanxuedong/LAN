package network.message.lan.thread;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import network.message.lan.constant.Identification;
import network.message.lan.constant.Port;

import static network.message.lan.constant.Identification.HEART_CHECK_CODE;
import static network.message.lan.constant.Identification.MESSAGE_END;

/**
 * @author mac
 * 用户保持服务器和多个客户端的会话数据和断连状态
 * 核心是定时器心跳检查和TCP协议端口数据监听
 * 基于TCP传输数据，保证数据交互的可靠性
 */
public class ServerThread extends Thread {

    private static final String TAG = "ServerThread";

    /**
     * 基于TCP协议，保证消息的可靠性
     **/
    private ServerSocket serverSocket = null;
    private List<ClientChannel> clientChannels = new ArrayList<ClientChannel>();
    private Timer heartCheck = null;
    private boolean isRunning = true;
    /**
     * 通过心跳检查房间人员离开状态间隔时间
     **/
    private final long PERIOD = 4 * 1000L;
    /**
     * 读取消息编码格式
     **/
    private final String CHARSET_NAME = "UTF-8";

    public ServerThread() {
    }

    /**
     * 做俩件事
     * 一：保证已关联的服务区和客户端的数据交互
     * 二：监听新的客户端连接
     **/
    @Override
    public void run() {
        try {
            heartCheck = new Timer();
            heartCheck.schedule(new TimerTask() {
                @Override
                public void run() {
                    //定时处理服务器和客户端的数据交互
                    if (isRunning) {
                        Iterator<ClientChannel> list = clientChannels.iterator();
                        while (list.hasNext()) {
                            ClientChannel c = list.next();
                            if (c.isConnecting == true) {
                                //通过心跳作为服务器和客户端是否保持连接的依据，因为只有心跳才会把isConnecting重置为true
                                // TODO: 2021/2/23 不灵活，可优化 ,比如断开重连
                                c.isConnecting = false;
                            } else {
                                Log.d(TAG, "run: c.name = " + c.name);
                                //离开了房间
                                list.remove();
                                c.forcedStop();
                            }
                        }
                        Log.d(TAG, "run: 还连接的客户端个数：" + clientChannels.size());
                    }
                }
            }, 0, PERIOD);

            //不断检测是否有新的客户端连接
            serverSocket = new ServerSocket(Port.PORT_SERVER_SOCKET);
            while (isRunning) {
                Log.d(TAG, "run: 等待客户端连接");
                ClientChannel clientChannel = new ClientChannel(serverSocket.accept());
                Log.d(TAG, "run: 一个客户端连接上了");
                clientChannels.add(clientChannel);
                //每连接上一个设备就会开启一个线程维护
                // TODO: 2021/2/22 过于消耗资源，可优化 ，比如使用线程池
                new Thread(clientChannel).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            Log.d(TAG, "ServerThread已结束");
        }

    }

    /**
     * 停止服务器
     **/
    public void stopNow() {
        try {
            isRunning = false;
            heartCheck.cancel();
            serverSocket.close();
            for (ClientChannel clientChannel : clientChannels) {
                clientChannel.forcedStop();
            }
            clientChannels.clear();
            interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于处理来自客户端的会话消息
     * 三种消息类型
     * 一：心跳消息
     * 二：加入房间消息
     * 三：正常会话消息
     **/
    class ClientChannel implements Runnable {

        /**
         * 读取客户端消息
         **/
        private BufferedReader br = null;
        /**
         * 给客户端发送消息
         **/
        private DataOutputStream dos = null;
        /**
         * 服务器和客户端的会话，基于此实现数据通信
         **/
        private Socket socket = null;
        private boolean isRunning = true;
        private boolean isConnecting = true;
        private String name = "";

        public ClientChannel(Socket socket) {
            try {
                this.socket = socket;
                Log.d(TAG, "ClientChannel: socket = " + socket.hashCode());
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), CHARSET_NAME));
                dos = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "ClientChannel: 客户端已连接");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    br.close();
                    dos.close();
                    isRunning = false;
                } catch (IOException e1) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            Log.d(TAG, "run: 客户端线程开始");
            while (isRunning) {
                String s = receive();
                Log.d(TAG, "run: 接收数据:" + s);
                if (s.equals(HEART_CHECK_CODE)) {
                    //接收到心跳消息并返回心跳消息
                    send(HEART_CHECK_CODE);
                    isConnecting = true;
                } else if (s != null && !s.equals("")) {
                    if (name == "") {
                        //加入房间的消息
                        if (s.startsWith(Identification.JOIN_START) && s.endsWith(Identification.JOIN_END)) {
                            name = s.replaceFirst(Identification.JOIN_START, "");
                            name = name.replaceFirst(Identification.JOIN_END, "");
                            dispatchTip("加入了房间");
                        }
                    } else {
                        //正常交互消息
                        Log.d(TAG, "收到客户端的的消息" + s);
                        dispatchData(s);
                    }
                }
            }
            Log.d(TAG, "run: " + name + "在客户端的线程已结束");
            Log.d(TAG, "run: clientManager剩余线程：" + clientChannels.size());
        }

        /**
         * 读取客户端消息或会话断开处理
         **/
        public String receive() {
            StringBuffer msg = new StringBuffer();
            try {
                //读取每一行的数据.注意大部分端口操作都需要交互数据。
                String str;
                boolean isFirst = true;
                Log.d(TAG, "现在: " + name + "开始读数据：a");
                while ((str = br.readLine()) != null) {
                    if (str.equals(MESSAGE_END)) {
                        //本次消息读取结束
                        break;
                    } else if (!isFirst) {
                        msg.append("\n");
                    } else {
                        isFirst = false;
                    }
                    msg.append(str);
                }
                Log.d(TAG, "现在: " + name + "数据读完：a");
                if ("".equals(msg.toString())) {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dispatchTip("离开了房间");
                clientChannels.remove(this);
                isRunning = false;
                Log.d(TAG, "run: " + name + "离开房间");
            }
            return msg.toString();
        }

        /**
         * 分发通用消息到每一个连接上的客户端
         **/
        private void dispatchData(String data) {
            data = name + " :/ " + data;
            for (ClientChannel channel : clientChannels) {
                channel.send(data);
            }
        }

        /**
         * 分发通知消息到每一个连接上的客户端
         **/
        private void dispatchTip(String data) {
            data = "*" + name + "*  / " + data;
            for (ClientChannel channel : clientChannels) {
                channel.send(data);
            }
        }

        /**
         * 给客户端发送消息
         **/
        public void send(String data) {
            if (data == null || data.equals("")) {
                return;
            }
            try {
                Log.d(TAG, "send: 向客户端返回数据");
                dos.writeUTF(data);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 关闭和客户端的连接并释放资源
         **/
        private void forcedStop() {
            isRunning = false;
            try {
                if (socket != null) {
                    socket.close();
                }
                if (br != null) {
                    br.close();
                }
                if (dos != null) {
                    dos.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "ClientChannel: 资源释放异常");
            }
        }
    }

}
