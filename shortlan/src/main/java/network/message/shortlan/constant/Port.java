package network.message.shortlan.constant;


/**
 * @author mac
 * demo使用的一些端口常量
 */
public class Port {
    /**
     * 服务器和服务器互相确认过程中--服务器发送给客户端数据端口号
     **/
    public static final int PORT_SERVER_RESPOND = 8890;
    /**
     * 服务器和服务器互相确认过程中--组播组指定接受数据端口号
     **/
    public static final int PORT_MULTICAST_SOCKET = 8892;
    /**
     * 客户端和服务器数据真正交互端口号
     **/
    public static final int PORT_SERVER_SOCKET = 8891;

}
