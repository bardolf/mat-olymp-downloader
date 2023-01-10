package com.skybit.mod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Utils {
    private Utils() {
    }

    public static void downloadPdf(String filename, String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpget)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(filename);
                        IOUtils.copy(inputStream, fos);
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                    fos.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
