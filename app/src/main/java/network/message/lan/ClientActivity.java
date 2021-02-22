package network.message.lan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import network.message.lan.client.Client;
import network.message.lan.service.ServerService;
import network.message.lan.utils.ServiceUtil;

/**
 * @author mac
 * 客户端示范类
 * 实现了客户端和服务器数据交互的作用
 */
public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private Client client = null;
    private String roomName = "";
    private String roomIp = "";
    private String userName = "";
    private Handler mHandler = null;
    private boolean isBackDown = false;

    public static void joinRoom(Context context, String roomName, String roomIp, String userName) {
        Intent intent = new Intent(context, ClientActivity.class);
        intent.putExtra("roomName", roomName);
        intent.putExtra("roomIp", roomIp);
        intent.putExtra("userName", userName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Client.WHAT_RECEIVE:
                        //收到服务器的消息
                        Log.d(TAG, msg.getData().get("msgSender") + "  " + msg.getData().get("recevieMsg"));
                        break;
                    case Client.WHAT_CLIENT_CREATE:
                        Toast.makeText(getApplicationContext(), "成功加入房间", Toast.LENGTH_SHORT).show();
                        break;
                    case Client.WHAT_CLIENT_NOT_CREATE:
                        //client连接失败时，client置空，提示Toast，退出房间
                        client = null;
                        Log.d(TAG, "onDie: 服务器异常，连接失败，无法进入房间");
                        Toast.makeText(getApplicationContext(), "该房间已被关闭或者您已不在该局域网中", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case Client.WHAT_SEND_SUCCESS:
                        Log.d(TAG, "run: 发送数据数据成功");
                        break;
                    case Client.WHAT_SEND_FAIL:
                        Log.d(TAG, "run: 客户端连接断开，数据发送失败");
                        break;
                    case Client.WHAT_SOCKET_CLOSE:
                        if (!ServiceUtil.isServiceRunning(getApplicationContext(), ServerService.CLASSNAME) && !isBackDown) {
                            Toast.makeText(getApplicationContext(), "房间连接已断开", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "\n*系统提示*\n\n" +
                                    "\u3000\u3000* 房间连接已断开，已无法接受与发送消息。\n\n" +
                                    "\u3000\u3000* 可能的原因 ： 房间服务器已关闭、您已不在该房间局域网内、您的网络连接已断开、其他未知的原因。\n\n" +
                                    "\u3000\u3000* 房间已不可用，请退出该房间。\n\n" +
                                    "*系统提示*\n");
                            client.stopNow();
                        } else if (ServiceUtil.isServiceRunning(getApplicationContext(), ServerService.CLASSNAME)) {
                            Toast.makeText(getApplicationContext(), "您的房间连接已断开", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "\n*系统提示*\n\n" +
                                    "\u3000\u3000* 您创建的房间已断开，已无法接受与发送消息。\n\n" +
                                    "\u3000\u3000* 可能的原因 ： 房间服务器已关闭、您已不在该房间局域网内、您的网络连接已断开、其他未知的原因。\n\n" +
                                    "\u3000\u3000* 房间已不可用，请退出该房间。\n\n" +
                                    "*系统提示*\n");
                            Intent intent = new Intent(ClientActivity.this, ServerService.class);
                            client.stopNow();
                            stopService(intent);
                        }
                        break;
                    default:
                }
            }
        };
        Intent intent = getIntent();
        if (intent != null) {
            roomName = intent.getStringExtra("roomName");
            roomIp = intent.getStringExtra("roomIp");
            userName = intent.getStringExtra("userName");
            Log.d(TAG, "onCreate: " + roomIp + "," + roomName + "," + userName);
        }
        client = new Client(roomIp, userName, mHandler);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            onChatBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出房间提示
     **/
    public void onChatBack() {
        if (ServiceUtil.isServiceRunning(this, ServerService.CLASSNAME)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle("提示").setMessage("你创建的房间正在运行，退出将会关闭房间服务器并结束聊天。是否继续退出？");

            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    Intent intent = new Intent(ClientActivity.this, ServerService.class);
                    client.stopNow();
                    stopService(intent);
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setTitle("提示").setMessage("你确定要退出房间吗？");

            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    isBackDown = true;
                    client.stopNow();
                    finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            Log.d(TAG, "onDestroy: client已销毁");
            client.stopNow();
        }
    }

}