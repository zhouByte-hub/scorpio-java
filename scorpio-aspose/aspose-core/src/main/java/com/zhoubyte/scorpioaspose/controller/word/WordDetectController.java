package com.zhoubyte.scorpioaspose.controller.word;

import com.zhoubyte.scorpioaspose.controller.UploadController;
import com.zhoubyte.scorpioaspose.dto.OfferDto;
import com.zhoubyte.scorpioaspose.service.WordService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Word -> 文字识别 -数据填充-> 新的Word
 */
@RestController
@RequestMapping(value = "/word/detect")
public class WordDetectController extends UploadController<WordService> {

    public WordDetectController() {
        super("DOCX");
    }

    @PostMapping(value = "/fill_placeholder")
    public Result<String> fillByPlaceholder(@RequestParam("path") String path,
                                             @RequestBody OfferDto offerDto) {
        String filledPath = service.fillByPlaceholder(path, offerDto);
        return Result.success(filledPath);
    }

    @PostMapping(value = "/fill_bookmark")
    public Result<String> fillByBookmark(@RequestParam("path") String path,
                                          @RequestBody OfferDto offerDto) {
        String filledPath = service.fillByBookmark(path, offerDto);
        return Result.success(filledPath);
    }
}
