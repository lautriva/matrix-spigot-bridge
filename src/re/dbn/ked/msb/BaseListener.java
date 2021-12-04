package re.dbn.ked.msb;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BaseListener implements Listener
{
	protected MatrixSpigotBridge _plugin;

    public BaseListener(MatrixSpigotBridge plugin)
    {
    	this._plugin = plugin;
    }

    protected void sendMatrixMessage(String format, String message)
    {
    	sendMatrixMessage(format, message, null);
    }

    protected void sendMatrixMessage(String format, String message, Player player)
    {
    	if (format == null || format.isEmpty())
    		return;

        _plugin.sendMessageToMatrix(format, message, player);
    }
}
