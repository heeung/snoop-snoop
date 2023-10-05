package appaanjanda.snooping;

import appaanjanda.snooping.domain.product.entity.product.ElasticsearchProduct;
import appaanjanda.snooping.domain.product.entity.product.Product;
import appaanjanda.snooping.domain.product.entity.product.TestProduct;
import appaanjanda.snooping.domain.product.repository.product.ElasticsearchProductRepository;
import appaanjanda.snooping.domain.product.repository.product.TestProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DBTest {

    @Autowired
    private TestProductRepository testProductRepository;

    @Autowired
    private ElasticsearchProductRepository elasticsearchProductRepository;

    @Test
    @Transactional
    public void RDBMSTest() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        LocalDateTime start = LocalDateTime.now();
        String formatterStartTime = start.format(formatter);
        System.out.println("시작시간 : " + formatterStartTime);

        for (int i=1; i<=10000; i++) {
            TestProduct product = TestProduct.builder()
                    .code("11상품" + i)
                    .majorCategory("디지털가전")
                    .minorCategory("노트북")
                    .provider("네이버")
                    .price(i)
                    .productName("상품" + i)
                    .productLink("www.test.com")
                    .productImage("image.com")
                    .timestamp(String.valueOf(LocalDateTime.now()))
                    .build();

            testProductRepository.save(product);
        }

        LocalDateTime end = LocalDateTime.now();
        String formatterEndTime = end.format(formatter);
        System.out.println("종료시간 : " + formatterEndTime);

        Duration duration = Duration.between(start, end);
        System.out.println("경과시간 : " + duration.toMillis() + "ms");

    }

    @Test
    @Transactional
    public void ElasticsearchTest() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        LocalDateTime start = LocalDateTime.now();
        String formatterStartTime = start.format(formatter);
        System.out.println("시작시간 : " + formatterStartTime);

//        for (int i=1; i<=10000; i++) {
//            ElasticsearchProduct product = ElasticsearchProduct.builder()
//                    .code("11상품" + i)
//                    .majorCategory("디지털가전")
//                    .minorCategory("노트북")
//                    .provider("네이버")
//                    .price(i)
//                    .productName("상품" + i)
//                    .productLink("www.test.com")
//                    .productImage("image.com")
//                    .timestamp(String.valueOf(LocalDateTime.now()))
//                    .build();
//
//            elasticsearchProductRepository.save(product);
//        }
        int batchSize = 1000;  // 적절한 배치 크기를 설정합니다.
        List<ElasticsearchProduct> products = new ArrayList<>();

        for (int i = 1; i <= 100000; i++) {
            ElasticsearchProduct product = ElasticsearchProduct.builder()
                    .code("11상품" + i)
                    .majorCategory("디지털가전")
                    .minorCategory("노트북")
                    .provider("네이버")
                    .price(i)
                    .productName("상품" + i)
                    .productLink("www.test.com")
                    .productImage("image.com")
                    .timestamp(String.valueOf(LocalDateTime.now()))
                    .build();
            products.add(product);

            // 배치 크기만큼 채워지면 저장하고 리스트를 초기화합니다.
            if (i % batchSize == 0 || i == 100000) {
                elasticsearchProductRepository.saveAll(products);
                products.clear();
            }
        }

        LocalDateTime end = LocalDateTime.now();
        String formatterEndTime = end.format(formatter);
        System.out.println("종료시간 : " + formatterEndTime);

        Duration duration = Duration.between(start, end);
        System.out.println("경과시간 : " + duration.toMillis() + "ms");

    }
}
