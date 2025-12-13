package com.zerotrust.backend.repositories;

import com.zerotrust.backend.entities.Device;
import com.zerotrust.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUser(User user);

}
