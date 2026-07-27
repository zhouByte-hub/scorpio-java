package com.zhoubyte.scorpioaspose.controller.pdf;

import com.zhoubyte.scorpioaspose.controller.UploadController;
import com.zhoubyte.scorpioaspose.service.PdfService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * PDF -> picture
 */
@RestController
@RequestMapping(value = "/pdf_to_picture")
public class PdfToPictureController extends UploadController<PdfService> {

    public PdfToPictureController() {
        super("PDF");
    }

    @GetMapping(value = "/transfer")
    public Result<List<String>> toPicture(@RequestParam("path") String path, HttpServletResponse response) {
        List<String> pictures = service.toPictures(path);
        return Result.success(pictures);
    }
}
