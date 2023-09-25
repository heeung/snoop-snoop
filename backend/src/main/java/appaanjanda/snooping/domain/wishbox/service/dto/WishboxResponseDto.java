package appaanjanda.snooping.domain.wishbox.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WishboxResponseDto {
    private String productId;
    private String productName;
    private String productImage;
    private int price;

    @Builder
    public WishboxResponseDto(String productId, String productName, String productImage, int price) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.price = price;
    }
}