package org.anarres.ircd;

import java.util.ArrayList;
import java.util.List;

/** An IRC network message. */
public class Message {

    private String prefix;
    private Command command;
    private String cmdtext;	// = command.name() if not null
    private String[] args;

    private Message() {
    }

    /** Constructs a new message from the given parts. */
    public Message(String prefix, Command command, String[] args) {
        this();
        this.prefix = prefix;
        this.command = command;
        this.cmdtext = command.name();
        this.args = args;
    }

    /* XXX Really, we should use this, but ... */
    @Deprecated
    public Message(String prefix, Response response, String... args) {
        this();
        this.prefix = prefix;
        this.command = Command.RESPONSE;

        int code = response.getCode();
        char[] ccode = new char[3];
        for (int i = 3; i > 0; i--) {
            ccode[i] = Character.forDigit(code % 10, 10);
            code = code / 10;
        }
        this.cmdtext = new String(ccode);
        /* XXX This isn't ideal. */
        // XXX this.args[0] = target.getName();
        this.args = toArgs(response.getMessage(args));
    }

    /**
     * Parses a protocol input line into a message according to RFC1459.
     */
    public Message(String line) {
        int start = 0;
        if (line.charAt(0) == ':') {
            start = line.indexOf(' ');
            this.prefix = line.substring(1, start);
            start++;
        } else {
            this.prefix = null;
        }

        int end = line.indexOf(' ', start);
        if (end == -1)
            end = line.length();
        this.cmdtext = line.substring(start, end);
        try {
            this.command = Enum.valueOf(Command.class, this.cmdtext);
        } catch (IllegalArgumentException e) {
            this.command = Command.UNKNOWN;
        }

        if (end >= line.length())
            this.args = new String[0];
        else
            this.args = toArgs(line.substring(end + 1));
    }

    private static String[] toArgs(String line) {
        int start = 0;
        List<String> out = new ArrayList<String>();
        for (int i = start; i < line.length(); i++) {
            if (line.charAt(i) != ' ')
                continue;
            out.add(line.substring(start, i));
            start = i + 1;
            if (start < line.length() && line.charAt(start) == ':')
                break;
        }
        if (start < line.length()) {
            if (line.charAt(start) == ':')
                out.add(line.substring(start + 1));
            else
                out.add(line.substring(start));
        }
        return out.toArray(new String[out.size()]);
    }

    /** Sets the prefix for this message, allowing it to be routed. */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /** Returns the prefix of this message. */
    public String getPrefix() {
        return prefix;
    }

    /** Returns the command of this message. */
    public Command getCommand() {
        return command;
    }

    public String getCommandText() {
        return cmdtext;
    }

    /** Returns the arguments of this message. */
    public String[] getArgs() {
        return args;
    }

    public int getArgCount() {
        return args.length;
    }

    /** Returns the specified argument of this message. */
    public String getArg(int idx) {
        return args[idx];
    }

    /** Formats this message as a string for network transmission. */
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (prefix != null)
            out.append(':').append(prefix).append(' ');
        if (command != null)
            out.append(String.valueOf(command));
        else
            out.append(cmdtext);
        for (int i = 0; i < args.length; i++) {
            out.append(' ');
            if (i == args.length - 1)
                out.append(':');
            out.append(args[i]);
        }
        return out.toString();
    }
}
