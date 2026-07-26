package com.zhoubyte.scorpioaspose.controller.word;

import com.zhoubyte.scorpioaspose.controller.UploadController;
import com.zhoubyte.scorpioaspose.service.WordService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Word -> Pdf
 */
@RestController
@RequestMapping(value = "/word_to_pdf")
public class WordToPdfController extends UploadController<WordService> {

    public WordToPdfController() {
        super("WORD");
    }

    @GetMapping(value = "/transfer")
    public Result<String> wordToPdf(@RequestParam("path") String path) {
        String pdfPath = service.toPdf(path);
        return Result.success(pdfPath);
    }

}
