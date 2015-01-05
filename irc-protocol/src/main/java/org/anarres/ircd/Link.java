package org.anarres.ircd;

import org.apache.mina.core.session.IoSession;
import static org.anarres.ircd.Command.NICK;
import static org.anarres.ircd.Command.PASS;
import static org.anarres.ircd.Command.QUIT;
import static org.anarres.ircd.Command.SERVER;
import static org.anarres.ircd.Command.SQUIT;
import static org.anarres.ircd.Command.USER;

/** A network link. */
public class Link {

    private final Daemon daemon;
    private final IoSession session;

    private String pass;
    private String name;
    private Target target;

    public Link(Daemon d, IoSession s) {
        this.daemon = d;
        this.session = s;
    }

    public Daemon getDaemon() {
        return daemon;
    }

    public IoSession getSession() {
        return session;
    }

    public String getName() {
        Target t = target;	// atomic
        if (t != null)
            return t.getName();
        String n = name;	// atomic
        if (n != null)
            return n;
        return "*";
    }

    /* Output */
    private void send(String msg) {
        if (session.isConnected())
            session.write(msg);
        else
            System.err.println("NOCONN: " + msg);
    }

    public void send(Response response, Object... args) {
        StringBuilder out = new StringBuilder();
        out.append(':').append(daemon.getName());
        out.append(' ');
        String code = String.valueOf(response.getCode());
        for (int i = code.length(); i < 3; i++)
            out.append('0');
        out.append(code);
        out.append(' ').append(getName());
        out.append(' ');
        String msg = response.getMessage();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c == '$') {
                char d = msg.charAt(++i);
                if (d == '$')
                    out.append('$');
                else
                    out.append(args[Character.digit(d, 10) - 1]);
            } else {
                out.append(c);
            }
        }
        send(out.toString());
    }

    public void send(Message m) {
        send(String.valueOf(m));
    }

    /* Input */
    protected boolean isLegal(Message m) {
        if (!m.getCommand().isLegal()) {
            send(Response.ERR_UNKNOWNCOMMAND, m.getCommandText());
            return false;
        }
        if (m.getArgCount() < m.getCommand().getMinArgs()) {
            send(Response.ERR_NEEDMOREPARAMS, m.getCommand().name(),
                    m.getArgCount(),
                    m.getCommand().getMinArgs());
            return false;
        }
        return true;
    }

    public synchronized void messageReceived(Message m) {

        if (!isLegal(m))
            return;

        if (target != null) {
            target.cmd(m);
            return;
        }

        /* Unregistered objects only. */
        switch (m.getCommand()) {

            case USER:
                // <username> <hostname> <servername> <realname>
                Client c = new Client(
                        this,
                        name,
                        m.getArg(0), m.getArg(1),
                        /* m.getArg(2), */ m.getArg(3)
                );
                this.target = c;
                daemon.addClient(c);
                c.helo();
                break;
            case PASS:
                this.pass = m.getArg(0);
                /* XXX Username removes the ~ */
                break;
            case NICK:
                /* XXX Duplicates Client.java */
                String nick = m.getArg(0);
                if (daemon.getClient(nick) != null) {
                    send(Response.ERR_NICKNAMEINUSE, nick);
                    break;
                }
                this.name = nick;
                break;

            case SERVER:
                // <name> <hopcount> <info>
                this.name = m.getArg(0);
                Server s = new Server(
                        this,
                        m.getArg(0), Integer.parseInt(m.getArg(1)),
                        m.getArg(2)
                );
                this.target = s;
                daemon.addServer(s);
                s.helo();
                break;

            case QUIT:
            case SQUIT:
                send("ERROR :Closing Link: YOUR_IP ()");
                close();
                break;
            default:
                send(Response.ERR_NOTREGISTERED);
                break;
        }
    }

    public void close() {
        getSession().close();
    }

}
