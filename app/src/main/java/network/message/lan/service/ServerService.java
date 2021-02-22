package network.message.lan.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import network.message.lan.thread.SearchResponseThread;
import network.message.lan.thread.ServerThread;
import network.message.lan.utils.WiFiUtils;

/**
 * @author mac
 * 创建房间服务，开启后可以在局域网下搜索到，关闭则搜索不到
 */

public class ServerService extends Service {
    private static final String TAG = "ServerService";
    public static final String CLASSNAME = ServerService.class.getName();
    /**
     * 用于客户端搜索响应的线程
     **/
    private SearchResponseThread searchResponseTh = null;
    private ServerThread serverThread = null;
    /**
     * 服务器线程运行标志
     **/
    private boolean isRunning;
    private String serverName = "";
    private String serverIp = "";


    /**
     * 创建房间服务
     **/
    public static void startServer(Context context, String roomName) {
        Intent intent = new Intent(context, ServerService.class);
        intent.putExtra("userName", roomName);
        intent.putExtra("serverIp", WiFiUtils.getIPAddress(context));
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        isRunning = false;
        searchResponseTh = new SearchResponseThread();
        serverThread = new ServerThread();
        serverThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Log.d(TAG, "onStartCommand: " + this.hashCode());
            isRunning = true;
            if (intent != null) {
                serverIp = intent.getStringExtra("serverIp");
                serverName = intent.getStringExtra("userName");
            }
            searchResponseTh.setIpAndName(serverIp + "/" + serverName);
            if (searchResponseTh != null && !searchResponseTh.isAlive()) {
                searchResponseTh.start();
            }
            Toast.makeText(getApplicationContext(), "服务器已创建，将在局域网中可见", Toast.LENGTH_SHORT);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        searchResponseTh.stopNow();
        serverThread.stopNow();
        Log.d(TAG, "onDestroy: 销毁ServerService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
