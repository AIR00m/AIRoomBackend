package com.airoom.airoom.attach.model.service;

import com.airoom.airoom.attach.model.dto.PresignedUrlRequest;
import com.airoom.airoom.attach.model.dto.PresignedUrlResponse;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public PresignedUrlResponse generateUploadUrl(PresignedUrlRequest urlRequest) {
        String extension = getExtension(urlRequest.getOriginalName());
        String savedName = UUID.randomUUID() + extension;
        String s3Key = urlRequest.getBoardType().name().toLowerCase() +"/"+ urlRequest.getBoardNo() + "/" + savedName;

        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 5);

        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucket, s3Key)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(request);

        return new PresignedUrlResponse(url.toString(), urlRequest.getOriginalName(), savedName, s3Key);
    }

    public String generateDownloadUrl(String s3Key) {
        Date expiration = new Date(System.currentTimeMillis() + 1000 * 60 * 5);

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, s3Key)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(request).toString();
    }

    private String getExtension(String name) {
        return name.contains(".") ? name.substring(name.lastIndexOf(".")) : "";
    }
}

