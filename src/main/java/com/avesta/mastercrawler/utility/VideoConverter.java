package com.avesta.mastercrawler.utility;

import java.io.IOException;

public class VideoConverter {

    private static final String FFMPEG_PATH = "/usr/local/bin/ffmpeg";

    public static void convertToSafariCompatible(String inputFilePath, String outputFilePath) throws IOException, InterruptedException {
        String command = String.format(
                "%s -i %s -c:v libx264 -profile:v high -level:v 4.2 -pix_fmt yuv420p -c:a aac -b:a 128k -movflags +faststart %s",
                FFMPEG_PATH,
                inputFilePath,
                outputFilePath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Video conversion failed with exit code " + exitCode);
        }
    }
}
