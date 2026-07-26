package com.zhoubyte.scorpioaspose.controller.pdf;

import com.zhoubyte.scorpioaspose.controller.UploadController;
import com.zhoubyte.scorpioaspose.service.PdfService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/pdf_to_word")
public class PdfToWordController extends UploadController<PdfService> {

    public PdfToWordController() {
        super("PDF");
    }

    @GetMapping(value = "/transfer")
    public Result<String> pdfToWord(@RequestParam("path") String path) {
        String word = service.toWord(path);
        return Result.success(word);
    }
}
