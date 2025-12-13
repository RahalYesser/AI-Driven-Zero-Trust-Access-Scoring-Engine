package com.zerotrust.backend.repositories;

import com.zerotrust.backend.entities.Device;
import com.zerotrust.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByUser(User user);

}
