package com.tiancai.controller;

import com.tiancai.entity.Result; // 确保导入你的Result类
import com.tiancai.entity.Result;
import com.tiancai.utils.AliOSSUtils; // 导入我们的工具类
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Slf4j // 使用Lombok的Slf4j注解，方便打日志
@RestController
public class UploadController {

    @Autowired
    private AliOSSUtils aliOSSUtils;

    /**
     * 通用文件上传接口
     * @param file 前端上传的文件，参数名需要与前端表单项的name一致
     * @return 包含文件访问URL的Result对象
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        log.info("文件上传开始，文件名: {}", file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            return Result.error("上传失败，请选择文件");
        }

        try {
            // 1. 调用工具类，传入MultipartFile对象，进行文件上传
            String url = aliOSSUtils.upload(file);
            log.info("文件上传成功，访问URL: {}", url);
            
            // 2. 上传成功后，将文件的URL返回给前端
            // 前端可以利用这个URL进行图片预览，或者在提交表单时一并发送回来
            return Result.success(url);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            // 捕获IO异常，并返回统一的错误信息
            return Result.error("文件上传失败，请稍后重试");
        } catch (com.aliyuncs.exceptions.ClientException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
    }
}