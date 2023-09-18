package appaanjanda.snooping.domain.product.service;

import appaanjanda.snooping.domain.product.entity.product.DigitalProduct;
import appaanjanda.snooping.domain.product.entity.product.FoodProduct;
import appaanjanda.snooping.domain.product.entity.product.FurnitureProduct;
import appaanjanda.snooping.domain.product.entity.product.NecessariesProduct;
import appaanjanda.snooping.domain.product.entity.product.*;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    //인덱스 별 상품 엔티티
    public Class<?> searchProductByIndex(String index) {
        switch (index) {
            case "디지털가전":
                return DigitalProduct.class;
            case "가구":
                return FurnitureProduct.class;
            case "생활용품":
                return NecessariesProduct.class;
            case "식품":
                return FoodProduct.class;
            default:
                throw new IllegalArgumentException("Invalid index");
        }
    }

    // 상품 정보 검색
    public SearchHits<?> searchProductById(String index, String productId) {
        // 반환할 상품 타입
        Class<?> productType = searchProductByIndex(index);

        try {

            // 인덱스랑 id로 상품 정보 요청
            NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("_index", index))
                            .must(QueryBuilders.termQuery("_id", productId))
                    )
                    .withSourceFilter(new FetchSourceFilter(null, null))
                    .build();
            // 검색 결과
            return elasticsearchRestTemplate.search(nativeSearchQuery, productType);
        } catch (Exception e) {
            throw new RuntimeException("요청 에러", e);
        }
    }

    // 상품 가격 검색
//    public SearchHits<?> searchPriceById(String index, String productId){
//
//        // "product_123" => "price_123"
//        String priceId = "price" + productId.split("_")[1];
//
//        // 반환할 상품 타입
//        Class<?> productType = searchDocumentByIndex(index);
//
//        // index, id로 상품 찾아서 시간 내림차순 정렬
//        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.termQuery("_index", index))
//                        .must(QueryBuilders.termQuery("_id", priceId))
//                )
//                .withSort(Sort.by(Sort.Order.desc("price_history.timestamp")))
//                .build();// 정렬
//
//        // 검색결과
//        SearchHits<Map> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, Map.class);
//    }


}
