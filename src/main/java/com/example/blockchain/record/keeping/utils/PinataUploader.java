package com.example.blockchain.record.keeping.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PinataUploader {

    private static final String API_KEY = EnvUtil.get("PINATA_API_KEY");
    private static final String SECRET_API_KEY = EnvUtil.get("PINATA_SECRET_API_KEY");

    public static String uploadFromUrlToPinata(String imageUrl) throws IOException {
        String pinataUrl = "https://api.pinata.cloud/pinning/pinFileToIPFS";

        // Tải ảnh từ Cloudinary về
        InputStream imageStream = new URL(imageUrl).openStream();

        // Gửi ảnh lên Pinata
        HttpPost post = new HttpPost(pinataUrl);
        post.setHeader("pinata_api_key", API_KEY);
        post.setHeader("pinata_secret_api_key", SECRET_API_KEY);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", imageStream, ContentType.DEFAULT_BINARY, "certificate.png");

        HttpEntity entity = builder.build();
        post.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {

            String json = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(json);

            if (!jsonObject.has("IpfsHash")) {
                throw new RuntimeException("Pinata response does not contain 'IpfsHash': " + jsonObject.toString());
            }
            return jsonObject.getString("IpfsHash");
        }
    }
}

