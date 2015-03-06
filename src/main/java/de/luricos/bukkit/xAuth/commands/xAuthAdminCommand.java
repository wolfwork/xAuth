package de.luricos.bukkit.xAuth.commands;

/**
 * @author lycano
 */
public abstract class xAuthAdminCommand extends xAuthCommand {

    private boolean result = false;

    public void setResult(boolean result) {
        this.result = result;
    }

    public boolean getResult() {
        return this.result;
    }
}
