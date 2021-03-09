package network.message.lan.adapter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import network.message.lan.R;
import network.message.lan.bean.RecordRoundBean;

/**
 * @author mac
 */
public class RecordHistoryAdapter extends BaseQuickAdapter<RecordRoundBean, BaseViewHolder> {

    public RecordHistoryAdapter(@Nullable List<RecordRoundBean> data) {
        super(R.layout.item_record_history, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, RecordRoundBean item) {
        RecyclerView historyList = helper.getView(R.id.history_list);
        historyList.setLayoutManager(new GridLayoutManager(mContext, 2));
        HistoryAdapter historyAdapter = new HistoryAdapter(item.getRecordBeans());
        historyList.setAdapter(historyAdapter);
    }


}
