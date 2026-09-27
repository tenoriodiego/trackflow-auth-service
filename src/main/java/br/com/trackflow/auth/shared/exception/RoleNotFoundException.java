package br.com.trackflow.auth.shared.exception;

import br.com.trackflow.auth.role.entity.RoleName;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(RoleName roleName) {
        super("Role não encontrada: " + roleName);
    }
}