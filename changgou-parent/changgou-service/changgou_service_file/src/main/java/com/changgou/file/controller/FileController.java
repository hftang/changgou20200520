package com.changgou.file.controller;

import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import entity.Result;
import entity.StatusCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Auther: hftang
 * @Date: 2019/12/23 16:10
 * @Description:
 */
@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {


    @GetMapping("/a")
    public void getA() {
        int i = 100;
        System.out.println("---->");
    }

    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {

        System.out.println("" + file);


        try {
            if (file == null) {
                throw new RuntimeException("文件不存在");
            }
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isEmpty(originalFilename)) {
                throw new RuntimeException("文件不存在");
            }

            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            //获取文件内容
            byte[] content = file.getBytes();

            //创建文件上传的封装实体类
            FastDFSFile fastDFSFile = new FastDFSFile(originalFilename, content, extName);

            //基于工具类进行文件上传,并接受返回参数  String[]
            String[] uploadResult = FastDFSClient.upload(fastDFSFile);

            //封装返回结果
            String url = FastDFSClient.getTrackerUrl() + uploadResult[0] + "/" + uploadResult[1];
            return new Result(true, StatusCode.OK, "文件上传成功", url);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return new Result(false, StatusCode.ERROR, "文件上传失败");
    }
}
