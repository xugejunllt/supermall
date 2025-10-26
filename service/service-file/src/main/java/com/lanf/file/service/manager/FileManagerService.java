package com.lanf.file.service.manager;

import com.lanf.file.model.dto.FileUploadDTO;
import com.lanf.file.model.dto.FileUploadFileDTO;

public interface FileManagerService {

    /**
     * 校验上传文件和构建FileUploadDTO
     *
     *
     */
    FileUploadDTO checkAndBuild(FileUploadFileDTO dto);
}
