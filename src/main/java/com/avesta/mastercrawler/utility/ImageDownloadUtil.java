package com.avesta.mastercrawler.utility;

import com.avesta.mastercrawler.model.Images;
import com.avesta.mastercrawler.service.IImagesService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ImageDownloadUtil {

    public static String processImages(String body, IImagesService iImagesService) {
        Document document = org.jsoup.Jsoup.parse(body);
        Elements imgTags = document.select("img[src]");

        for (Element imgTag : imgTags) {
            String imgSrc = imgTag.attr("src");
            System.out.println("Processing image: " + imgSrc);

            if (imgSrc.contains("/news/photos/")) {
                System.out.println("Skipping image (already downloaded): " + imgSrc);
                continue;
            }

            try {

                String uploadDir = "news/photos/";
                Files.createDirectories(Paths.get(uploadDir));


                String imageName = "news_image_" + System.currentTimeMillis() + ".jpg";
                Path imagePath = Paths.get(uploadDir, imageName);


                try (InputStream in = new URL(imgSrc).openStream()) {
                    Files.copy(in, imagePath);
                }

                imgTag.attr("src", "/" + uploadDir + imageName);

                Images gallery = new Images(imageName, '/'+uploadDir+imageName );
                System.out.println(gallery);
                iImagesService.save(gallery);

            } catch (IOException e) {
                System.err.println("Error downloading or saving image: " + imgSrc);
                e.printStackTrace();
            }
        }

        return document.body().html();
    }

    public static String sanitizeHeadline(String headline) {
        return headline.replaceAll("/", " ");
    }
}
