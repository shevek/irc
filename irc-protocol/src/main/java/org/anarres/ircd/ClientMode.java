package org.anarres.ircd;

/** Client modes. */
public enum ClientMode {

    b("bots", "See bot and drone flooding notices"),
    c("cconn", "Client connection/quit notices"),
    D("deaf", "Don't receive channel messages"),
    d("debug", "See debugging notices"),
    f("full", "See I: line full notices"),
    G("softcallerid", "Server Side Ignore for users not on your channels"),
    g("callerid", "Server Side Ignore (for privmsgs etc)"),
    i("invisible", "Not shown in NAMES or WHO unless you share a channel"),
    k("skill", "See server generated KILL messages"),
    l("locops", "See LOCOPS messages"),
    n("nchange", "See client nick changes"),
    o("operator", "User is a server operator"),
    r("rej", "See rejected client notices"),
    s("servnotice", "See general server notices"),
    u("unauth", "See unauthorized client notices"),
    w("wallop", "See server generated WALLOPS"),
    x("external", "See remote server connection and split notices"),
    y("spy", "See LINKS, STATS, TRACE notices etc."),
    z("operwall", "See oper generated WALLOPS");

    private final String name;
    private final String desc;

    ClientMode(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    @Override
    public String toString() {
        return "+" + name() + " - "
                + getName() + ": " + getDescription();
    }

}
