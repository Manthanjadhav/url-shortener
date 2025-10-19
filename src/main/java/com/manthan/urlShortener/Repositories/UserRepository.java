package com.manthan.urlShortener.Repositories;

import com.manthan.urlShortener.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findByUserName(String username);
}
