package com.madeeasy.command.api.query.repository;

import com.madeeasy.command.api.query.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
