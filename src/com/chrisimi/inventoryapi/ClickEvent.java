package com.chrisimi.inventoryapi;

import org.bukkit.inventory.ItemStack;

public class ClickEvent
{
	private final ItemStack clicked;
	private final org.bukkit.inventory.Inventory bukkitInventory;
	private final Inventory inventory;
	public ClickEvent(ItemStack clicked, org.bukkit.inventory.Inventory bukkitInventory, Inventory inventory)
	{
		this.clicked = clicked;
		this.bukkitInventory = bukkitInventory;
		this.inventory = inventory;
	}
	
	public ItemStack getClicked() { return clicked;}
	public org.bukkit.inventory.Inventory getBukkitInventory() { return bukkitInventory;}
	public Inventory getInventoryAPI() {return inventory;}
}
