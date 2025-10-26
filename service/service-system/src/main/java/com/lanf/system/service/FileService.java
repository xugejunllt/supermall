package com.lanf.system.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/8/27 22:40
 */
public interface FileService {
    public String upload(MultipartFile urlFile) throws IOException;
}
