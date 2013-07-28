/**
 * 
 */
package org.evilco.bukkit.DispenserRefill.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.evilco.bukkit.DispenserRefill.DispenserRefillPlugin;
import org.evilco.bukkit.DispenserRefill.database.InfiniteContainer;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bukkit.BukkitPlayer;


/**
 * @author		Johannes Donath
 * @copyright		2012 Evil-Co.de <http://www.evil-co.de>
 * @package		org.evilco.bukkit.DispenserRefill
 */
public class GeneralCommands {
	
	/**
	 * Contains the instance of refill plugin.
	 */
	private DispenserRefillPlugin plugin;
	
	/**
	 * Creates a new instance of type GeneralCommands.
	 * @param plugin
	 */
	public GeneralCommands(DispenserRefillPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * The autorefill command.
	 * @param args
	 * @param sender
	 * @throws CommandException
	 */
	@Command(aliases = {"dispenserrefill", "infinitedispenser", "autorefill"}, usage = "[cooldown]", desc = "Switches a dispenser inventory between infinite and normal.", min = 0, max = 1)
	public void autorefill(CommandContext args, CommandSender sender) throws CommandException {
		// wrap player
		BukkitPlayer player = this.plugin.getWorldEdit().wrapPlayer((Player) sender);
		
		// check permissions
		this.plugin.checkPermission(sender, "dispenserrefill.general.dispenser");
		
		// get position
		WorldVector pos = player.getBlockTrace(300);
		
		if (pos != null) {
			// check for correct block
			if (pos.getWorld().getBlockType(pos) != Material.DISPENSER.getId()) throw new CommandException("Sorry, but currently only dispensers are allowed to be infinite.");
			
			// get current location
			Location dispenserPosition = new Location(this.plugin.getServer().getWorld(pos.getWorld().getName()), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
			
			// get cooldown
			int cooldown = (args.argsLength() >= 1 ? args.getInteger(0) : -1);
			
			// loop through dispensers
			for(InfiniteContainer dispenser : this.plugin.getContainerDatabase().getContainerList()) {
				Location currentPosition = dispenser.getLocation();
				
				if (currentPosition.equals(dispenserPosition)) {
					if (args.argsLength() >= 1) {
						// update cooldown period
						dispenser.setCooldown(cooldown);
						dispenser.setCooldownPeriod(-1);
						
						// save database
						this.plugin.saveDatabase();
						
						// notify player
						player.print("The dispenser has been updated.");
					} else {
						// dispenser already infinite! turn back to normal inventory
						this.plugin.getContainerDatabase().getContainerList().remove(dispenser);
						
						// save database
						this.plugin.saveDatabase();
						
						// notify player
						player.print("The dispenser is now back in normal mode. It can now run out of contents.");
					}
					
					// stop loop & command
					return;
				}
			}
			
			// make dispenser infinite
			this.plugin.getContainerDatabase().getContainerList().add(new InfiniteContainer(dispenserPosition, cooldown));
			
			// save database
			this.plugin.saveDatabase();
			
			// notify player
			player.print("The dispenser is now infinite");
			return;
		} else
			throw new CommandException("No block in sight!");
	}
	
	/**
	 * The /refillcooldown command.
	 * @param args
	 * @param sender
	 * @throws CommandException
	 */
	@Command(aliases = {"refillcooldown", "dispensercooldown"}, usage = "", desc = "Shows how much cooldown is still left on a dispenser.", flags = "s", max = 0)
	public void refillcooldown(CommandContext args, CommandSender sender) throws CommandException {
		// wrap player
		BukkitPlayer player = this.plugin.getWorldEdit().wrapPlayer((Player) sender);
		
		// check permissions
		this.plugin.checkPermission(sender, "dispenserrefill.general.dispenser");
		
		// get position
		WorldVector pos = player.getBlockTrace(300);
		
		if (pos != null) {
			// check for correct block
			if (pos.getWorld().getBlockType(pos) != Material.DISPENSER.getId()) throw new CommandException("This block doesn't like cookies!"); // XXX: ee
			
			// get current location
			Location dispenserPosition = new Location(this.plugin.getServer().getWorld(pos.getWorld().getName()), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

			// loop through dispensers
			for(InfiniteContainer dispenser : this.plugin.getContainerDatabase().getContainerList()) {
				Location currentPosition = dispenser.getLocation();
				
				if (currentPosition.equals(dispenserPosition)) {
					// get cooldown
					long cooldownPeriod = (dispenser.getCooldownPeriod() - (currentPosition.getWorld().getFullTime() - dispenser.getCooldown()));
					
					// no cooldown
					if (cooldownPeriod <= -1) {
						player.print("There is no cooldown left.");
						return;
					}
					
					// cooldown active
					player.print("There are still " + cooldownPeriod + " ticks left.");
					
					// stop loop & command
					return;
				}
			}
		} else
			throw new CommandException("No block in sight!");
	}
}