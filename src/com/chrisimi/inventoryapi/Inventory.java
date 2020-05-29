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
		Bukkit.getLogger().info("addEvents method");
		for(Method method : inventoryAPI.getClass().getMethods()) {
			if(!(method.getAnnotations().length == 1)) continue;

			Bukkit.getLogger().info("found Method with any annotation");
			if(method.getAnnotations()[0].annotationType().equals(EventMethodAnnotation.class)) {
				Bukkit.getLogger().info("found Annotation type");
				Bukkit.getLogger().info(method.getName() + ":  length of Parameters: " + method.getParameterCount());
				Bukkit.getLogger().info(method.getName() + ": type of parameter:  " + method.getParameters()[0].getType().getClass().toString());
				if(method.getParameters().length == 1 && method.getParameters()[0].getType().equals(ChatEvent.class)) {
					Bukkit.getLogger().info("method has correct Parameters");
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
					Bukkit.getLogger().info("added chatEventMethod ");
				}
				else if(method.getParameters().length == 1 && method.getParameters()[0].getType().equals(ClickEvent.class))
				{
					Bukkit.getLogger().info("method has correct parameters");
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
					Bukkit.getLogger().info("added inventoryclick method");
				}
			}
		}
	}
	/**
	 * Register chat input for Player
	 * @param player who should write something in chat
	 */
	public void waitforChatInput(final Player player) {
		waitingForChatInput.put(player, this);
		this.closeInventory();
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				if(waitingForChatInput.containsKey(player))
					waitingForChatInput.remove(player);
				Bukkit.getLogger().info("removed waitingForchat for Player: " + player.getName());
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
		Bukkit.getLogger().info("click " + registeredClickEvents.size());

		org.bukkit.inventory.Inventory inv = event.getInventory();
		ItemStack getClicked = event.getCurrentItem();
		if(getClicked == null) return;

		synchronized (registeredClickEvents) {
			for(Map.Entry<Inventory, HashMap<ItemStack, BukkitRunnable>> entry : registeredClickEvents.entrySet()) {

				Inventory inventory = entry.getKey();
				Bukkit.getLogger().info("found entries: " + entry.getValue().size());
				for(Map.Entry<ItemStack, BukkitRunnable> entries : entry.getValue().entrySet()) {
					Bukkit.getLogger().info(entries.getKey().toString() + " - " + entries.getValue().toString());
				}
				Bukkit.getLogger().info(getClicked.toString());
				if(!(inventory.bukkitInventory.equals(inv))) {
					Bukkit.getLogger().info("not equal inventory");
					continue;
				}

				BukkitRunnable runnable = entry.getValue().get(getClicked);
				if(runnable != null) {
					runnable.runTask(inventory.plugin);
					event.setCancelled(true);
					Bukkit.getLogger().info("ausf�hren");
				} else {
					Bukkit.getLogger().info("runnable is null");
				}
				return;
			}
		}
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
					Bukkit.getLogger().info("method is null");
					return;
				}

				method.invoke(data.get(EventType.INVENTORYCLICK).obj, clickEvent);
				Bukkit.getLogger().info("invoked");
				event.setCancelled(true);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	public static void inventoryCloseEvent(InventoryCloseEvent event) {

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
		if(waitingForChatInput.containsKey(event.getPlayer())) event.setCancelled(true);

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				Bukkit.getLogger().info("found entries: " + waitingForChatInput.size());
				for(Map.Entry<Player, Inventory> entries : waitingForChatInput.entrySet()) {
					Bukkit.getLogger().info(entries.getKey().getName() + " - " + entries.getValue().player.getName());
				}
				Player player = event.getPlayer();
				Inventory inventory = waitingForChatInput.remove(player);
				if(inventory == null) {
					Bukkit.getLogger().info("inventory is null");
					return;
				}
				Bukkit.getLogger().info("registeredEvents: " + registeredEvents.size());
				if(registeredEvents.containsKey(inventory)) {

					Bukkit.getLogger().info("Found waitingForChat");
					ChatEvent chatEvent = new ChatEvent(player, event.getMessage(), inventory);
					HashMap<EventType, Struct> hashMap = registeredEvents.get(inventory);
					Method method = hashMap.get(EventType.CHATINPUT).method;
					if(method == null) {
						Bukkit.getLogger().info("method is null");
						return;
					}

					try {
						method.invoke(hashMap.get(EventType.CHATINPUT).obj, chatEvent);
						Bukkit.getLogger().info("invoked");
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Bukkit.getLogger().info("no registered event found");
				}
			}
		});
	}
	private static Inventory getInventoryFromBukkitInventory(org.bukkit.inventory.Inventory inventory) {
		synchronized (registeredEvents) {
			for(Map.Entry<Inventory, HashMap<EventType, Struct>> entry : registeredEvents.entrySet()) {
				if(entry.getKey().bukkitInventory.equals(inventory)) return entry.getKey();
			}
		}
		return null;
	}
}
