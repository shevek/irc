package org.anarres.ircd;

/** A main() routine to invoke a standalone server. */
public class Main {

    private Main() {
    }

    public static final void main(String[] args) throws Exception {
        System.out.println("Spircle version " + Version.getVersion() + " starting up ...");
        Daemon d = new Daemon();
        d.addListener(new Listener(6667));
        d.start();
        for (;;) {
            Thread.sleep(100);
        }
    }

}
