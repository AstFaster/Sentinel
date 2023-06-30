package fr.astfaster.sentinel.proxy.terminal;

import fr.astfaster.sentinel.api.Sentinel;
import fr.astfaster.sentinel.api.command.Command;
import fr.astfaster.sentinel.api.command.CommandContext;
import fr.astfaster.sentinel.api.command.CommandSender;
import fr.astfaster.sentinel.proxy.command.CommandContextImpl;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.Scanner;

public class SentinelConsole implements CommandSender {

    private final Logger logger = LogManager.getLogger(SentinelConsole.class);

    private Thread thread;

    public void start() {
        this.thread = new Thread(new Process());
        this.thread.start();
    }

    public void shutdown() {
        this.thread.interrupt();
    }

    private void parseInput(String input) {
        input = input.trim().toLowerCase();

        String[] args = input.split(" ");

        final String commandLabel = args[0];

        args = Arrays.copyOfRange(args, 1, args.length);

        final Command command = Sentinel.instance().commandsRegistry().command(commandLabel);

        if (command != null) {
            final CommandContext ctx = new CommandContextImpl(this, args);

            command.execute(ctx);
        } else {
            this.logger.error("Couldn't find '" + commandLabel + "' command!");
        }
    }

    private class Process implements Runnable {

        private final Scanner inputReader = new Scanner(System.in);

        @Override
        public void run() {
            this.retrieveInput();
        }

        private void retrieveInput() {
            final String line = this.inputReader.nextLine();

            if (!line.isEmpty() && !line.isBlank()) {
                SentinelConsole.this.parseInput(line);
            }

            this.retrieveInput();
        }

    }

}
