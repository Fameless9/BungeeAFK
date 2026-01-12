```
Couldn't pass ProxyInitializeEvent to bungeeafk <version>
java.net.BindException: Address already in use: bind
```

If you encounter the issue `java.net.BindException: Address already in use: bind` on startup, it means that the default
netty port (9000)
is already in use by another application on your system. To resolve this issue, you need to change the netty port
configuration for both the proxy and subserver.

You can find the guide to resolve this exception here: [Netty Channel Configuration](netty_channel_configuration.md)