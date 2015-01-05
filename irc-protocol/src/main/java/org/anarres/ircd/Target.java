package org.anarres.ircd;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.meta.When;


/** Abstract superclass of Client and Server. */
public abstract class Target {

	private Server		server;
	private Link		link;
	private String		name;

	protected Target(Link link, String name) {
		this.server = null;
		this.link = link;
		this.name = name;
	}

	protected Target(Server server, String name) {
		this.server = server;
		this.link = server.getLink();
		this.name = name;
	}

	/**
	 * Returns the core daemon object.
	 */
	public Daemon getDaemon() {
		Link	l = getLink();
		if (l == null)
			return null;
		return l.getDaemon();
	}

	/**
	 * Returns the server via which this target is connected.
	 *
	 * This will be null if the target is directly connected.
	 */
	protected Server getServer() {
		return server;
	}

	/**
	 * Returns the link by which this target is directly connected.
	 */
	protected Link getLink() {
		return link;
	}

	/**
	 * Returns the name of this target.
	 *
	 * This is the {@link Server} name or {@link Client} nick.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this target.
	 *
	 * This is called in response to the client NICK command.
	 */
	protected void setName(String name) {
		this.name = name;
	}

	protected void send(Message m) {
		getLink().send(m);
	}

	protected void send(Response r, Object... args) {
		getLink().send(r, args);
	}

	protected void forward(Iterable<Link> links, Message m) {
		// Link	_l = getLink();
		for (Link l : links)
			// if (l != _l)
				l.send(m);
	}

	public abstract void helo();

	/** Interprets the given message as a command from a locally
	 * connected target. */
	public abstract void cmd(Message m);

	/** Breaks all pointers to this object. */
	@OverridingMethodsMustInvokeSuper
	public void destroy() {
		server = null;
		link = null;
	}
}
