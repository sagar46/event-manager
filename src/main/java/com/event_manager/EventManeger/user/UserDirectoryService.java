package com.event_manager.EventManeger.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event_manager.EventManeger.auth.dto.UserResponse;
import com.event_manager.EventManeger.common.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDirectoryService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Transactional(readOnly = true)
	public UserResponse getPublicProfile(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User not found"));
		return userMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public List<UserSummary> listCrewForContributor(User contributor) {
		return userRepository.findByAffiliatedContributorOrderByFullNameAsc(contributor).stream()
				.map(UserSummary::from)
				.toList();
	}
}
