package network.message.shortlan.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * @author mac
 */
public class ServiceUtil {

    /**
     * 通过获取100个正在service
     * 判断想要的某个service是否运行
     *
     * @param mContext
     * @param className
     * @return
     */
    // TODO: 2021/2/23 处理方式过于愚笨，建议优化
    public static boolean isServiceRunning(Context mContext, String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(100);

        if (serviceList.size() == 0) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
