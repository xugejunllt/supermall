package com.lanf.file.controller;

import com.lanf.constant.result.Result;
import com.lanf.file.model.bo.FileUploadResultBO;
import com.lanf.file.model.dto.FileUploadFileDTO;
import com.lanf.file.service.manager.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {


    @Autowired
    private FileService fileService;

    @PostMapping("/uploadFile")
    public Result<FileUploadResultBO> uploadFile(@RequestParam("multipartFile") MultipartFile multipartFile) {

        log.info("进行文件上传");
        FileUploadFileDTO dto = new FileUploadFileDTO();
        dto.setMultipartFile(multipartFile);

       return Result.ok(fileService.uploadFile(dto));
    }














}
