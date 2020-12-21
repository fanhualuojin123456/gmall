package com.ping.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ping.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {

    @Value("${fileService.url}")
    private String fileUrl;

   @RequestMapping("fileUpload")
   public String fileUpload(MultipartFile file) throws IOException, MyException {

        String imgUrl = fileUrl;
        if (file != null){
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            // 获取连接
            TrackerServer trackerServer=trackerClient.getConnection();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            // 获取上传文件名称
            String orginalFilename = file.getOriginalFilename();
            // 获取文件的后缀名
            String extName = StringUtils.substringAfterLast(orginalFilename, ".");
            // 上传图片
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                //			s = group1
                //s = M00/00/00/wKjqg1_TRB-AHEoiAAXYVAD014s663.jpg
                //192.168.234.131/group1/M00/00/00/wKjqg1_TRB-AHEoiAAXYVAD014s663.jpg
                imgUrl += "/" + path;
            }
            System.out.println(imgUrl);
        }
        return imgUrl;
    }
}
