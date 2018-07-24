package com.pinyougou.manage.controller;

import com.pinyougou.common.util.FastDFSClient;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    /**
     * 将图片保存到fastDFS中并返回图片可访问路径
     * @param file 图片
     * @return
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file){

        try {
            //利用上传图片工具类上传图片
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastdfs/tracker.conf");
            //获取图片的后缀
            String file_ext_name = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);

            String url = fastDFSClient.uploadFile(file.getBytes(), file_ext_name);

            return Result.ok(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.fail("上传图片失败");

    }
}
