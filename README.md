# LAN-局域网功能实践

作者：wanxuedong
qq：238114912
制作时间：2021-2-22

简介
局域网下实现群聊的功能
一个或多个用户创建服务器，其余用户可以搜索并加入房间，所有房间内的聊天和进出状态会通知到所有人
使用点播技术和udp搜索同一局域网下的设备并互相交换ip，再使用tcp发送消息

重要API
1.点播技术的MulticastSocket
2.UDP协议的DatagramSocket和DatagramPacket
3.TCP协议的ServerSocket和Socket




