package appaanjanda.snooping.domain.product.entity.product;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;

@Document(indexName = "test2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ElasticsearchProduct {

    @Id
    private String id;

    @Field(name = "code", type = FieldType.Text)
    private String code;

    @Field(name = "major_category", type = FieldType.Text)
    private String majorCategory;

    @Field(name = "minor_category", type = FieldType.Text)
    private String minorCategory;

    @Field(name = "provider", type = FieldType.Text)
    private String provider;

    @Field(name = "price", type = FieldType.Integer)
    private int price;

    @Field(name = "product_name", type = FieldType.Text)
    private String productName;

    @Field(name = "product_link", type = FieldType.Text)
    private String productLink;

    @Field(name = "product_image", type = FieldType.Text)
    private String productImage;

    @Field(name = "@timestamp", type = FieldType.Date)
    private String timestamp;

    @Builder
    public ElasticsearchProduct(String code, String majorCategory, String minorCategory, String provider, int price, String productName, String productLink, String productImage, String timestamp) {
        this.code = code;
        this.majorCategory = majorCategory;
        this.minorCategory = minorCategory;
        this.provider = provider;
        this.price = price;
        this.productName = productName;
        this.productLink = productLink;
        this.productImage = productImage;
        this.timestamp = timestamp;
    }
}
