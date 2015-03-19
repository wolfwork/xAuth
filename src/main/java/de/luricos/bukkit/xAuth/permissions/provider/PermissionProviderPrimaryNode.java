package de.luricos.bukkit.xAuth.permissions.provider;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lycano
 */
public enum PermissionProviderPrimaryNode {
    XAUTH("xauth", "xAuth"),
    GUEST("guest", "Guest");

    /** The permission node. */
    private final String name;
    private final String prettyName;

    /** The Constant permissionMap. */
    private static final Map<String, PermissionProviderPrimaryNode> nodeMap = new HashMap<String, PermissionProviderPrimaryNode>();

    static {
        for (final PermissionProviderPrimaryNode type : EnumSet.allOf(PermissionProviderPrimaryNode.class)) {
            nodeMap.put(type.name, type);
        }
    }

    private PermissionProviderPrimaryNode(final String name, final String prettyName) {
        this.name = name;
        this.prettyName = prettyName;
    }

    public String getName() {
        return this.name;
    }

    public String getPrettyName() {
        return this.prettyName;
    }

    /**
     * From Primary node.
     *
     * @param primaryNode
     *            the primary node as string
     * @return PrimaryNode type
     */
    public static PermissionProviderPrimaryNode fromString(final String primaryNode) {
        return nodeMap.get(primaryNode);
    }
}
