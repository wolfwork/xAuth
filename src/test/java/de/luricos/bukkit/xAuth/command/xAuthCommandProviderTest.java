package de.luricos.bukkit.xAuth.command;

import junit.framework.Assert;
import org.junit.Test;


/**
 * @author lycano
 */
public class xAuthCommandProviderTest {

    protected xAuthCommandProvider provider;

    public xAuthCommandProviderTest() {
        this.initialize();
    }

    public void initialize() {
        this.provider = new xAuthCommandProviderWrapper();
    }

    @Test
    public void testEmptyInitialize() throws Exception {
        Assert.assertEquals(this.provider.getAliasCommand("command"), null);
    }

    @Test
    public void testHasAlias() throws Exception {
        this.initialize();

        xAuthCommandMap commandMap = new xAuthCommandMap("xauth", new xAuthCommandAlias("x"));
        this.provider.addCommandMap("xauth", commandMap);
        Assert.assertTrue(this.provider.hasAlias("x"));
    }

    @Test
    public void testHasCommand() throws Exception {
        this.initialize();

        xAuthCommandMap commandMap = new xAuthCommandMap("xauth", new xAuthCommandAlias("x"));
        this.provider.addCommandMap("xauth", commandMap);

        Assert.assertTrue(commandMap.getCommand().equals("xauth"));
    }

    @Test
    public void testSetAliasCommand() throws Exception {
        this.initialize();

        this.provider.setAliasCommand("x", "xauth");
        Assert.assertTrue(this.provider.hasAlias("x"));
    }

    @Test
    public void testGetAliasCommand() throws Exception {
        this.provider.setAliasCommand("x", "XaUth");
        Assert.assertEquals(this.provider.getAliasCommand("x"), "xauth");
    }

    @Test
    public void testAddCommandMap() throws Exception {
        this.initialize();
        xAuthCommandMap commandMap = new xAuthCommandMap("xAutH", new xAuthCommandAlias("X"));
        this.provider.addCommandMap(commandMap.getCommand(), commandMap);

        Assert.assertTrue(this.provider.hasAlias("x"));
    }

    @Test
    public void testGetAliasCommandMap() throws Exception {
        this.testAddCommandMap();

        Assert.assertEquals(this.provider.getAliasCommand("x"), "xauth");
        Assert.assertNotNull(this.provider.getAliasCommandMap("X"));
    }

    @Test
    public void testGetCommandMappings() throws Exception {
        this.initialize();
        this.testAddCommandMap();

        Assert.assertNotNull(this.provider.getCommandMappings("xauth"));
    }

    @Test
    public void testGetCommandMap() throws Exception {
        this.initialize();
        this.testAddCommandMap();

        // get a commandmap with command xauth, should not be null
        Assert.assertNotNull(this.provider.getCommandMap("xauth"));

        // get a commandmap by using alias. Get the Command that should be xauth
        Assert.assertEquals(this.provider.getCommandMap("xauth").getCommand(), "xauth");
        Assert.assertEquals(this.provider.getCommandMap("xauth").getAlias(), "x");

        this.provider.addCommandMap("info", new xAuthCommandMap("info", new xAuthCommandAlias("profile")));
        Assert.assertEquals(this.provider.lookup("profile").getCommand(), "info");
    }

    @Test
    public void testIsResponsible() throws Exception {
        this.initialize();
        Assert.assertFalse(this.provider.isResponsible("command"));

        this.testAddCommandMap();
        Assert.assertTrue(this.provider.isResponsible("xAutH"));
    }

    public class xAuthCommandProviderWrapper extends xAuthCommandProvider {

        @Override
        public void initialize() {

        }
    }

}