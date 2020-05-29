package com.chrisimi.inventoryapi;

import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryAPI {

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
	
	
	public static void InventoryAPIInventoryClickEvent(InventoryClickEvent event) {
		Inventory.inventoryClick(event);
	}
	public static void InventoryCloseEvent(InventoryCloseEvent event) {
		Inventory.inventoryCloseEvent(event);
	}
	public static void InventoryAPIPlayerChatEvent(AsyncPlayerChatEvent event, JavaPlugin plugin) {
		Inventory.chatInput(event, plugin);
	}
}
