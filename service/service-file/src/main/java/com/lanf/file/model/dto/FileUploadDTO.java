package com.lanf.file.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class FileUploadDTO implements Serializable {

    private  String dir;

    private String fileName;

    private byte[] content;

}
