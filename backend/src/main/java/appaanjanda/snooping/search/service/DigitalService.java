package appaanjanda.snooping.search.service;

import appaanjanda.snooping.product.entity.product.DigitalProduct;
import appaanjanda.snooping.search.repository.DigitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalService {
    private final DigitalRepository digitalRepository;

    public DigitalProduct getById(String id) {
        DigitalProduct digitalProduct = digitalRepository.findById(id).get();
        log.info(digitalProduct.getProvider());
        digitalProduct.setProvider("SSAFY");
        digitalRepository.save(digitalProduct);
        log.info(digitalProduct.getProvider());
        return digitalProduct;
    }

    public DigitalProduct getByName(String name) {
        DigitalProduct digitalProduct = digitalRepository.findByProductName(name).get();
        log.info(digitalProduct.getProvider());
        digitalProduct.setProvider("SSAFYSSAFY");
        digitalRepository.save(digitalProduct);
        log.info(digitalProduct.getProvider());
        return digitalProduct;
    }
}
