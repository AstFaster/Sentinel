package fr.astfaster.sentinel.proxy.command;

import fr.astfaster.sentinel.api.command.CommandMeta;
import fr.astfaster.sentinel.api.util.BuilderException;
import org.jetbrains.annotations.NotNull;

record CommandMetaImpl(String label, String[] aliases) implements CommandMeta {

    static class Builder implements CommandMeta.Builder {

        private String label = null;
        private String[] aliases = new String[0];

        @Override
        public CommandMeta.Builder label(@NotNull String label) {
            this.label = label.toLowerCase();
            return this;
        }

        @Override
        public CommandMeta.Builder aliases(@NotNull String... aliases) {
            this.aliases = aliases;
            return this;
        }

        public CommandMeta build() {
            if (this.label == null) {
                throw new BuilderException("CommandMeta", "label");
            }
            return new CommandMetaImpl(label, aliases);
        }

    }

}
