package com.zhoubyte.core.controller;

import com.zhoubyte.core.service.RagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping(value = "/import")
    public void importData(@RequestParam("message") String message) {
        ragService.importData(message);
    }
}
