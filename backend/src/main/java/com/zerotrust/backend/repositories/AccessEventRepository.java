package com.zerotrust.backend.repositories;

import com.zerotrust.backend.entities.AccessEvent;
import com.zerotrust.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccessEventRepository extends JpaRepository<AccessEvent, UUID> {
    List<AccessEvent> findByUser(User user);

}
