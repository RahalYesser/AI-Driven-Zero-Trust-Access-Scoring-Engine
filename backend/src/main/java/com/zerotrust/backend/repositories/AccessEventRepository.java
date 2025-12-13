package com.zerotrust.backend.repositories;

import com.zerotrust.backend.entities.AccessEvent;
import com.zerotrust.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessEventRepository extends JpaRepository<AccessEvent, Long> {
    List<AccessEvent> findByUser(User user);

}
