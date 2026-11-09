package com.cartoon.api.auth;

public record UsuarioAutenticado(Long id, String email, String role) {

    public boolean isSuperAdmin() {
        return hasRole("SUPERADMIN");
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isMecanico() {
        return hasRole("MECANICO");
    }

    public boolean hasRole(String targetRole) {
        if (role == null || targetRole == null) {
            return false;
        }
        String normalizedRole = role.startsWith("ROLE_") ? role.substring(5) : role;
        String normalizedTarget = targetRole.startsWith("ROLE_") ? targetRole.substring(5) : targetRole;
        return normalizedRole.equalsIgnoreCase(normalizedTarget);
    }
}
