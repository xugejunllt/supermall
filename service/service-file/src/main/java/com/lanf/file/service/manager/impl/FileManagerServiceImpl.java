package com.lanf.file.service.manager.impl;

import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.file.model.dto.FileUploadDTO;
import com.lanf.file.model.dto.FileUploadFileDTO;
import com.lanf.file.service.manager.FileManagerService;
import com.lanf.file.service.manager.FileService;
import com.lanf.file.service.manager.impl.config.FileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class FileManagerServiceImpl implements FileManagerService {

    @Autowired
    private FileConfig fileConfig;

    private List<String> images = Arrays.asList("png","jpg");


    @Override
    public FileUploadDTO checkAndBuild(FileUploadFileDTO dto) {

        MultipartFile multipartFile = dto.getMultipartFile();
        //357f5ba47b439865088a6a5eef9cdb0.png
        String originalFilename = multipartFile.getOriginalFilename();
        String[] split = originalFilename.split("\\.");
        if (split.length<2){
            throw new BizException("不允许上传没有后缀的文件");
        }
        //文件后缀
        String v = split[split.length-1];
        if (images.contains(v)){
            //图片
            byte[] multipartFileBytes = getByte(multipartFile);
            int fileSize = toM(multipartFileBytes.length);
            if (multipartFileBytes.length<=0){
                throw new BizException("文件为空");
            }

            if (fileSize > fileConfig.getImageMax()){
                throw new BizException("超过文件大小限制");
            }
            //构建FileUploadDTO
            String dir = FileService.IMAGE_DIR;
            //图片名称默认使用时间戳作为文件名
            String fileName = System.currentTimeMillis()+"."+v;
            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            fileUploadDTO.setDir(dir);
            fileUploadDTO.setContent(multipartFileBytes);
            fileUploadDTO.setFileName(fileName);
            return  fileUploadDTO;

        }

        throw new BizException("不支持的文件上传类型");


    }
    private byte[] getByte(MultipartFile multipartFile){
        byte[] multipartFileBytes = null;
        try {
            multipartFileBytes = multipartFile.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException("文件上传失败");
        }
        return  multipartFileBytes;
    }
    private int toM(int byteSize ){

        BigDecimal v2 = BigDecimalUtil.divide(new BigDecimal(byteSize), new BigDecimal(1024*1024));

        return  v2.intValue();
    }

}
