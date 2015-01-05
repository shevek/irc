package org.anarres.ircd;

import java.io.IOException;

import java.net.InetSocketAddress;

import javax.management.ObjectName;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.integration.jmx.IoServiceMBean;

/** A listen port. */
public class Listener {
	private int				port;

	public Listener(int port) {
		this.port = port;
	}

	private SocketAcceptor		acceptor;

	private ObjectName			name;

	/* pp */ void start(Daemon daemon)
						throws IOException {
		acceptor = new NioSocketAcceptor();

		DefaultIoFilterChainBuilder
						chain = acceptor.getFilterChain();
		chain.addLast("codec", new ProtocolCodecFilter(
			new TextLineCodecFactory()
		));
		chain.addLast("logger", new LoggingFilter());

		acceptor.setHandler(new ProtocolHandler(daemon));
		acceptor.bind(new InetSocketAddress(port));

		name = Management.register(
			"IoServiceMBean,port=" + port,
			new IoServiceMBean(acceptor)
				);
	}

	/* pp */ void stop() {
		Management.unregister(name);
		acceptor.unbind();
		acceptor = null;
	}

}
