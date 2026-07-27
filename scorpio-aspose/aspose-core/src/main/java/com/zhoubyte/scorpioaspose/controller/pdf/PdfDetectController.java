package com.zhoubyte.scorpioaspose.controller.pdf;

import com.zhoubyte.scorpioaspose.controller.UploadController;
import com.zhoubyte.scorpioaspose.service.PdfService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * PDF -> 文字识别 -填充数据->新的PDF
 */
@RestController
@RequestMapping(value = "/pdf/detect")
public class PdfDetectController extends UploadController<PdfService> {

    public PdfDetectController() {
        super("PDF");
    }

    @GetMapping(value = "/transfer")
    public Result<String> transfer(@RequestParam("path") String path) {
        String fill = service.fill(path);
        return Result.success(fill);
    }
}
