package com.zhoubyte.scorpioaspose.service;

import java.util.List;

public interface PdfService {

    List<String> toPictures(String filePath);

    String toWord(String filePath);

    String fill(String filePath);
}
