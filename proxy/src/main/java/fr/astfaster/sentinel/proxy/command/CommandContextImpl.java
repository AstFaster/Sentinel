package fr.astfaster.sentinel.proxy.command;

import fr.astfaster.sentinel.api.command.CommandContext;
import fr.astfaster.sentinel.api.command.CommandSender;

public record CommandContextImpl(CommandSender sender, String[] args) implements CommandContext {}
