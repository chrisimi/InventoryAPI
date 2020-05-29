package com.chrisimi.inventoryapi;

import org.bukkit.entity.Player;
/**
 * ChatEvent
 * 
 * When someone wrote something in the chat while he is on the List, the EventMethod will be called with this as parameter
 * 
 * @author chrisimi
 *
 */
public class ChatEvent {

	protected final Player player;
	protected final String message;
	protected final Inventory inventory;
	
	public ChatEvent(Player player, String message, Inventory inventory) {
		this.player = player;
		this.message = message;
		this.inventory = inventory;
	}
	/**
	 * 
	 * @return player who wrote in the chat
	 */
	public Player getPlayer() {return player;}
	/**
	 * 
	 * @return message which the player wrote in chat
	 */
	public String getMessage() {return message;}
	/**
	 * 
	 * @return Inventory on which the event was registered
	 */
	public Inventory getInventory() {return inventory;}
}
