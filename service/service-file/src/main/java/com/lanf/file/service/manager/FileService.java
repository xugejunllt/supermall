package com.lanf.file.service.manager;


import com.lanf.file.model.bo.FileUploadResultBO;
import com.lanf.file.model.dto.FileUploadDTO;
import com.lanf.file.model.dto.FileUploadFileDTO;

public interface FileService {

    static final String IMAGE_DIR = "images/";

    /**
     * 文件上传
     *
     */


    FileUploadResultBO uploadFile(FileUploadFileDTO dto);
}
