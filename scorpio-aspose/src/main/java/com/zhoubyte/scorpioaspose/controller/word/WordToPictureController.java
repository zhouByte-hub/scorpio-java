package com.zhoubyte.scorpioaspose.controller.word;

import com.zhoubyte.scorpioaspose.controller.UploadController;
import com.zhoubyte.scorpioaspose.service.WordService;
import com.zhoubyte.scorpioaspose.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Word -> picture
 */
@RestController
@RequestMapping(value = "/word_to_picture")
public class WordToPictureController extends UploadController<WordService> {

    public WordToPictureController() {
        super("WORD");
    }

    @GetMapping(value = "/transfer")
    public Result<List<String>> wordToPictures(@RequestParam("path") String path) {
        List<String> pictures = service.toPictures(path);
        return Result.success(pictures);
    }

}
