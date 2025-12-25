package tk.bubustein.money.bank;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class IbanGenerator {

    public static String generateIban(String countryCode,
                                      String bankPrefix,
                                      AccountKind kind,
                                      int accountId) {

        String cc = countryCode.toUpperCase(Locale.ROOT);
        if (cc.length() != 2) {
            throw new IllegalArgumentException("Country code must be 2 letters");
        }

        String bban = getString(bankPrefix, kind, accountId);

        String rearranged = bban + cc + "00";

        StringBuilder numeric = new StringBuilder();
        for (char ch : rearranged.toCharArray()) {
            if (Character.isLetter(ch)) {
                int val = Character.toUpperCase(ch) - 'A' + 10;
                numeric.append(val);
            } else {
                numeric.append(ch);
            }
        }

        int mod = 0;
        for (int i = 0; i < numeric.length(); i++) {
            int digit = numeric.charAt(i) - '0';
            mod = (mod * 10 + digit) % 97;
        }

        int check = 98 - mod;
        String checkStr = String.format("%02d", check);

        return cc + checkStr + bban;
    }
    private static @NotNull String getString(String bankPrefix, AccountKind kind, int accountId) {
        String prefix = bankPrefix.toUpperCase(Locale.ROOT);
        if (prefix.length() != 4) {
            throw new IllegalArgumentException("Bank prefix must be 4 letters");
        }
        char typeChar = switch (kind) {
            case CREDIT  -> 'C';
            case SAVINGS -> 'S';
            case DEBIT   -> 'D';
        };
        String accountStr = String.format("%010d", accountId);
        return prefix + typeChar + accountStr;
    }
}