package network.message.lan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import network.message.lan.R;

/**
 * @author mac
 */
public class MessageAdapter extends BaseAdapter {

    private Context context;
    private List<String> list = new ArrayList<>();

    public MessageAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_talk_message, null);
        TextView message = view.findViewById(R.id.message);
        message.setText(list.get(position));
        return view;
    }

}
