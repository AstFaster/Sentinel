package fr.astfaster.sentinel.api.util;

public class BuilderException extends SentinelException {

    public BuilderException(String item, String... invalidFields) {
        super("Couldn't build " + item + "! Invalid fields: " + formatFields(invalidFields) + ".");
    }

    private static String formatFields(String... fields) {
        final StringBuilder builder = new StringBuilder();

        for (String field : fields) {
            builder.append("'").append(field).append("', ");
        }

        final String formatted = builder.toString();

        return formatted.substring(0, formatted.length() - 2);
    }

}
