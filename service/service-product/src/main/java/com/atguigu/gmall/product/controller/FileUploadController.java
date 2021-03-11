package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import io.swagger.annotations.Api;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mqx
 * @date 2021-2-2 10:23:51
 */
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("admin/product")
public class FileUploadController {

    //  获取文件上传服务器的Ip 地址
    //  将服务的Ip 地址配置在配置文件中这种方式叫：软编码
    @Value("${fileServer.url}")
    private String fileUrl;  // fileUrl = http://192.168.200.128:8080/
    //  http://api.gmall.com/admin/product/fileUpload
    //  文件上传的时候，我们如何获取到文件的名称以及文件的数据?
    //  文件名称不能重复，后缀名要与源文件后缀名保持一致，设置文件的大小，格式等等......
    //  springMvc 讲文件上传的时候，使用过一个对象{MultipartFile  file}  file 与页面数据接口对象是一致！
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception{
        /*
            1.  读取到tracker.conf 文件
            2.  初始化FastDFS
            3.  创建对应的TrackerClient,TrackerServer
            4.  创建一个StorageClient，调用文件上传方法
            5.  获取到文件上传的url 并返回
         */
        //  读取这个配置文件的时候：我们项目路径不能有空格，中文等。
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        //  表示记录文件url
        String path = null;

        //  判断是否读取到了数据
        if(configFile!=null){
            //  有异常需要抛出！
            ClientGlobal.init(configFile);
            //  TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //  TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            // StorageClient 存储文件用的
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,null);

            //  获取文件的后缀名 101.jpg  使用工具类 .jpg
            String extName = FilenameUtils.getExtension(file.getOriginalFilename());
            // 调用文件上传方法 返回的是文件上传之后的url
            //  path=group1/M00/00/02/wKjIgF_GZBqEKKqDAAAAAIyTSXk606.png
            path = storageClient1.upload_appender_file1(file.getBytes(), extName, null);
            //  才是文件上传之后的全路径
            System.out.println("文件上传之后的全路径:\t"+fileUrl + path);
        }
        //  将文件的整体全路径放入data 中
        //  http://192.168.200.128:8080/group1/M00/00/02/wKjIgF_GZBqEKKqDAAAAAIyTSXk606.png
        return Result.ok(fileUrl + path);
    }
}
