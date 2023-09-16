package appaanjanda.snooping.search.service;

import appaanjanda.snooping.product.entity.product.BaseProduct;
import appaanjanda.snooping.product.entity.product.DigitalProduct;
import appaanjanda.snooping.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final ProductSearchService productSearchService;

    // 카테고리로 상품 검색
    // TODO 페이징 처리
    public List<?> searchProductByCategory(String index, String minor) {
        // 반환할 상품 타입
        Class<?> productType = productSearchService.searchProductByIndex(index);

        // 쿼리 작성
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("_index", index))
                        .must(QueryBuilders.termQuery("minor_category.keyword", minor))
                )
                .withSourceFilter(new FetchSourceFilter(null, null))
                .withPageable(Pageable.ofSize(100)) // 100 개
                .build();

        // 검색 결과
        SearchHits<?> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, productType);

        // content만 추출
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

    }

    // 키워드로 상품 검색
    public List<?> searchProductByKeyword(String keyword) {
        List<Object> totalResults = new ArrayList<>();
        String[] indices = {"디지털가전", "가구", "생활용품", "식품"}; // 검색할 인덱스들

        for (String index : indices) {
            Class<?> productType = productSearchService.searchProductByIndex(index);

            NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.fuzzyQuery("product_name", keyword))
                    .build();

            SearchHits<?> searchHits = elasticsearchRestTemplate.search(searchQuery, productType);

            // content만 추출해서 전체 결과에 추가
            totalResults.addAll(
                    searchHits.getSearchHits().stream()
                            .map(SearchHit::getContent)
                            .collect(Collectors.toList())
            );
        }
        return totalResults;
    }
}
