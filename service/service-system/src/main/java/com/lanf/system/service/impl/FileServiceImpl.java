package com.lanf.system.service.impl;

import com.lanf.system.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/8/27 22:41
 */
@Service
public class FileServiceImpl implements FileService {

    public String upload(MultipartFile urlFile) throws IOException {
        // 文件名称
        Calendar calendar = Calendar.getInstance();
        String year = "" + calendar.get(Calendar.YEAR);
        String month = "" + (calendar.get(Calendar.MONTH) + 1);
        String day = "" + calendar.get(Calendar.DAY_OF_MONTH);
        String fileName = urlFile.getOriginalFilename();
        // 文件存储的路径
        String path = "\\img\\" + year + "\\" + month + "\\" + day + "\\";
        String filePath = System.getProperty("user.dir") + "\\src\\main\\resources\\static" + path;
        File file = new File(filePath + fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();                //不存在就全部创建
        }
        String storeUrlPath = path + fileName;
        urlFile.transferTo(file);
        System.out.println("上传成功");
        return storeUrlPath;
    }

}
