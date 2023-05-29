package com.me.demo.entity;

import com.me.demo.dto.MemberFormDto;
import com.me.demo.repository.CartRepository;
import com.me.demo.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@TestPropertySource(locations="classpath:application-test.properties")
class CartTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PersistenceContext
    EntityManager em;

    // 회원 엔티티를 생성하는 메소드를 만든다
    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("test@email.com");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
    public void findCartAndMemberTest(){
        Member member = createMember();
        memberRepository.save(member);
        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        // JPA는 영속성 컨텍스트에 데이터를 저장 후 트랜잭션이 끝날 때 flush()를 호출하여 DB에 반영
        // 회원 엔티티와 장바구니 엔티티를 영속성 컨텍스트에 저장 후 엔티티 매니저로부터 강제로
        // flush()를 호출하여 DB에 반영
        em.flush();

        // JPA는 영속성 컨텍스트로부터 엔티티를 조회 후 영속성 컨텍스트에 엔티티가 없을 경우 DB를 조회
        // 실제 DB에서 장바구니 엔티티를 가지고 올 때 회원 엔티티도 같이 가지고오는지 보기 위해
        // 영속성 컨텍스트를 비워준다
        em.clear();

        // 저장된 장바구니 엔티티를 조회
        Cart savedCart = cartRepository.findById(cart.getId())
                .orElseThrow(EntityNotFoundException::new);
        // 처음에 저장한 member 엔티티의 id와 savedCart에 매핑된 member 엔티티의 id를 비교한다
        assertEquals(savedCart.getMember().getId(), member.getId());
    }


}
