package org.anarres.ircd;

public class Invite extends java.lang.ref.WeakReference<Channel> {
	public Invite(Channel ch) {
		super(ch);
	}
}
