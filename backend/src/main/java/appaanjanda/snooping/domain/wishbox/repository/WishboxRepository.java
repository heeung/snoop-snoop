package appaanjanda.snooping.domain.wishbox.repository;

import appaanjanda.snooping.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import appaanjanda.snooping.domain.wishbox.entity.Wishbox;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WishboxRepository extends JpaRepository<Wishbox, Long> {

    @Query("SELECT w.productCode FROM Wishbox w WHERE w.member.id = :memberId")
    Set<String> findProductById(Long memberId);

    @Query("SELECT w.productCode FROM Wishbox w")
    Set<String> findAllProductCode();

    @Query("SELECT w FROM Wishbox w WHERE w.productCode = :productCode")
    List<Wishbox> findWishboxByProductCode(String productCode);

    List<Wishbox> findByMember(Member member);

    Optional<Wishbox> findByProductCodeAndMember(String productCode, Member member);
}
