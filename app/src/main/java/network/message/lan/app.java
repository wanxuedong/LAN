package network.message.lan;

import android.app.Application;

import network.message.shortlan.utils.BaseUtil;
import network.message.shortlan.utils.CallBack;

public class app extends Application implements CallBack<Application> {

    @Override
    public void onCreate() {
        super.onCreate();
        BaseUtil.setCallBack(this);
    }

    @Override
    public Application call(String... data) {
        return this;
    }
}
