package org.anarres.ircd;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/** An IRC channel. */
public class Channel {

    private final Daemon daemon;
    private final String name;
    private String topic;
    private final EnumMap<ChannelMode, Object> mode;
    private final Set<Client> clients;

    private Set<Mask> ban;
    private Set<Mask> banexcept;

    public Channel(Daemon daemon, String name) {
        this.daemon = daemon;
        this.name = name;
        this.topic = null;
        this.mode = new EnumMap<ChannelMode, Object>(ChannelMode.class);
        this.clients = new HashSet<Client>();
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isEmpty() {
        return clients.isEmpty();
    }

    /** Use {@link Daemon#joinChannel(String,Client)}. */
    public void addClient(Client c) {
        clients.add(c);
    }

    /** Use {@link Daemon#partChannel(Channel,Client)}. */
    public void removeClient(Client c) {
        clients.remove(c);
        if (clients.isEmpty())
            daemon.removeChannel(this);
    }

    /*
     public Collection<Client> getClients() {
     return new ArrayList<Client>(clients);
     }
     */
    /** Returns the set of links listening to this channel. */
    public Collection<Link> getLinks() {
        Set<Link> out = new HashSet<Link>();
        getLinks(out);
        return out;
    }

    /* pp */ void getLinks(Collection<Link> out) {
        for (Client c : clients) {
            out.add(c.getLink());
        }
    }

    @Override
    public String toString() {
        Set<String> names = new HashSet<String>(clients.size());
        for (Client c : clients) {
            names.add(c.getNick());
        }
        return "Client(name=" + getName()
                + ", topic=" + topic
                + ", mode=" + mode
                + ", clients=" + names
                + ")";
    }

}
