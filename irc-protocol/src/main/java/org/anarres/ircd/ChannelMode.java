package org.anarres.ircd;

/** Channel modes. */
public enum ChannelMode {

    // give/take channel operator privileges;

    o("operator", "User is a channel operator", false),
    // private channel flag;
    p("private", "Private channel", false),
    // secret channel flag;
    s("secret", "Secret channel", false),
    // invite-only channel flag;
    i("invite", "Invite only channel", false),
    // topic settable by channel operator only flag;
    t("topicops", "Topic settable by channel operator only", false),
    // no messages to channel from clients on the outside;
    n("nomessages", "No messages to channel from outside", false),
    // moderated channel;
    m("moderated", "Moderated channel", false),
    // set the user limit to channel;
    l("limit", "User limit for channel", true),
    // set a ban mask to keep users out;
    b("ban", "List of n!u@h masks which are banned", true),
    // give/take the ability to speak on a moderated channel;
    v("voice", "User has ability to speak on a moderated channel", true),
    // set a channel key (password).
    k("key", "Channel key (password)", true),
    e("banexcept", "List of n!u@h masks which can join through a +b", true),
    E("invexcept", "List of n!u@h masks which can join through a +i", true);

    private final String name;
    private final String desc;
    private final boolean arg;

    ChannelMode(String name, String desc, boolean arg) {
        this.name = name;
        this.desc = desc;
        this.arg = arg;
    }

    public String getName() {
        return name == null ? name() : name;
    }

    public String getDescription() {
        return desc;
    }

    public boolean getArgument() {
        return arg;
    }

    @Override
    public String toString() {
        return "+" + name() + " - "
                + getName() + ": " + getDescription();
    }

}
