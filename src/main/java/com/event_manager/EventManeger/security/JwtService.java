package com.event_manager.EventManeger.security;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.event_manager.EventManeger.user.Role;
import com.event_manager.EventManeger.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public String generateToken(User user) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(jwtProperties.getExpiration());

		List<String> roles = user.getRoles().stream().map(Role::name).toList();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.getIssuer())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(user.getPhone())
				.claim("userId", user.getId())
				.claim("phone", user.getPhone())
				.claim("roles", roles)
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long expiresInSeconds() {
		return jwtProperties.getExpiration().toSeconds();
	}
}
