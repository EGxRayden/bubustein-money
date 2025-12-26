package tk.bubustein.money.bank;

import org.jetbrains.annotations.NotNull;
import tk.bubustein.money.MoneyMod;

import java.math.BigInteger;
import java.util.Locale;

public class IbanGenerator {

    public static String generateIban(String countryCode,
                                      String bankPrefix,
                                      AccountKind kind,
                                      int accountId) {
        String cc = countryCode.toUpperCase(Locale.ROOT);
        if (cc.length() != 2 || !cc.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Country code must be 2 letters: " + countryCode);
        }

        String bban = buildBban(bankPrefix, kind, accountId);
        int checkDigits = calculateCheckDigits(bban, cc);
        String checkStr = String.format("%02d", checkDigits);

        String iban = cc + checkStr + bban;
        MoneyMod.LOGGER.debug("[{}] Generated IBAN: {}", MoneyMod.MOD_ID, iban);

        return iban;
    }
    private static @NotNull String buildBban(String bankPrefix, AccountKind kind, int accountId) {
        String prefix = bankPrefix.toUpperCase(Locale.ROOT);
        if (prefix.length() != 4 || !prefix.matches("[A-Z]{4}")) {
            throw new IllegalArgumentException("Bank prefix must be 4 letters: " + bankPrefix);
        }
        if (accountId < 0) {
            throw new IllegalArgumentException("Account ID must be non-negative: " + accountId);
        }
        char typeChar = switch (kind) {
            case CREDIT -> 'C';
            case SAVINGS -> 'S';
            case DEBIT -> 'D';
        };
        String accountStr = String.format("%010d", accountId);
        return prefix + typeChar + accountStr;
    }
    private static int calculateCheckDigits(String bban, String countryCode) {
        String rearranged = bban + countryCode + "00";
        StringBuilder numeric = new StringBuilder();
        for (char ch : rearranged.toCharArray()) {
            if (Character.isLetter(ch)) {
                int val = Character.toUpperCase(ch) - 'A' + 10;
                numeric.append(val);
            } else if (Character.isDigit(ch)) {
                numeric.append(ch);
            } else {
                throw new IllegalArgumentException("Invalid character in IBAN: " + ch);
            }
        }
        BigInteger numericValue = new BigInteger(numeric.toString());
        BigInteger mod = numericValue.mod(BigInteger.valueOf(97));

        return 98 - mod.intValue();
    }
    public static boolean validateIban(String iban) {
        if (iban == null) {
            return false;
        }
        iban = iban.toUpperCase().replaceAll("\\s+", "");

        if (!iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]+")) {
            return false;
        }
        if (iban.length() != 19) {
            return false;
        }
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();
        for (char ch : rearranged.toCharArray()) {
            if (Character.isLetter(ch)) {
                int val = ch - 'A' + 10;
                numeric.append(val);
            } else {
                numeric.append(ch);
            }
        }
        try {
            BigInteger numericValue = new BigInteger(numeric.toString());
            BigInteger mod = numericValue.mod(BigInteger.valueOf(97));
            return mod.intValue() == 1;
        } catch (NumberFormatException e) {
            MoneyMod.LOGGER.error("[{}] Invalid IBAN format: {}", MoneyMod.MOD_ID, iban);
            return false;
        }
    }
}