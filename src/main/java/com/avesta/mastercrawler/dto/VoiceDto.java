package com.avesta.mastercrawler.dto;

public class VoiceDto {
        private String fileName;
        private double fileSize;

        public VoiceDto(String fileName, double fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public double getFileSize() {
            return fileSize;
        }

        public void setFileSize(double fileSize) {
            this.fileSize = fileSize;
        }

    @Override
    public String toString() {
        return "VoiceDto{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
