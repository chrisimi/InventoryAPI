package com.chrisimi.inventoryapi.sample;

import com.chrisimi.inventoryapi.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TestInventory extends Inventory implements IInventoryAPI {

    /*
    you have to write InventoryAPI.initialize(this) in JavaPlugin (main) class to make the InventoryAPI work

     */


    public TestInventory(Player player, Plugin plugin)
    {
        super(player, 9*3, plugin, "test inventory");

        //DO NOT FORGET THIS
        //the method will register all events in this menu
        this.addEvents(this);


    }

    //to create a event method, you have to use the EventMethodAnnotation and your needed parameter
    @EventMethodAnnotation
    public void onClick(ClickEvent event)
    {
        if(event.getClicked().getType() == Material.IRON_INGOT)
        {
            player.sendMessage("you clicked on a iron ingot");
            waitforChatInput(player);
        }
    }
    @EventMethodAnnotation
    public void onChat(ChatEvent event)
    {
        if(event.getMessage().equals("yes"))
        {
            player.sendMessage("you wrote yes in the chat");
        }
    }
}
