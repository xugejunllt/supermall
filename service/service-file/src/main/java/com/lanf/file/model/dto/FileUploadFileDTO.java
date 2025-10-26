package com.lanf.file.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
public class FileUploadFileDTO implements Serializable {

    private MultipartFile multipartFile;

}
