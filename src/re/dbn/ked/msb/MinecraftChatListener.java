package re.dbn.ked.msb;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MinecraftChatListener extends BaseListener
{
    public MinecraftChatListener(MatrixSpigotBridge plugin) 
    {
		super(plugin);
	}

	@EventHandler
    public void messageReceived(AsyncPlayerChatEvent evt)
    {
        sendMatrixMessage(
    		_plugin.getConfig().getString("format.player.chat"),
    		ChatColor.stripColor(evt.getMessage()),
    		evt.getPlayer()
		);
    }
}
