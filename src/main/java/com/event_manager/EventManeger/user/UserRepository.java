package com.event_manager.EventManeger.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByPhone(String phone);

	boolean existsByPhone(String phone);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("select distinct u from User u join u.roles r where r = :role order by u.fullName")
	List<User> findAllByRole(@Param("role") Role role);

	@Query("select u from User u join u.roles r where u.id = :id and r = :role")
	Optional<User> findByIdAndRole(@Param("id") Long id, @Param("role") Role role);

	List<User> findByAffiliatedContributorOrderByFullNameAsc(User contributor);
}
