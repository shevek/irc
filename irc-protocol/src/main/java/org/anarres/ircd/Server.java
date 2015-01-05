package org.anarres.ircd;

import java.util.HashSet;
import java.util.Set;

/**
 * A server, not necessarily directly connected.
 */
public class Server extends Target {

	private int				hopcount;
	private String			info;

	private Set<Client>		clients;

	private void _init(int hopcount, String info) {
		this.hopcount = hopcount;
		this.info = info;

		this.clients = new HashSet<Client>();
	}

	public Server(Server server, String name,
					int hopcount, String info) {
		super(server, name);
		_init(hopcount, info);
	}

	public Server(Link link, String name,
					int hopcount, String info) {
		super(link, name);
		_init(hopcount, info);
	}

/* Clients */

	public void addClient(Client c) {
		clients.add(c);
	}

	public void removeClient(Client c) {
		clients.remove(c);
	}

/* Commands */

	public void helo() {
		assert getLink() != null : "helo requires local server";
	}

	public void cmd(Message m) {
		switch (m.getCommand()) {
			case PASS:
				send(Response.ERR_ALREADYREGISTRED);
				return;

			/* Server only: */
			case USER:
			case SERVER:
			case KILL:
				break;

			case SQUIT:
				destroy();	// XXX
				break;

		}
	}

	@Override
	public void destroy() {
		Set<Client>	cl = clients;
		if (cl != null)
			for (Client c : clients)
				c.destroy();
		clients = null;
		Daemon	d = getDaemon();
		if (d != null)
			d.removeServer(this);
		Link	l = getLink();
		if (l != null)
			l.close();
		super.destroy();
	}

}
