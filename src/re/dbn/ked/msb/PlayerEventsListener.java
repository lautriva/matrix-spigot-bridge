package re.dbn.ked.msb;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerEventsListener extends BaseListener
{
	public PlayerEventsListener(MatrixSpigotBridge plugin) 
	{
		super(plugin);
	}
	
	@EventHandler
    public void playerJoined(PlayerJoinEvent evt) 
	{
    	String message = evt.getJoinMessage();
        if (message == null)
        	message = "";

        sendMatrixMessage(
    		_plugin.getConfig().getString("format.player.join"),
    		ChatColor.stripColor(message),
    		evt.getPlayer()
		);
    }

	@EventHandler
    public void playerQuit(PlayerQuitEvent evt) 
	{
    	String message = evt.getQuitMessage();
        if (message == null)
        	message = "";
        
        sendMatrixMessage(
    		_plugin.getConfig().getString("format.player.quit"),
    		ChatColor.stripColor(message),
    		evt.getPlayer()
		);
    }

    @EventHandler
    public void playerDied(PlayerDeathEvent evt) 
    {
    	String message = evt.getDeathMessage();
        if (message == null)
        	message = "";

        sendMatrixMessage(
    		_plugin.getConfig().getString("format.player.death"),
    		ChatColor.stripColor(message),
    		evt.getEntity()
		);
    }
}
