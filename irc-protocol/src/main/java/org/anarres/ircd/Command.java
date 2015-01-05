package org.anarres.ircd;

/** Network commands. */
public enum Command {
    /* 4.1: Connection registration. */

    PASS(1), NICK(1, 2), USER(4),
    SERVER(3),
    OPER(2),
    QUIT(0, 1),
    SQUIT(1, 2), // is comment optional?

    /* 4.2: Channel operations. */
    JOIN(1, 2), PART(1),
    MODE(2, 5), TOPIC(1, 2),
    NAMES(0, 1), LIST(0, 1),
    INVITE(2), KICK(2, 3),
    /* 4.3: Server queries and commands. */
    VERSION(0, 1),
    STATS(0, 2), LINKS(0, 2),
    TIME(0, 1),
    CONNECT(1, 3), TRACE(0, 1),
    ADMIN(0, 1), INFO(0, 1),
    /* 4.4: Sending messages. */
    PRIVMSG(2), NOTICE(2),
    /* 4.5: User based queries. */
    WHO(0, 2), WHOIS(1, 2), WHOWAS(1, 3),
    /* 4.6: Miscellaneous messages. */
    KILL(2),
    PING(1), PONG(1),
    ERROR(1),
    /* 5: Optionals. */
    AWAY(0, 1),
    REHASH(0),
    RESTART(0),
    SUMMON(1, 2),
    USERS(0, 1),
    WALLOPS(1),
    USERHOST(1, 5),
    ISON(1, 1024),
    /* Local constants. */
    DUMP(0),
    RESPONSE(false),
    UNKNOWN(false);

    private boolean legal;
    private int minargs;
    private int maxargs;

    Command(int args) {
        this.legal = true;
        this.minargs = args;
        this.maxargs = args;
    }

    Command(int minargs, int maxargs) {
        this.legal = true;
        this.minargs = minargs;
        this.maxargs = maxargs;
    }

    Command(boolean always_false) {
        this.legal = false;
        this.minargs = 0;
        this.maxargs = 0;
    }

    public boolean isLegal() {
        return legal;
    }

    public int getMinArgs() {
        return minargs;
    }

    public int getMaxArgs() {
        return maxargs;
    }

}
