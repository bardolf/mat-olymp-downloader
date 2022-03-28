package com.skybit.mod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
    public static final String URL = "http://www.matematickaolympiada.cz/";

    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        Elements links = doc.select("a[href]");

        Element zsLink = null;
        for (Element link : links) {
            if (link.text().equals("Olympiáda pro ZŠ")) {
                zsLink = link;
                break;
            }
        }
        if (zsLink == null) {
            throw new RuntimeException("Unable to find ZS link.");
        }

        links = zsLink.parent().children().select("ul").get(0).children().select("a");


        for (Element link : links) {
            doc = Jsoup.connect(URL + link.attr("href")).get();
            Elements pdfs = doc.select("a[href]");

            for (Element pdf : pdfs) {
                if (!pdf.attr("href").endsWith("pdf")) {
                    continue;
                }
                String category = "download/" + pdf.text();
                String href = pdf.attr("href");
                Files.createDirectories(Paths.get(category));
                String filename = href.substring(href.lastIndexOf("/") + 1);
                System.out.println("Downloading " + filename);
                downloadPdf(category + "/" + filename, URL + href);
            }
        }
    }

    private static void downloadPdf(String filename, String url) {
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
