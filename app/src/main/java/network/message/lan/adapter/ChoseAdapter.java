package network.message.lan.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import network.message.lan.R;
import network.message.lan.bean.ChoseBean;

/**
 * @author mac
 */
public class ChoseAdapter extends BaseQuickAdapter<ChoseBean, BaseViewHolder> {

    public ChoseAdapter(@Nullable List<ChoseBean> data) {
        super(R.layout.item_chose, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChoseBean item) {
        if (item.getChose()) {
            helper.setBackgroundRes(R.id.chose_status, R.drawable.chose_yes);
            helper.setTextColor(R.id.chose_status, mContext.getResources().getColor(R.color.white));
        } else {
            helper.setBackgroundRes(R.id.chose_status, R.drawable.chose_not);
            helper.setTextColor(R.id.chose_status, mContext.getResources().getColor(R.color.black));
        }
        helper.setText(R.id.chose_status, item.getNumber());
        helper.addOnClickListener(R.id.chose_status);
    }
}
