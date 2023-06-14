package com.huawei.sdc.restful.activeregister;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ActiveRegisterDemo {
        /* 平台侧主动注册服务监听地址、端口、URL */
        private static final String host = "172.16.1.98";
        private static final int port = 8083;
        private static final String natUrl = "/nat";

        /* 主动注册传输方式: TCP/TLS */
        private static boolean useTLS = false;

        public static void main(String[] args) throws Exception {
            try {
                // TLS Server test
                ActiveRegisterServerDemo server;
                if (useTLS) {
                    server = ActiveRegisterTLSServerDemo.create(host, port, natUrl);
                } else {
                    server = ActiveRegisterTCPServerDemo.create(host, port, natUrl);
                }
                new Thread(server).start();

                // TODO: Demo Test, 通过主动注册长连接发送请求
                TestSendRequest(server);
                System.out.println("testtt");
                TestSendPostRequest(server);

                System.out.println("over");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        }

        private static void TestSendRequest(ActiveRegisterServerDemo server) throws InterruptedException, IOException, NoSuchAlgorithmException {
            ActiveRegisterSyncWorker worker;

            do {
                /* TODO: 根据SN号查询上线设备，并向其发送获取设备基础信息请求 */
                worker = server.getDeviceWorker("1234567890");
                if (worker != null) {
                    worker.getSystemInfo();
                    break;
                }
                /* 等待设备上线 */
                Thread.sleep(1000);
            } while(true);
        }


    private static void TestSendPostRequest(ActiveRegisterServerDemo server) throws InterruptedException, IOException, NoSuchAlgorithmException {
        ActiveRegisterSyncWorker worker;

        do {
            /* TODO: 根据SN号查询上线设备，并向其发送获取设备基础信息请求 */
            worker = server.getDeviceWorker("1234567890");
            if (worker != null) {
                worker.postSystemInfo();
                break;
            }

            /* 等待设备上线 */
            Thread.sleep(1000);
        } while(true);
    }
}
