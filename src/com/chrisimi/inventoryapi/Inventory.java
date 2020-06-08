package com.chrisimi.inventoryapi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Inventory {

	private class Struct {
		Method method;
		IInventoryAPI obj;
	}
	

	private static HashMap<Player, Inventory> waitingForChatInput = new HashMap<>();
	private static HashMap<Inventory, HashMap<EventType, Struct>> registeredEvents = new HashMap<>();

	private final org.bukkit.inventory.Inventory bukkitInventory;
	private final Player player;


	private final UUID InventoryID;
	private final JavaPlugin plugin;

	@Deprecated
	public Inventory(Player player, int size, JavaPlugin plugin, String inventoryName) {
		this.player = player;
		this.bukkitInventory = Bukkit.createInventory(player, size, inventoryName);
		this.InventoryID = UUID.randomUUID();
		this.plugin = plugin;
	}
	/**
	 * Register EventMethods from the class
	 * @param inventoryAPI
	 */
	public void addEvents(IInventoryAPI inventoryAPI) {
		
		/**
		 * first it goes through all methods and search if there is a ChatEvent or ClickEvent as Parameter.
		 * Afterwards it'll add it to the Map and from there it is registered in the System
		 * 
		 * 
		 * A valid Event method must not have more than 1 parameters. 
		 * The name of the method is ignored (only the instance of the method from reflection will be saved)
		 */
		
		for(Method method : inventoryAPI.getClass().getMethods()) {
			if(!(method.getAnnotations().length == 1)) continue;

			if(method.getAnnotations()[0].annotationType().equals(EventMethodAnnotation.class)) {
				if(method.getParameters().length == 1 && method.getParameters()[0].getType().equals(ChatEvent.class)) {
					HashMap<EventType, Struct> hashMap = new HashMap<>();
					Struct struct = new Struct();
					struct.method = method;
					struct.obj = inventoryAPI;
					hashMap.put(EventType.CHATINPUT, struct);

					if(registeredEvents.containsKey(this))
					{
						HashMap<EventType, Struct> map = registeredEvents.get(this);
						map.put(EventType.CHATINPUT, struct);
						registeredEvents.put(this, map);
					}
					else
						registeredEvents.put(this, hashMap);
				}
				else if(method.getParameters().length == 1 && method.getParameters()[0].getType().equals(ClickEvent.class))
				{
					HashMap<EventType, Struct> hashMap = new HashMap<>();
					Struct struct = new Struct();
					struct.method = method;
					struct.obj = inventoryAPI;
					hashMap.put(EventType.INVENTORYCLICK, struct);

					if(registeredEvents.containsKey(this))
					{
						HashMap<EventType, Struct> map = registeredEvents.get(this);
						map.put(EventType.INVENTORYCLICK, struct);
						registeredEvents.put(this, map);
					}
					else
						registeredEvents.put(this, hashMap);
				}
			}
		}
	}
	/**
	 * Register chat input for Player
	 * @param player who should write something in chat
	 */
	public void waitforChatInput(final Player player) {
		if(player == null) return;
		
		waitingForChatInput.put(player, this);
		this.closeInventory();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				if(waitingForChatInput.containsKey(player))
					waitingForChatInput.remove(player);
			}
		}, 20*60);
	}
	/**
	 * remove all registered events from Inventory
	 */
	public void removeAllEvents() {
		registeredEvents.remove(this);
	}
	/**
	 * Get the bukkit inventory linked with this inventory
	 * @return bukkit inventory
	 */
	public org.bukkit.inventory.Inventory getInventory() {
		return bukkitInventory;
	}
	/**
	 * close the bukkit inventory
	 */
	public void closeInventory() {
		player.closeInventory();
	}
	/**
	 * open the bukkit inventory
	 */
	public void openInventory() {
		player.closeInventory();
		player.openInventory(bukkitInventory);
	}

	public static void inventoryClick(InventoryClickEvent event) {
		
		/*
		 * The call of the method comes from the InventoryAPI which registered the Event by EventHandler
		 * 
		 * First the inventory from map and then check if it's a valid inventory.
		 * If everything is clear then the registered ClickEvent method will be invoked by reflection
		 * 
		 * if ItemStack is null the method won't be invoked
		 */
		
		org.bukkit.inventory.Inventory inv = event.getInventory();
		Inventory inventoryAPI = getInventoryFromBukkitInventory(inv);
		ItemStack getClicked = event.getCurrentItem();
		if(getClicked == null) return;

		if(registeredEvents.containsKey(inventoryAPI))
		{
			ClickEvent clickEvent = new ClickEvent(getClicked, inv, inventoryAPI);
			try
			{
				HashMap<EventType, Struct> data = registeredEvents.get(inventoryAPI);
				Method method = data.get(EventType.INVENTORYCLICK).method;
				if(method == null)
				{
					return;
				}

				method.invoke(data.get(EventType.INVENTORYCLICK).obj, clickEvent);
				event.setCancelled(true);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	public static void inventoryCloseEvent(InventoryCloseEvent event) {

		/* Have to be rewritten
		 * TODO 
		 * A idea to clean the inventories
		 * 
		 */
		
		/*
		org.bukkit.inventory.Inventory inv = event.getInventory();
		synchronized (registeredEvents) {
			for(Map.Entry<Inventory, HashMap<EventType, Struct>> entry : registeredEvents.entrySet()) {
				if(entry.getKey().bukkitInventory.equals(inv)) {
					entry.getKey().removeAllEvents();
					Bukkit.getLogger().info("removed all events from closing inv");
				}
			}
		}
		*/

	}
	public static void chatInput(final AsyncPlayerChatEvent event, JavaPlugin plugin) {
		
		/* First check if a inventory registered a chat event for that player and if that's true
		 * invoke the ChatEvent method which the inventory registered by reflection
		 * 
		 */
		
		if(waitingForChatInput.containsKey(event.getPlayer())) event.setCancelled(true);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				Player player = event.getPlayer();
				Inventory inventory = waitingForChatInput.remove(player);
				if(inventory == null) {
					return;
				}
				if(registeredEvents.containsKey(inventory)) {

					ChatEvent chatEvent = new ChatEvent(player, event.getMessage(), inventory);
					HashMap<EventType, Struct> hashMap = registeredEvents.get(inventory);
					Method method = hashMap.get(EventType.CHATINPUT).method;
					if(method == null) {
						return;
					}

					try {
						method.invoke(hashMap.get(EventType.CHATINPUT).obj, chatEvent);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
				}
			}
		});
	}
	/**
	 * get the InventoryAPI Inventory from a bukkit Inventory
	 * @param inventory {@link org.bukkit.inventory.Inventory} instance of the Inventory
	 * @return {@link Inventory} instance of the InventoryAPI inventory
	 */
	private static Inventory getInventoryFromBukkitInventory(org.bukkit.inventory.Inventory inventory) {
		synchronized (registeredEvents) {
			for(Map.Entry<Inventory, HashMap<EventType, Struct>> entry : registeredEvents.entrySet()) {
				if(entry.getKey().bukkitInventory.equals(inventory)) return entry.getKey();
			}
		}
		return null;
	}
}
