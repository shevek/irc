package org.anarres.ircd;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/** A client, not necessarily directly connected. */
public class Client extends Target {

    private String username;
    private String hostname;
    private String realname;

    private Set<ClientMode> mode;
    private Set<Channel> channels;
    private Set<Invite> invites;

    private void _init(String username, String hostname,
            /* String servername, */ String realname) {
        this.username = username;
        this.hostname = hostname;
        // this.servername = servername;
        this.realname = realname;

        this.mode = EnumSet.noneOf(ClientMode.class);
        this.channels = new HashSet<Channel>();
    }

    /** Constructs a new indirectly connected client. */
    public Client(Server server, String nick,
            String username, String hostname,
            /* String servername, */ String realname) {
        super(server, nick);
        _init(username, hostname, realname);
    }

    /** Constructs a new directly connected client. */
    public Client(Link link, String nick,
            String username, String hostname,
            /* String servername, */ String realname) {
        super(link, nick);
        _init(username, hostname, realname);
    }

    /** Returns the nick of this client. */
    public String getNick() {
        return getName();
    }

    public String getUsername() {
        return username;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean setNick(String nick) {
        if (!getDaemon().addClient(nick, this))
            return false;
        String prev = getName();
        setName(nick);
        getDaemon().removeClient(prev);
        return true;
    }

    public boolean isOperator() {
        return mode.contains(ClientMode.o);
    }

    public void addChannel(Channel ch) {
        channels.add(ch);
        ch.addClient(this);
    }

    public void removeChannel(Channel ch) {
        channels.remove(ch);
        ch.removeClient(this);
    }

    public String getPrefix() {
        return getName() + "!" + username + "@" + hostname;
    }

    /** Returns the set of directly connected servers which see nick
     * changes on this client. */
    public Collection<Link> getLinks() {
        Set<Link> out = new HashSet<Link>();
        getLinks(out);
        return out;
    }

    /* pp */ void getLinks(Collection<Link> out) {
        for (Channel c : channels)
            c.getLinks(out);
        out.add(getLink());
    }

    private static <T extends Enum<T>> String join(Class<T> c) {
        StringBuilder out = new StringBuilder();
        for (T o : EnumSet.<T>allOf(c))
            out.append(o.name());
        return out.toString();
    }
    private static final String CLIENT_MODES = join(ClientMode.class);
    private static final String CHANNEL_MODES = join(ChannelMode.class);
    private static final String OPER_MODES = join(OperMode.class);

    public void helo() {
        Daemon d = getDaemon();
        assert getLink() != null : "helo requires local client";
        send(Response.IFO_WELCOME,
                "NETWORK_NAME", getNick());
        send(Response.IFO_HOSTNAME,
                d.getName(),
                getLink().getSession().getLocalAddress(),
                d.getVersion());
        send(Response.IFO_STARTUP, d.getCreated());
        send(Response.IFO_MODES, d.getName(),
                d.getVersion(),
                CLIENT_MODES, CHANNEL_MODES, OPER_MODES);
        send(Response.IFO_FEATURES, "TOPICLEN=390 CHANTYPES=#&");
        send(Response.RPL_LUSERCLIENT);
        send(Response.RPL_LUSEROP);
        send(Response.RPL_LUSERUNKNOWN);
        send(Response.RPL_LUSERCHANNELS);
        send(Response.RPL_LUSERME);
        // 265, 266, 250
        send(Response.RPL_MOTDSTART, d.getName());
        send(Response.RPL_MOTD);
        send(Response.RPL_ENDOFMOTD);
    }

    public void cmd(Message m) {
        Channel ch;
        Client cl;
        String name;
        Response r;

        m.setPrefix(getPrefix());	// Allows forwarding.
        switch (m.getCommand()) {
            /* 4.1: Connection registration. */
            case PASS:
            case USER:
            case SERVER:
                send(Response.ERR_ALREADYREGISTRED);
                return;

            case NICK:
                /* XXX Max nick length. */
                if (!setNick(m.getArg(0))) {
                    send(Response.ERR_NICKNAMEINUSE, m.getArg(0));
                    break;
                }
                forward(getLinks(), m);
                break;

            case OPER:
                /* XXX Check privs. */
                mode.add(ClientMode.o);
                break;

            case SQUIT:
                if (!isOperator()) {
                    send(Response.ERR_NOPRIVILEGES);
                    break;
                }
                /* XXX Do something. */
                break;

            /* Client only: */
            case JOIN:
                ch = getDaemon().getChannel(m.getArg(0), true);
                addChannel(ch);
                forward(ch.getLinks(), m);
                break;
            case PART:
                ch = getDaemon().getChannel(m.getArg(0), false);
                if (ch == null) {
                    send(Response.ERR_NOSUCHCHANNEL, m.getArg(0));
                    break;
                }
                removeChannel(ch);
                forward(ch.getLinks(), m);
                break;

            case MODE:
                name = m.getArg(0);
                // XXX check not empty
                switch (name.charAt(0)) {
                    case '#':
                    case '&':
                        ch = getDaemon().getChannel(name, false);
                        if (ch == null) {
                            send(Response.ERR_NOSUCHCHANNEL, name);
                            break;
                        }
                        // XXX set mode
                        forward(ch.getLinks(), m);
                        break;
                    default:
                        cl = getDaemon().getClient(name);
                        if (cl == null) {
                            send(Response.ERR_NOSUCHNICK, name);
                            break;
                        }
                        // XXX set mode
                        forward(cl.getLinks(), m);
                        break;
                }
                break;

            case TOPIC:
                name = m.getArg(0);
                ch = getDaemon().getChannel(name, false);
                if (ch == null) {
                    send(Response.ERR_NOSUCHCHANNEL, name);
                    break;
                }
                if (m.getArgCount() == 1) {
                    String topic = ch.getTopic();
                    if (topic == null)
                        send(Response.RPL_NOTOPIC, name);
                    else
                        send(Response.RPL_TOPIC, name, topic);
                } else {
                    ch.setTopic(m.getArg(1));
                    forward(ch.getLinks(), m);
                }
                break;

            case PRIVMSG:
            case NOTICE:
                name = m.getArg(0);
                // XXX check not empty
                switch (name.charAt(0)) {
                    case '#':
                    case '&':
                        ch = getDaemon().getChannel(name, false);
                        if (ch == null) {
                            send(Response.ERR_NOSUCHCHANNEL, name);
                            break;
                        }
                        /* XXX Check not +n */
                        forward(ch.getLinks(), m);
                        break;
                    default:
                        cl = getDaemon().getClient(name);
                        if (cl == null) {
                            send(Response.ERR_NOSUCHNICK, name);
                            break;
                        }
                        cl.getLink().send(m);
                        break;
                }
                break;

            case PING:
                send(new Message(getDaemon().getName(),
                        Command.PONG, m.getArgs()));
                break;

            case QUIT:
                destroy();	// XXX
                break;

            case DUMP:
                System.out.println("Daemon = " + getDaemon());
                System.out.println("Server = " + getServer());
                System.out.println("Client = " + this);
                for (Channel c : channels)
                    System.out.println("Channel = " + c);
                break;

        }
    }

    @Override
    public void destroy() {
        Set<Channel> ch = channels;
        if (ch != null)
            for (Channel c : ch)
                c.removeClient(this);
        channels = null;
        invites = null;
        Server s = getServer();
        if (s != null)
            s.removeClient(this);
        Daemon d = getDaemon();
        if (d != null)
            d.removeClient(this);
        Link l = getLink();
        if (l != null)
            l.close();
        super.destroy();
    }

    @Override
    public String toString() {
        return "Client(nick=" + getNick()
                + ", mode=" + mode
                + ")";
    }

}
