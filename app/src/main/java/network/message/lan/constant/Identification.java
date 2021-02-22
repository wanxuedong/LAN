package network.message.lan.constant;

/**
 * @author mac
 * 定义特定消息字符串的标识
 */
public class Identification {

    /**
     * 三种消息之一
     * 加入房间的字符串前后缀
     **/
    public static final String JOIN_START = "lzg.chatRoom";
    public static final String JOIN_END = "First.in";

    /**
     * 三种消息之一
     * 心跳信息
     * **/
    public static final String HEART_CHECK_CODE = "LZG.hEaRTcHckE";

    /**
     * 三种消息之一
     * 正常交互消息
     * **/
    public static final String MESSAGE_END = "DatA iS eNd";

}
