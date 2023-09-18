package appaanjanda.snooping.search.controller;

import appaanjanda.snooping.product.entity.product.DigitalProduct;
import appaanjanda.snooping.search.service.DigitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/digital")
public class DigitalController {
    private final DigitalService digitalService;

    @PostMapping("/test/id")
    public ResponseEntity<DigitalProduct> getById(@RequestParam String id) {
        return ResponseEntity.ok(digitalService.getById(id));
    }

    @PostMapping("/test/name")
    public ResponseEntity<DigitalProduct> getByName(@RequestParam String name) {
        return ResponseEntity.ok(digitalService.getByName(name));
    }
}
