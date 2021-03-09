package network.message.shortlan.thread;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import network.message.shortlan.constant.Port;

import static network.message.shortlan.constant.Identification.HEART_CHECK_CODE;
import static network.message.shortlan.constant.Identification.MESSAGE_END;

/**
 * @author mac
 * ServerSocket服务端  2021/3/8
 */
public class ServerThread extends Thread {
    private static final String TAG = "ServerThread";
    private ServerSocket serverSocket = null;
    /**
     * 维持和每一个客户端连接状态和读写操作的集合
     **/
    private List<ClientChannel> clientChannels = new ArrayList<ClientChannel>();
    private ScheduledExecutorService executorService;
    private boolean isRunning = true;

    public ServerThread() {
    }

    @Override
    public void run() {
        try {
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        Iterator<ClientChannel> list = clientChannels.iterator();
                        while (list.hasNext()) {
                            ClientChannel c = list.next();
                            if (c.isConnecting == true) {
                                c.isConnecting = false;
                            } else {
                                Log.d(TAG, "run: c.name = " + c.name);
                                list.remove();
                                c.forcedStop();
                            }
                        }
                        Log.d(TAG, "run: 还连接的客户端个数：" + clientChannels.size());
                    }
                }
            }, 0, 4000, TimeUnit.SECONDS);
            serverSocket = new ServerSocket(Port.PORT_SERVER_SOCKET);
            while (isRunning) {
                Log.d(TAG, "run: 等待客户端连接");
                ClientChannel clientChannel = new ClientChannel(serverSocket.accept());
                Log.d(TAG, "run: 一个客户端连接上了");
                clientChannels.add(clientChannel);
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
     * 停止服务器，和所有客户端的连接都会被强制断开
     **/
    public void stopNow() {
        try {
            isRunning = false;
            executorService.shutdownNow();
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
     * 每加入一个客户端就建立一个连接
     **/
    class ClientChannel implements Runnable {
        private BufferedReader br = null;
        private DataOutputStream dos = null;
        private boolean isRunning = true;
        private boolean isConnecting = true;
        private Socket socket = null;
        private String name = "";

        public ClientChannel(Socket socket) {
            try {
                this.socket = socket;
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                dos = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "ClientChannel: 客户端已连接");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    br.close();
                    dos.close();
                    isRunning = false;
                } catch (IOException e1) {

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
                    realSend(HEART_CHECK_CODE);
                    isConnecting = true;
                } else if (s != null && !s.equals("")) {
                    if (name == "") {
                        if (s.startsWith("lzg.chatRoom") && s.endsWith("First.in")) {
                            name = s.replaceFirst("lzg.chatRoom", "");
                            name = name.replaceFirst("First.in", "");
                            dispatchTip("加入了聊天室");
                        }
                    } else {
                        Log.d(TAG, "收到客户端的的消息" + s);
                        dispatchData(s);
                    }
                }
            }
            Log.d(TAG, "run: " + name + "在客户端的线程已结束");
            Log.d(TAG, "run: clientManager剩余线程：" + clientChannels.size());
        }

        public String receive() {
            StringBuffer msg = new StringBuffer();
            try {
                // 读取每一行的数据.注意大部分端口操作都需要交互数据。
                String str;
                boolean isFirst = true;
                while ((str = br.readLine()) != null) {
                    if (str.equals(MESSAGE_END)) {
                        break;
                    } else if (!isFirst) {
                        msg.append("\n");
                    } else {
                        isFirst = false;
                    }
                    msg.append(str);
                }
                if (msg.toString().equals("")) {
                    throw new Exception();
                }
            } catch (Exception e) {
                e.printStackTrace();
                dispatchTip("离开了聊天室");
                clientChannels.remove(this);
                isRunning = false;
                Log.d(TAG, "run: " + name + "离开房间");
            }
            return msg.toString();
        }

        /**
         * 分发用户聊天信息到所有群聊用户
         **/
        private void dispatchData(String data) {
            data = name + " :/ " + data;
            for (ClientChannel channel : clientChannels) {
                channel.realSend(data);
            }
        }

        /**
         * 分发用户进出状态消息到所有群聊用户
         **/
        private void dispatchTip(String data) {
            data = "*" + name + "*  / " + data;
            for (ClientChannel channel : clientChannels) {
                channel.realSend(data);
            }
        }

        /**
         * 实际发送数据给客户端方法
         **/
        public void realSend(String data) {
            if (data == null || data.equals("")) {
                return;
            }
            try {
                Log.d(TAG, "send: 向客户端返回数据");
                dos.writeUTF(data);
                dos.flush();
            } catch (IOException e) {
            }
        }

        /**
         * 释放资源，断开连接
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
