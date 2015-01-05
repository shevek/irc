package org.anarres.ircd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An IRC daemon object. */
public class Daemon {

    private String name;
    private final Date created;

    private final List<Listener> listeners;

    private final Map<String, Server> servers;
    private final Map<String, Client> clients;
    private final Map<String, Channel> channels;

    public Daemon() {
        this.name = "irc.anarres.org";
        this.created = new Date();
        this.listeners = new ArrayList<Listener>();
        this.servers = new HashMap<String, Server>();
        this.clients = new HashMap<String, Client>();
        this.channels = new HashMap<String, Channel>();
    }

    /** Returns the name (hostname) of this daemon. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Returns the version of this daemon. */
    public String getVersion() {
        return "Spircle-" + Version.getVersion();
    }

    /** Returns the date at which this daemon was created. */
    public String getCreated() {
        return String.valueOf(created);
    }

    /* Networking */
    /** Adds a network listener to this daemon. */
    public void addListener(Listener l) {
        listeners.add(l);
    }

    /* Servers */
    public Server getServer(String name) {
        return servers.get(name);
    }

    public void addServer(Server server) {
        String name = server.getName();
        servers.put(name, server);
    }

    public void removeServer(Server server) {
        servers.remove(server.getName());
    }

    /* Clients */
    public Client getClient(String nick) {
        return clients.get(nick);
    }

    public boolean addClient(String nick, Client client) {
        assert nick != null : "Cannot add client without nick";
        if (clients.containsKey(nick))
            return false;
        clients.put(nick, client);
        return true;
    }

    public boolean addClient(Client client) {
        return addClient(client.getNick(), client);
    }

    public void removeClient(String nick) {
        clients.remove(nick);
    }

    public void removeClient(Client client) {
        removeClient(client.getNick());
    }

    /* Channels */
    public Channel getChannel(String name, boolean create) {
        Channel c = channels.get(name);
        if (c != null)
            return c;
        if (!create)
            return null;
        c = new Channel(this, name);
        addChannel(c);
        return c;
    }

    public Channel getChannel(String name) {
        return channels.get(name);
    }

    public void addChannel(Channel channel) {
        channels.put(channel.getName(), channel);
    }

    public void removeChannel(Channel channel) {
        channels.remove(channel.getName());
    }

    /* Events */
    public void start()
            throws IOException {
        for (Listener l : listeners)
            l.start(this);
    }

    public void stop() {
        for (Listener l : listeners)
            l.stop();
    }

    @Override
    public String toString() {
        return "Daemon("
                + "servers=" + servers.keySet()
                + ", clients=" + clients.keySet()
                + ", channels=" + channels.keySet()
                + ")";
    }

}
