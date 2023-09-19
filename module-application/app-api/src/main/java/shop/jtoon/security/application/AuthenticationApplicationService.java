package shop.jtoon.security.application;

import static shop.jtoon.util.SecurityConstant.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import shop.jtoon.dto.MemberDto;
import shop.jtoon.security.request.LoginReq;
import shop.jtoon.security.service.AuthenticationService;
import shop.jtoon.security.service.JwtInternalService;
import shop.jtoon.service.MemberDomainService;

@Service
@RequiredArgsConstructor
public class AuthenticationApplicationService implements AuthenticationService {

	private final MemberDomainService memberDomainService;
	private final JwtInternalService jwtInternalService;

	public String[] loginMember(LoginReq loginReq) {
		memberDomainService.localLoginMember(loginReq.toDto());
		String accessToken = jwtInternalService.generateAccessToken(loginReq.email());
		String refreshToken = jwtInternalService.generateRefreshToken();

		return new String[] {accessToken, refreshToken};
	}

	@Override
	public Authentication getAuthentication(String claimsEmail) {
		MemberDto memberDto = memberDomainService.findMemberDtoByEmail(claimsEmail);

		return new UsernamePasswordAuthenticationToken(memberDto, BLANK,
			List.of(new SimpleGrantedAuthority(memberDto.role().toString())));
	}
}