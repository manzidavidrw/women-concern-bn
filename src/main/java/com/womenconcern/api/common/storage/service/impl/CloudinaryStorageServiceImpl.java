package com.womenconcern.api.common.storage.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.womenconcern.api.common.storage.dto.UploadedFile;
import com.womenconcern.api.common.storage.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageServiceImpl implements IFileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public UploadedFile upload(MultipartFile file) {

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.emptyMap()
            );

            return new UploadedFile(
                    result.get("secure_url").toString(),
                    result.get("public_id").toString()
            );

        } catch (IOException e) {
            throw new RuntimeException("Unable to upload file", e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

            String status = result.get("result").toString();

            if (!"ok".equals(status)) {
                throw new RuntimeException("Cloudinary deletion failed: " + status);
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to delete file", e);
        }
    }
}