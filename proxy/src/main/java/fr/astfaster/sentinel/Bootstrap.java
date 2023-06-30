package fr.astfaster.sentinel;

import fr.astfaster.sentinel.proxy.SentinelOptions;
import fr.astfaster.sentinel.proxy.SentinelServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.DecimalFormat;

public class Bootstrap {

    private final static Logger LOGGER = LogManager.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        SentinelOptions options = null;
        try {
             options = new SentinelOptions(args);
        } catch (IOException e) {
            LOGGER.error("Couldn't parse options!", e);
        }

        if (options != null && !options.helped()) {
            final long beforeStarting = System.currentTimeMillis();
            final SentinelServer sentinel = new SentinelServer();

            sentinel.start(options);

            LOGGER.info("Sentinel started in {}s!", new DecimalFormat("#.##").format((double) (System.currentTimeMillis() - beforeStarting) / 1000));
        }
    }

}
