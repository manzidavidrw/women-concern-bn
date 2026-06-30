package com.womenconcern.api.common.storage.service;

import com.womenconcern.api.common.storage.dto.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    UploadedFile upload(MultipartFile file);

    void delete(String publicId);
}
