package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.security.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final String fileSeparator = System.getProperty("file.separator");
    private final String rootPath = File.listRoots()[0].getPath() + fileSeparator + "api_doc_center";

    @ResponseBody
    @RequestMapping("/upload")
    public Response<Map<String,String>> upload(MultipartFile file_data) {
        if(file_data == null) {
            return Response.fail(StringConstants.REQUEST_FILE_IS_NULL);
        }
        MultipartFile file = file_data;
        User user = SecurityUtil.getUser();
        long userId = user.getId();
        log.debug("upload file {} {} user id={}", file.getName(), file.getOriginalFilename(), userId);
        try {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            File file1 = new File(rootPath + fileSeparator + userId + fileSeparator + now + file.getOriginalFilename());
            if(file1.mkdirs()) {
                file.transferTo(file1);
                String downloadUrl = "/file/download?userId=" + userId + "&fileName=" + now + file.getOriginalFilename();
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("filename", file.getOriginalFilename());
                dataMap.put("downloadUrl", downloadUrl);
                return Response.success(dataMap);
            } else {
                return Response.fail(StringConstants.UPLOAD_FAIL);
            }
        } catch (IOException e) {
            log.error("upload file error userId={} file={}", userId, file.getOriginalFilename(), e);
            return Response.fail(StringConstants.UPLOAD_FAIL);
        }
    }

    @RequestMapping("/download")
    public void downloadLocal(@RequestParam long userId, @RequestParam String fileName, HttpServletResponse response) throws IOException {
        String path = rootPath + fileSeparator + userId + fileSeparator + fileName;
        File file = new File(path);
        if(!file.exists()) {
            throw new FileNotFoundException(StringConstants.FILE_NOT_FOUND);
        }
        // 读到流中
        InputStream inputStream = new FileInputStream(path);// 文件的存放路径
        response.reset();
        response.setContentType("application/octet-stream");
        String filename = new File(path).getName();
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        byte[] b = new byte[1024];
        int len;
        //从输入流中读取一定数量的字节，并将其存储在缓冲区字节数组中，读到末尾返回-1
        while ((len = inputStream.read(b)) > 0) {
            outputStream.write(b, 0, len);
        }
        inputStream.close();
    }

}
