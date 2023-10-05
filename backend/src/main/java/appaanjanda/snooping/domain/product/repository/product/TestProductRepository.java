package appaanjanda.snooping.domain.product.repository.product;

import appaanjanda.snooping.domain.product.entity.product.Product;
import appaanjanda.snooping.domain.product.entity.product.TestProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestProductRepository extends JpaRepository<TestProduct, Long> {

}
