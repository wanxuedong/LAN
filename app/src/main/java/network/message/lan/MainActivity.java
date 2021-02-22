package network.message.lan;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import network.message.lan.service.ServerService;
import network.message.lan.utils.WiFiUtils;

/**
 * @author mac
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText roomName;
    private TextView searchRoom, createRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        roomName = findViewById(R.id.room_name);
        createRoom = findViewById(R.id.create_room);
        searchRoom = findViewById(R.id.search_room);
        createRoom.setOnClickListener(this);
        searchRoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_room:
                //创建房间
                if (TextUtils.isEmpty(roomName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "请先输入房间名",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (WiFiUtils.isWiFiConnected(this)) {
                    //启动服务器
                    ServerService.startServer(this, roomName.getText().toString());
                    //进入房间
                    ClientActivity.joinRoom(this, roomName.getText().toString() + " 的房间", WiFiUtils.getIPAddress(this), roomName.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "未加入局域网",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.search_room:
                //搜索房间
                if (WiFiUtils.isWiFiConnected(this)) {
                    RoomsActivity.intoRoom(this, roomName.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "未加入局域网",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

}