package org.anarres.ircd;

import javax.management.ObjectName;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.integration.jmx.IoSessionMBean;

/** Glue between Mina and Spircle. */
public class ProtocolHandler extends IoHandlerAdapter {

	/* This uses getClass() so cannot be static. */
	private final AttributeKey	LINK =
					new AttributeKey(getClass(), "link");

	private Daemon		daemon;
	private ObjectName	name;

	public ProtocolHandler(Daemon d) {
		this.daemon = d;
	}

	@Override
	public void sessionOpened(IoSession session) {
		Link	l = new Link(daemon, session);
		session.setAttribute(LINK, l);
		name = Management.register(
			"IoSessionMBean,id=" + System.identityHashCode(session),
			new IoSessionMBean(session)
				);
	}

	/**
	 * Mina message handler method.
	 *
	 * If we plan to have multiple threads entering this method,
	 * we must make sure to serialize the messages to any particular
	 * link/handler object.
	 */
	@Override
	public void messageReceived(IoSession session, Object message) {
		String	text = (String)message;
		if (text.length() == 0)
			return;
		Message	m = new Message(text);
		System.out.println("Message = " + m);
		Link	l = (Link)session.getAttribute(LINK);
		l.messageReceived(m);
	}

	@Override
	public void sessionClosed(IoSession session) {
		Management.unregister(name);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		cause.printStackTrace();
		session.close();
	}

}
