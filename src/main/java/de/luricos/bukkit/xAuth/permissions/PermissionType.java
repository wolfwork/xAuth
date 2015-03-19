package de.luricos.bukkit.xAuth.permissions;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lycano
 */
public enum PermissionType {
    SECURITY_USE_ADMIN_COMMAND("security.player.use.admin.command"),
    SECURITY_DENY_ADMIN_COMMAND_TARGET_PREFIX("security.deny.command.target"),
    SECURITY_BYPASS_ACCOUNT_LIMIT("security.player.account.bypass.limit");

    /** The permission node. */
    private final String permissionNode;
    /** The Constant permissionMap. */
    private static final Map<String, PermissionType> permissionMap = new HashMap<String, PermissionType>();

    static {
        for (final PermissionType type : EnumSet.allOf(PermissionType.class)) {
            permissionMap.put(type.permissionNode, type);
        }
    }

    private PermissionType(final String permissionNode) {
        this.permissionNode = permissionNode;
    }

    public String getPermissionNode() {
        return this.permissionNode;
    }

    /**
     * From permission node.
     *
     * @param permissionNode
     *            the permission node
     * @return the permission type
     */
    public static PermissionType fromString(final String permissionNode) {
        return permissionMap.get(permissionNode);
    }
}
