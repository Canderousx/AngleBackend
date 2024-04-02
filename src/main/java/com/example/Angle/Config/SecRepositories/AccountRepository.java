package com.example.Angle.Config.SecRepositories;


import com.example.Angle.Config.Models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account>findByUsername(String username);

    Optional<Account>findByEmail(String email);

    Optional<Account>findByActive(boolean active);



}
