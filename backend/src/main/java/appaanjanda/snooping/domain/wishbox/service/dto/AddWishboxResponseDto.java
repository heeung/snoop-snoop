package appaanjanda.snooping.domain.wishbox.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddWishboxResponseDto {
    private boolean wishYn;

    @Builder
    public AddWishboxResponseDto(boolean wishYn) {
        this.wishYn = wishYn;
    }
}
