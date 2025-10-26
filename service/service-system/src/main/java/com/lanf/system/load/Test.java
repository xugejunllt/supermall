//package com.lanf.system.load;
//
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.api.naming.NamingMaintainFactory;
//import com.alibaba.nacos.api.naming.NamingMaintainService;
//import com.alibaba.nacos.api.naming.pojo.Instance;
//
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//public class Test {
//
//    public static void main(String[] args) throws NacosException {
//
//        Properties properties = new Properties();
//        properties.put("serverAddr", "localhost:8848");
//        Map<String,String> updatedMetadata = new HashMap<>();
//        updatedMetadata.put("appVersion", "14");
//
//        NamingMaintainService maintainService = NamingMaintainFactory.createMaintainService(properties);
//        Instance instance = new Instance();
//        instance.setIp(getIp());
//        instance.setPort(9002);
//        instance.setMetadata(updatedMetadata);
//        maintainService.updateInstance("service-system", "DEFAULT_GROUP",instance);
//
//
//    }
//
//    private static  String getIp(){
//
//        try {
//            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); // 获取所有网络接口
//            while (interfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = interfaces.nextElement();
//                // 跳过回环接口和未启动的接口
//                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
//                    continue;
//                }
//
//                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
//                while (addresses.hasMoreElements()) {
//                    InetAddress address = addresses.nextElement();
//                    // 检查是否是IPv4地址
//                    if (address instanceof java.net.Inet4Address) {
//                        // 输出内网IP地址
//                        return  address.getHostAddress();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace(); // 打印网络异常信息
//        }
//        throw new RuntimeException("获取内网ip失败");
//    }
//
//}
