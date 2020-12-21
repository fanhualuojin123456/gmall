package com.ping.gmall.manage.controller;


import com.aliyun.oss.OSSClient;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

//@RestController
//@CrossOrigin
public class FileUploadOSSController {

    /**
     * 上传文件
     * 接收请求
     * 返回响应
     */
    private static String TYPESTR[] = {".png",".jpg",".bmp",".gif",".jpeg"};
//    @RequestMapping("fileUpload")
    public String upload(MultipartFile file,String host){
        if (!StringUtils.isEmpty(host)){
            ConstantPropertiesUtil.FILE_HOST = host;
        }

        OSSClient ossClient = null;
        String url = null;
        try {
            // 创建OSSClient实例。
            ossClient = new OSSClient(
                    ConstantPropertiesUtil.END_POINT,
                    ConstantPropertiesUtil.ACCESS_KEY_ID,
                    ConstantPropertiesUtil.ACCESS_KEY_SECRET);
            boolean flag = false;
            //判断文件格式
            for(String type : TYPESTR){
                if(StringUtils.endsWithIgnoreCase(file.getOriginalFilename(),type)){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return "图片格式不正确";
            }
            //判断文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image != null){
                System.err.println(String.valueOf(image.getHeight()));
                System.err.println(String.valueOf(image.getWidth()));
            } else{
                return "文件内容不正确";
            }
            //获取文件名称
            String filename = file.getOriginalFilename();
            //文件名字： lijin.shuai.jpg
            String ext = filename.substring(filename.lastIndexOf("."));
            String newName = UUID.randomUUID().toString() + ext;
            String dataPath = new DateTime().toString("yyyy/MM/dd");
            String urlPath = ConstantPropertiesUtil.FILE_HOST + "/" + dataPath + "/" + newName;
            // 上传文件流。
            InputStream inputStream = file.getInputStream();
            ossClient.putObject(ConstantPropertiesUtil.BUCKET_NAME,urlPath,inputStream);
            url = "https://" + ConstantPropertiesUtil.BUCKET_NAME + "." + ConstantPropertiesUtil.END_POINT + "/" + urlPath;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }

        return url;
    }
}
