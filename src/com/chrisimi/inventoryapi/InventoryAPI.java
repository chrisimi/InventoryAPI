package com.chrisimi.inventoryapi;

import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryAPI implements Listener{

	private static JavaPlugin pluginInstance;
	
	private static HashMap<String, Inventory> registeredInventories = new HashMap<>();
	
	/**
	 * Create an Inventory 
	 * @param player Player who should own the inventory
	 * @param size how big the inventory should be
	 * @param plugin JavaPlugin instance
	 * @param inventoryName the name of the inventory
	 * @return created Inventory
	 */
	public static Inventory createInventory(Player player, int size, JavaPlugin plugin, String inventoryName) {
		@SuppressWarnings("deprecation")
		Inventory inventory = new Inventory(player, size, plugin, inventoryName);
		
		return inventory;
	}
	/**
	 * Initiate the whole InventoryAPI 
	 * @param plugin {@link JavaPlugin} instance of the plugin
	 */
	public static void initiate(JavaPlugin plugin)
	{
		pluginInstance = plugin;
		plugin.getServer().getPluginManager().registerEvents(new InventoryAPI(), plugin);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) 
	{
		Inventory.inventoryClick(event);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		Inventory.inventoryCloseEvent(event);
	}
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event)
	{
		Inventory.chatInput(event, pluginInstance);
	}

	
	public static String convertItemStackToString(ItemStack itemStack) {
		YamlConfiguration config = new YamlConfiguration();
		config.set("i", itemStack);
		return config.saveToString();
	}
	public static ItemStack convertStringToItemStack(String string) {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(string);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		return config.getItemStack("i", null);
	}
	public static Inventory getInventory(String UUID) {
		return registeredInventories.get(UUID);
	}
}
