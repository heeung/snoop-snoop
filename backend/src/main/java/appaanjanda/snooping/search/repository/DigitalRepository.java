package appaanjanda.snooping.search.repository;

import appaanjanda.snooping.product.entity.product.DigitalProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Optional;

public interface DigitalRepository extends ElasticsearchRepository<DigitalProduct, String> {
    Optional<DigitalProduct> findByProductName(String name);
}
