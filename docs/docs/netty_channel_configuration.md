The plugin uses netty channels for proxy to sub-server communication. By default, the netty channels are configured to
work
out of the box. However, in rare cases, the default host and port (localhost:9000) may already be in use. In such cases,
the plugin
will throw a `java.net.BindException: Address already in use: bind` exception on startup. To resolve this issue,
you need to customize the netty channel configuration options for both the proxy and subserver:

## **Proxy Netty Channel Configuration Options**

```yaml
netty-port: 9000
```

This option allows you to set the port on which the proxy listens for incoming connections from subservers.
The default value is 9000. You can change this to any available port on your system. The server socket will bind to
`localhost:<netty-port>`.

**Note: Make sure to update the subserver configuration to match this port if you change it.**

## **Subserver Netty Channel Configuration Options**

```yaml
netty-host: "localhost"
netty-port: 9000
```

These values need to match the host and port of the proxy netty channel configuration. The default host is `localhost`
and the default port is `9000`.
If you change the proxy's netty port, make sure to update the subserver's `netty-port` to the same value. If the proxy
is running on a different host,
update the `netty-host` accordingly.