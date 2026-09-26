package br.com.trackflow.auth.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.trackflow.auth.role.entity.Role;
import br.com.trackflow.auth.role.entity.RoleName;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}