package com.huawei.sdc.restful.activeregister;

import org.apache.commons.httpclient.HttpParser;
import com.huawei.sdc.restful.http.HttpHeadersHelper;
import com.huawei.sdc.restful.http.HttpReqLineHelper;
import com.huawei.sdc.restful.basic.SDCDeviceInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class ActiveRegisterSyncWorker {

    /* 主动注册长连接对端设备端口 */
    private String remoteHost;

    /* 主动注册长连接对端设备端口 */
    private int remotePort;

    /* 主动注册长连接输出流，基于此对象接收主动注册请求及后续请求的响应 */
    private InputStream inputStream;

    /* 主动注册长连接输出流，基于此对象发送后续请求 */
    private OutputStream outputStream;

    /* 当前使用的鉴权算法，必须与设备端配置一致 */
    private final static String authMethod = "MD5";

    /* 设备端北向接入账号、密码 */
    private String userName = "ApiAdmin";
    private String password = "ChangeMe123";
    private String auth="";
    /* 设备信息 */
    private SDCDeviceInfo deviceInfo = new SDCDeviceInfo();
    public SDCDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public ActiveRegisterSyncWorker(Socket acceptSocket) {
        try {
            InetSocketAddress remoteAdd = (InetSocketAddress)acceptSocket.getRemoteSocketAddress();
            remotePort = remoteAdd.getPort();
            remoteHost = remoteAdd.getHostName();
            this.inputStream = acceptSocket.getInputStream();
            this.outputStream = acceptSocket.getOutputStream();
            System.out.println("remote host: " + remoteHost + ", port: " + remotePort);
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void dealWithActiveRegisterReq (String natUrl) throws IOException {
        reqLineObj = null;
        httpHeadersHelper = null;
        httpBody = null;
        parseHttpMessage();

        /* 设备端配置的主动注册URL应与服务端一致 */
        if (!reqLineObj.url.equals(natUrl)) {
            System.out.println("wrong active register request!! reqUrl: " + reqLineObj.url + ", natUrl: " + natUrl);
        }

        try {
            deviceInfo = new SDCDeviceInfo();
            deviceInfo.sn = "1234567890";
            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                    "Connection: keep-alive\r\n\r\n";
            sendRequest(httpResponse);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /*
    * 示例，获取设备基础信息：GET /SDCAPI/V1.0/MiscIaas/System
    * TODO: 可参考此方法实现其他接口调用。
    * */
    public void postSystemInfo() throws IOException, NoSuchAlgorithmException {
        String testApi="{\"topic\":\"alarm.paas.sdc\",\"filter\":[{\"type\":\"event\",\"app\":\"itgtSaas\",\"name\":\"statistics_leaveDetect\"}],\"url\":\"http://10.0.0.105:5000/\",\"acceptType\":{\"dataType\":\"json\"}}";
        System.out.println("test start!------------------");
        String method = "POST";
        String url = "/SDCAPI/V1.0/Notification/Subscription/Topics";
        String reqLine = method + " " + url + " HTTP/1.1\r\n";
        HttpHeadersHelper headers = new HttpHeadersHelper();
        headers.addHeader("Accept", "*/*");
        headers.addHeader("Host", remoteHost + ":" + remotePort);
        headers.addHeader("Connection", "keep-alive");
        headers.addHeader("Content-Type", "application/json");
        headers.addHeader("Content-Length", String.valueOf(testApi.length()));

        sendReqAndRecvBody(new HttpReqLineHelper(reqLine),  headers, testApi.getBytes());

        // TODO: 处理响应，可基于成熟的json库解析响应消息(this.httpBody)
    }

    public void getSystemInfo() throws IOException, NoSuchAlgorithmException {
        String testApi="{\"mediaStreamParam\":{\"sessionID\":0,\"streamId\":0,\"tansmitType\":0,\"streamType\":0,\"compelIFrame\":0,\"keepalivePt\":0,\"timeStampRestart\":0,\"protocolType\":0,\"linkMode\":0,\"peerIp\":\"192.168.12.154\",\"peerPortInfo\":{\"interleavedPort\":45254,\"videoRtpPort\":20066,\"videoRtcpPort\":20067,\"audioRtpPort\":0,\"audioRtcpPort\":0,\"metadataRtpPort\":45256,\"metadataRtcpPort\":45256,\"defult\":0},\"localIp\":\"192.168.12.133\",\"networkCardType\":0,\"sessionIDForNAT\":\"\",\"sessionUrl\":\"\",\"sessionSrc\":0,\"notSendxNATInfo\":0,\"enableSinkPort\":0,\"sinkPortInfo\":{\"sinkAudioRtpPort\":0,\"defult\":0},\"encryPt\":3,\"videoCrypto\":\"\",\"audioCrypto\":\"\",\"metaCrypto\":\"\",\"videoCryptoLen\":0,\"audioCryptoLen\":0,\"metaCryptoLen\":0,\"iv\":\"\",\"metaDataPacketType\":0,\"metaBusiness\":0,\"metaRequestType\":0}}"+"\r\n";
        String method = "GET";
        String url = "/SDCAPI/V1.0/MiscIaas/System";
        String reqLine = method + " " + url + " HTTP/1.1\r\n";
        HttpHeadersHelper headers = new HttpHeadersHelper();
        headers.addHeader("Accept", "*/*");
        headers.addHeader("Host", remoteHost + ":" + remotePort);
        headers.addHeader("Connection", "keep-alive");

        sendReqAndRecvBody(new HttpReqLineHelper(reqLine),  headers, null);

        // TODO: 处理响应，可基于成熟的json库解析响应消息(this.httpBody)
    }
    private void sendReqAndRecvBody(HttpReqLineHelper reqLine, HttpHeadersHelper headers, byte[] body) throws IOException, NoSuchAlgorithmException {
        //String httpMessage = reqLine.toString() + headers.toString();
        StringBuilder httpMessage = new StringBuilder();
        httpMessage.append(reqLine.toString());
        httpMessage.append(headers.toString());
        if (null != body){
            sendRequest(httpMessage.toString(),body);
        }else {
            sendRequest(httpMessage.toString());
        }

        // receive response
        parseHttpMessage();
        if (null != reqLineObj && reqLineObj.statusCode == 401) {
            // deal with authentication
            headers.addHeader(httpHeadersHelper.getAuthorizationHeader(authMethod, "ApiAdmin", "HuaWei123", reqLine.method, reqLine.url));
            httpMessage = new StringBuilder();

            httpMessage.append(reqLine.toString()).append(headers.toString());
            sendRequest(httpMessage.toString());
            parseHttpMessage();
        }
    }

    // 本次接收到的响应的消息
    /* HTTP消息请求行 */
    private HttpReqLineHelper reqLineObj = null;
    /* HTTP消息头 */
    private HttpHeadersHelper httpHeadersHelper = null;
    /* HTTP消息体 */
    private byte[] httpBody = null;
    private void parseHttpMessage() throws IOException {
        reqLineObj = new HttpReqLineHelper(inputStream);

        httpHeadersHelper = new HttpHeadersHelper(HttpParser.parseHeaders(inputStream, "UTF-8"));
        int contentLen = httpHeadersHelper.contentLen;

        // read body
        httpBody = null;
        if (contentLen > 0) {
            httpBody = new byte[contentLen];
            int offset = 0;
            do{
                int readLen = inputStream.read(httpBody, offset, contentLen-offset);
                if (readLen < 0) {
                    // no more data
                    break;
                }
                offset += readLen;
            } while (offset < contentLen);
            // TODO: deal with message body， stored in httpBody. choose json library to decode it or save to file.

        }
        printHTTPMessage();
    }

    /* 发送带二进制body的请求（涉及文件上传） */
    public void sendRequest(String reqLineAndHeaders, byte[] body) throws IOException {
        System.out.println("sendRequest: \n" + reqLineAndHeaders);
        byte[] bt1=reqLineAndHeaders.getBytes();
        byte[] bt2=body;
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        if (null != body) {
            outputStream.write(bt3);
        }
    }

    /* 发送不带二进制body的请求 */
    public void sendRequest(String httpRequest) throws IOException {
        System.out.println(httpRequest);
        outputStream.write(httpRequest.getBytes());
    }

    private void printHTTPMessage() {
        if (null != reqLineObj) {
            System.out.print(reqLineObj.toString());
        }
        if (null != httpHeadersHelper) {
            System.out.print(httpHeadersHelper.toString());
        }
        if (null != httpBody) {
            System.out.println(new String(httpBody));
        }
    }
}
