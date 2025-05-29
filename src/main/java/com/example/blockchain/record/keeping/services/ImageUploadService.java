package com.example.blockchain.record.keeping.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadService {
    private final Cloudinary cloudinary;

    public ImageUploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return result.get("secure_url").toString(); // trả về link ảnh
    }

    public String uploadImage(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        byte[] imageBytes = baos.toByteArray();

        String randomFileName = "certificate_" + UUID.randomUUID();

        Map<?, ?> result = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap(
                "public_id", randomFileName,
                "resource_type", "image"
        ));

        return result.get("secure_url").toString();
    }

}
