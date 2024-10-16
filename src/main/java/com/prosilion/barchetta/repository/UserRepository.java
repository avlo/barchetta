package com.prosilion.barchetta.repository;

import com.prosilion.barchetta.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//  Optional<User> findByNostrPubKey(@NonNull String nostrPubKey);
}
