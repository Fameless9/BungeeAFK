 package net.fameless.velocity;

 import com.velocitypowered.api.event.Subscribe;
 import com.velocitypowered.api.event.command.CommandExecuteEvent;
  import com.velocitypowered.api.event.player.PlayerChatEvent;
 import com.velocitypowered.api.event.player.ServerPostConnectEvent;
 import com.velocitypowered.api.proxy.Player;
   import net.fameless.core.handling.AFKHandler;
    import org.jetbrains.annotations.NotNull;


public class VelocityAFKHandler extends AFKHandler {

    @Override
    public void onInit() {
        VelocityPlatform.getProxy().getEventManager().register(VelocityPlatform.get(), this);
    }

    @Subscribe
    public void onCommandExecute(@NotNull CommandExecuteEvent event) {
        if (event.getCommandSource() instanceof Player p) {
            VelocityPlayer player = VelocityPlayer.adapt(p);
            player.setActive();
        }
    }

    @Subscribe
    public void onPlayerChat(@NotNull PlayerChatEvent event) {
        VelocityPlayer player = VelocityPlayer.adapt(event.getPlayer());
        player.setActive();
    }

    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent event) {
        VelocityPlayer player = VelocityPlayer.adapt(event.getPlayer());
        if (event.getPreviousServer() == null) {
            player.setActive();
        }
    }
}
