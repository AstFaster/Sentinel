package fr.astfaster.sentinel.proxy.util;

public class CharactersChecker {

    private static boolean isCharacterAllowed(char c) {
        return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '.' || c == '-';
    }

    public static boolean isNameValid(String name) {
        for (int i = 0; i < name.length(); i++) {
            if (!isCharacterAllowed(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
