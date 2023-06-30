package fr.astfaster.sentinel.api.command;

public interface CommandContext {

    CommandSender sender();

    String[] args();

}
