package com.skybit.mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CermatPrijimacky {
    private static final Logger log = Logger.getLogger(CermatPrijimacky.class.getName());
    public static final String URL = "https://prijimacky.cermat.cz";
    public static final String URL_TESTS = URL + "/menu/testova-zadani-k-procvicovani/testova-zadani-v-pdf";

    public static void main(String[] args) throws IOException {

        log.info("Downloading 'prijimacky' from " + URL_TESTS);

        Document doc = Jsoup.connect(URL_TESTS).get();
        Elements links = doc.select("ul.nav.menu.graphic-menu a[href]");

        for (Element link : links) {
            String pathPrefix = unifyString(link.text());
            String testUrl = URL + link.attr("href");

            log.info("\tlet download all PDFs (" + pathPrefix + ") at " + testUrl);

            //get the div with PDFs
            doc = Jsoup.connect(testUrl).get();
            Element itemPage = doc.selectFirst("div.item-page");

            String currentH2 = "unknown";
            String currentYear = "unknown";
            for (Element e : itemPage.children()) {
                if (e.tag().getName().equals("h2")) {
                    currentH2 = unifyString(e.text());
                    int yearIdx = currentH2.indexOf("_2");
                    currentYear = currentH2.substring(yearIdx + 1, yearIdx + 5);
                    continue;
                }
                if (e.tag().getName().equals("ul")) {
                    Elements pdfLinks = e.select("a[href]");

                    for (Element pdfLink : pdfLinks) {
                        String fileText = unifyString(pdfLink.select("span").text());
                        if (fileText.isEmpty()) {
                            fileText = unifyString(pdfLink.text());
                        }

                        String pdfUrl = URL + pdfLink.attr("href");
                        String path = "download/cermat/" + pathPrefix + "/" + currentYear + "/" + currentH2;
                        String filename = fileText + ".pdf";

                        log.info("\t\tdownloading " + currentH2 + " - " + filename + " at " + pdfUrl);
                        createDirectory(path);
                        Utils.downloadPdf(path + "/" + filename, pdfUrl);
                    }
                }
            }
        }
    }

    private static String unifyString(String s) {
        return StringUtils.stripAccents(s).replaceAll("[^A-Za-z0-9]", "_");
    }

    private static void createDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
