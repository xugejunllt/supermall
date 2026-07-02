package com.lanf.file.service.manager.impl;

import com.lanf.constant.exception.BizException;
import com.lanf.file.model.bo.FileUploadResultBO;
import com.lanf.file.model.dto.FileUploadFileDTO;
import com.lanf.file.service.manager.FileService;
import com.lanf.file.service.manager.impl.config.FileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class LocalFileServiceImpl implements FileService {

    @Autowired
    private FileConfig fileConfig;

    @Override
    public FileUploadResultBO uploadFile(FileUploadFileDTO dto) {

        String path = fileConfig.getLocalPath();
        MultipartFile file = dto.getMultipartFile();
        try (java.io.InputStream inputStream = file.getInputStream()) {
            // 确保上传目录存在
            Path uploadDir = Paths.get(path);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String uniqueName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            // 保存文件到本地
            Path filePath = uploadDir.resolve(uniqueName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            String url = fileConfig.getDomain() + fileConfig.getLocalFileUrlPre()+"/" + file.getOriginalFilename();

           return  new FileUploadResultBO(url);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException("文件上传失败");
        }

    }
}
