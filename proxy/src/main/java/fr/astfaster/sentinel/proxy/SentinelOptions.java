package fr.astfaster.sentinel.proxy;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.util.Arrays;

public class SentinelOptions {

    private final OptionSet set;

    private boolean helped = false;

    public SentinelOptions(String[] args) throws IOException {
        final OptionParser parser = new OptionParser();
        final OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("?", "help"), "Shows help")
                .forHelp();

        this.set = parser.parse(args);

        if (this.set.has(help)) {
            parser.printHelpOn(System.out);

            this.helped = true;
        }
    }

    public OptionSet set() {
        return this.set;
    }

    public boolean helped() {
        return this.helped;
    }

}
