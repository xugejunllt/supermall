package com.lanf.file.service.manager.impl;

import com.lanf.file.model.bo.FileUploadResultBO;
import com.lanf.file.model.dto.FileUploadDTO;
import com.lanf.file.model.dto.FileUploadFileDTO;
import com.lanf.file.service.manager.FileManagerService;
import com.lanf.file.service.manager.FileService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractFileService implements FileService {
    @Autowired
    private FileManagerService fileManagerService;
    @Override
    public FileUploadResultBO uploadFile(FileUploadFileDTO dto) {

        FileUploadDTO uploadDTO = fileManagerService.checkAndBuild(dto);

        return upload( uploadDTO);
    }

   public abstract FileUploadResultBO upload(FileUploadDTO dto);
}
