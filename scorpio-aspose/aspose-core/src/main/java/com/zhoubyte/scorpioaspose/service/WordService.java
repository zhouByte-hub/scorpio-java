package com.zhoubyte.scorpioaspose.service;

import com.zhoubyte.scorpioaspose.dto.OfferDto;

import java.util.List;

public interface WordService {

    String fillByPlaceholder(String filePath, OfferDto offerDto);

    String fillByBookmark(String filePath, OfferDto offerDto);

    List<String> toPictures(String filePath);

    String toPdf(String filePath);
}
