package net.fameless.network.packet;

import io.netty.channel.Channel;
import net.fameless.network.MessageType;
import net.fameless.network.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class AbstractPacket {

    public final MessageType type;

    public AbstractPacket(MessageType type) {
        this.type = type;
    }

    public void send(Channel @NotNull ... channels) {
        for (Channel ch : channels) {
            ch.writeAndFlush(NetworkUtil.msg(this.type, this));
        }
    }

    public void send(@NotNull Collection<Channel> channels) {
        channels.forEach(ch -> ch.writeAndFlush(NetworkUtil.msg(this.type, this)));
    }
}
