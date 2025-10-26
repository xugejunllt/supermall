package com.lanf.file.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileUploadResultBO implements Serializable {

  private  String fileUrl;

  public FileUploadResultBO(String fileUrl) {
    this.fileUrl = fileUrl;
  }
}
