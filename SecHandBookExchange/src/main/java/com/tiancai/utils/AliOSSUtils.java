package com.tiancai.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传工具类
 * <p>
 * 该工具类自动从系统环境变量中读取 AccessKeyId 和 AccessKeySecret。
 * 请确保已设置环境变量：OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET。
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliOSSUtils {

    private String endpoint;
    private String bucketName;
    private String region; // region 也是你示例中需要的

    // Setters for @ConfigurationProperties
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 上传文件到OSS，并返回文件的访问URL。
     *
     * @param file Spring MVC 的 MultipartFile 对象
     * @return 文件的公网访问URL
     * @throws IOException 如果文件读取失败
     */
    public String upload(MultipartFile file) throws IOException,com.aliyuncs.exceptions.ClientException {
        // 1. 从环境变量中获取访问凭证
        CredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 2. 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 3. 避免文件覆盖，生成唯一的文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
             fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString() + fileExtension;

        // 4. 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);

        try {
            // 5. 上传文件到指定的 bucket 和 objectName
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw new RuntimeException("文件上传到OSS时出错", oe);
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            throw new RuntimeException("OSS客户端连接出错", ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 6. 拼接并返回文件的公网访问URL
        String fileUrl = "https://" + bucketName + "." + endpoint.substring(endpoint.lastIndexOf("/") + 1) + "/" + objectName;
        return fileUrl;
    }
}