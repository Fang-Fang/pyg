package cn.itcast.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

public class FastDFSTest {

    @Test
    public void test() throws Exception {

        //获取配置文件（绝对路径）
        String conf_filename = ClassLoader.getSystemResource("fastdfs/tracker.conf").getPath();

        //设置全局的配置信息
        ClientGlobal.init(conf_filename);

        //创建trackerClient
        TrackerClient trackerClient = new TrackerClient();

        //创建trackerServer
        TrackerServer trackerServer = trackerClient.getConnection();

        //存储服务器，因为可以由trackerClient得到，所以可以为空
        StorageServer storageServer = null;

        //上传文件
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);

        /**
         * 参数1：文件路径
         * 参数2：扩展名，后缀
         * 参数3：文件其它信息
         */
        String[] upload_file = storageClient.upload_file("D:\\itcast\\pics\\575968fcN2faf4aa4.jpg", "jpg", null);

        /**
         * 返回内容如下：
         * group1  组名
         * M00/00/00/wKgMqFs4V6CAEJr3AABw0se6LsY137.jpg 文件相对路径
         */
        if (upload_file != null && upload_file.length > 0) {

            for (String info : upload_file) {
                System.out.println(info);
            }


            //根据文件信息获取存储服务器信息
            //参数1：trackerServer，参数2：组名，参数3：相对路径
            ServerInfo[] serverInfos = trackerClient.getFetchStorages(trackerServer, upload_file[0], upload_file[1]);
            for (ServerInfo serverInfo : serverInfos) {
                System.out.println("ip = " + serverInfo.getIpAddr() + "；port=" + serverInfo.getPort());
            }


            String url = "http://" + serverInfos[0].getIpAddr() + "/" + upload_file[0] + "/" + upload_file[1];

            System.out.println(url);
        }



    }
}