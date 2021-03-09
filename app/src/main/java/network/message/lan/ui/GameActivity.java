package network.message.lan.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;

import java.util.ArrayList;
import java.util.List;

import network.message.lan.R;
import network.message.lan.adapter.ChoseAdapter;
import network.message.lan.adapter.RecordHistoryAdapter;
import network.message.lan.bean.ChoseBean;
import network.message.lan.bean.RecordRoundBean;

/**
 * @author mac
 */
public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recordHistory;
    private RecyclerView currentChose;
    private ImageView gameBack;
    private TextView gameSetting;
    private TextView gameTimes;
    private TextView gameNumber;
    private RecordHistoryAdapter recordHistoryAdapter;
    private List<RecordRoundBean> recordsList = new ArrayList<>();
    private ChoseAdapter choseAdapter;
    private List<ChoseBean> choseList = new ArrayList<>();

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, GameActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        recordHistory = findViewById(R.id.record_history);
        currentChose = findViewById(R.id.current_chose);
        gameBack = findViewById(R.id.game_back);
        gameSetting = findViewById(R.id.game_setting);
        gameTimes = findViewById(R.id.game_times);
        gameNumber = findViewById(R.id.game_number);
    }

    private void initData() {
        recordHistory.setLayoutManager(new LinearLayoutManager(this));
        for (int i = 0; i < 10; i++) {
            RecordRoundBean bean = new RecordRoundBean();
            List<RecordRoundBean.RecordBean> recordBeans = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                recordBeans.add(new RecordRoundBean.RecordBean());
            }
            bean.setRecordBeans(recordBeans);
            recordsList.add(bean);
        }
        recordHistoryAdapter = new RecordHistoryAdapter(recordsList);
        recordHistory.setAdapter(recordHistoryAdapter);

        currentChose.setLayoutManager(new GridLayoutManager(this, 2));
        for (int i = 0; i < 6; i++) {
            ChoseBean choseBean = new ChoseBean();
            if (i == 0) {
                choseBean.setChose(true);
            }
            choseBean.setNumber(i + 1 + "");
            choseList.add(choseBean);
        }
        choseAdapter = new ChoseAdapter(choseList);
        currentChose.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.chose_status) {
                    for (int i = 0; i < choseList.size(); i++) {
                        if (i == position) {
                            choseList.get(position).setChose(true);
                        } else {
                            choseList.get(i).setChose(false);
                        }
                    }
                    choseAdapter.notifyDataSetChanged();
                }
            }
        });
        currentChose.setAdapter(choseAdapter);
    }

    private void initEvent() {
        gameBack.setOnClickListener(this);
        gameSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_back:
                //返回
                finish();
                break;
            case R.id.game_setting:
                //游戏设置
                break;
            default:
        }
    }
}