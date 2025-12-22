package tk.bubustein.money.bank;

import java.util.Locale;

public class IbanGenerator {
    public static String generateIban(String countryCode,
                                      String playerName,
                                      String bankPrefix,
                                      AccountKind kind,
                                      int accountId) {
        String cc = countryCode.toUpperCase(Locale.ROOT);

        int len = playerName.length();
        int lenDigit = len > 9 ? 0 : len;

        String prefix = bankPrefix.toUpperCase(Locale.ROOT);
        if (prefix.length() != 4) {
            throw new IllegalArgumentException("Bank prefix must be 4 letters");
        }
        char typeChar = switch (kind) {
            case CREDIT -> 'C';
            case DEBIT -> 'D';
            case SAVINGS -> 'E';
        };
        String accountIdStr = String.format("%06d", accountId);
        return cc + lenDigit + prefix + typeChar + accountIdStr;
    }
}
