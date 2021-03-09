package network.message.lan.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import network.message.lan.R;
import network.message.lan.bean.RecordRoundBean;

/**
 * @author mac
 */
public class HistoryAdapter extends BaseQuickAdapter<RecordRoundBean.RecordBean, BaseViewHolder> {

    public HistoryAdapter(@Nullable List<RecordRoundBean.RecordBean> data) {
        super(R.layout.item_history, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, RecordRoundBean.RecordBean item) {

    }

}
