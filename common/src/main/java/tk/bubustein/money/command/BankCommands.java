package tk.bubustein.money.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import tk.bubustein.money.MoneyMod;
import tk.bubustein.money.bank.Bank;
import tk.bubustein.money.bank.BankAccountManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class BankCommands {

    // Pending bank deletions: playerUUID -> prefix awaiting confirmation
    private static final ConcurrentHashMap<UUID, PendingDeletion> pendingDeletions = new ConcurrentHashMap<>();

    private record PendingDeletion(String prefix, long expiresAtMillis) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }

    /** How long (ms) a /bubustein confirm is valid after the warning message. */
    private static final long CONFIRM_WINDOW_MS = 30_000; // 30 seconds

    /** Delay (ticks) before showing the confirmation warning. 3 seconds = 60 ticks. */
    private static final int WARNING_DELAY_TICKS = 60;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bubustein")
                .then(Commands.literal("banks")
                        .executes(context -> listBanks(context.getSource())))

                .then(Commands.literal("bank")
                        .then(Commands.literal("create")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("prefix", StringArgumentType.word())
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(context -> createBank(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "prefix"),
                                                        StringArgumentType.getString(context, "name")
                                                )))))
                        .then(Commands.literal("delete")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("prefix", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            BankAccountManager mgr = BankAccountManager.get();
                                            String input = builder.getRemaining().toUpperCase();
                                            for (String prefix : mgr.getBanks().keySet()) {
                                                if (!mgr.isProtectedBank(prefix) && prefix.startsWith(input)) {
                                                    builder.suggest(prefix);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> requestDeleteBank(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "prefix")
                                        )))))

                .then(Commands.literal("confirm")
                        .executes(context -> confirmDelete(context.getSource())))
        );
    }

    private static int listBanks(CommandSourceStack source) {
        BankAccountManager mgr = BankAccountManager.get();
        Map<String, Bank> banks = mgr.getBanks();

        if (banks.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No banks registered.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        source.sendSuccess(() -> {
            MutableComponent msg = Component.literal("═══════════════════════════════════════")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("\n")
                            .append(Component.translatable("commands.bubusteinmoneymod.banks.title")
                                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)))
                    .append(Component.literal("\n═══════════════════════════════════════")
                            .withStyle(ChatFormatting.GOLD));

            int index = 1;
            for (Map.Entry<String, Bank> entry : banks.entrySet()) {
                Bank bank = entry.getValue();
                String prefix = bank.getPrefix() != null ? bank.getPrefix() : entry.getKey();
                String name = bank.getName() != null ? bank.getName() : "Unknown";
                boolean active = bank.isActive();
                boolean isProtected = mgr.isProtectedBank(prefix);
                String statusIcon = active ? "✓" : "✗";
                ChatFormatting statusColor = active ? ChatFormatting.GREEN : ChatFormatting.RED;

                int finalIndex = index;
                msg.append(Component.literal("\n\n  [" + finalIndex + "] ")
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(prefix)
                                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                        .append(Component.literal(" " + statusIcon)
                                .withStyle(statusColor));
                if (isProtected) {
                    msg.append(Component.literal(" ★")
                            .withStyle(ChatFormatting.GOLD));
                }
                msg.append(Component.literal("\n      ")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(name)
                                .withStyle(ChatFormatting.WHITE));
                index++;
            }

            msg.append(Component.literal("\n\n═══════════════════════════════════════")
                            .withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("\n")
                            .append(Component.translatable("commands.bubusteinmoneymod.banks.total", banks.size())
                                    .withStyle(ChatFormatting.DARK_GRAY)));

            return msg;
        }, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int createBank(CommandSourceStack source, String prefix, String name) {
        prefix = prefix.toUpperCase().trim();

        if (prefix.length() != 4) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.create.invalid_prefix_length", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!prefix.matches("[A-Z]{4}")) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.create.invalid_prefix_chars", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (name == null || name.trim().isEmpty()) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.create.empty_name"
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        BankAccountManager mgr = BankAccountManager.get();

        if (mgr.bankExists(prefix)) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.create.already_exists", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Determine the owner UUID (the player who runs the command, if it's a player)
        UUID ownerUuid = null;
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            ownerUuid = player.getUUID();
        }

        try {
            mgr.registerBank(prefix, name.trim(), ownerUuid);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
            return 0;
        }

        String finalPrefix = prefix;
        String finalName = name.trim();
        source.sendSuccess(() -> Component.translatable(
                "commands.bubusteinmoneymod.bank.create.success", finalPrefix, finalName
        ).withStyle(ChatFormatting.GREEN), true);

        MoneyMod.LOGGER.info("[{}] Admin {} created bank: {} ({})",
                MoneyMod.MOD_ID, source.getTextName(), finalName, finalPrefix);

        return Command.SINGLE_SUCCESS;
    }

    private static int requestDeleteBank(CommandSourceStack source, String prefix) {
        prefix = prefix.toUpperCase().trim();
        BankAccountManager mgr = BankAccountManager.get();

        // Check if bank exists
        if (!mgr.bankExists(prefix)) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.not_found", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Check if it's a protected bank
        if (mgr.isProtectedBank(prefix)) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.protected", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Check ownership: must be the creator, or full admin override isn't allowed for this
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command can only be run by a player.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        Bank bank = mgr.getBank(prefix).orElse(null);
        if (bank == null) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.not_found", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Verify ownership
        if (bank.getOwnerUuid() == null || !bank.getOwnerUuid().equals(player.getUUID())) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.not_owner", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Immediate feedback
        player.sendSystemMessage(Component.translatable(
                "commands.bubusteinmoneymod.bank.delete.processing", prefix
        ).withStyle(ChatFormatting.YELLOW));

        // Schedule the warning message after 3 seconds (60 ticks)
        String finalPrefix = prefix;
        MinecraftServer server = source.getServer();
        UUID playerUuid = player.getUUID();

        CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() ->
                server.execute(() -> {
                    ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerUuid);
                    if (onlinePlayer == null) {
                        return; // Player disconnected
                    }

                    // Store the pending deletion with a 30-second confirmation window
                    pendingDeletions.put(playerUuid, new PendingDeletion(
                            finalPrefix,
                            System.currentTimeMillis() + CONFIRM_WINDOW_MS
                    ));

                    onlinePlayer.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                            .withStyle(ChatFormatting.RED));
                    onlinePlayer.sendSystemMessage(Component.translatable(
                            "commands.bubusteinmoneymod.bank.delete.warning", finalPrefix
                    ).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    onlinePlayer.sendSystemMessage(Component.translatable(
                            "commands.bubusteinmoneymod.bank.delete.confirm_hint"
                    ).withStyle(ChatFormatting.YELLOW));
                    onlinePlayer.sendSystemMessage(Component.literal("═══════════════════════════════════════")
                            .withStyle(ChatFormatting.RED));
                })
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int confirmDelete(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("This command can only be run by a player.")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        UUID playerUuid = player.getUUID();
        PendingDeletion pending = pendingDeletions.remove(playerUuid);

        if (pending == null) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.confirm.nothing_pending"
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        if (pending.isExpired()) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.confirm.expired"
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        BankAccountManager mgr = BankAccountManager.get();
        String prefix = pending.prefix();

        // Double-check protections (in case something changed)
        if (mgr.isProtectedBank(prefix)) {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.protected", prefix
            ).withStyle(ChatFormatting.RED));
            return 0;
        }

        MinecraftServer server = source.getServer();
        tk.bubustein.money.bank.BankAccountSavedData data = tk.bubustein.money.bank.BankAccountSavedData.get(server);

        // Migrate all accounts from the deleted bank to a random protected bank
        String targetPrefix = mgr.getRandomProtectedPrefix();
        String countryCode = tk.bubustein.money.config.ModConfig.getInstance().getServerCountryCode();

        int migratedCount = data.migrateAccountsFromBank(prefix, targetPrefix, countryCode);

        if (migratedCount > 0) {
            player.sendSystemMessage(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.migrated", migratedCount, prefix, targetPrefix
            ).withStyle(ChatFormatting.YELLOW));

            MoneyMod.LOGGER.warn("[BANK DELETE] Migrated {} account(s) from {} to {} before deletion",
                    migratedCount, prefix, targetPrefix);
        }

        boolean removed = mgr.removeBank(prefix);

        if (removed) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.success", prefix
            ).withStyle(ChatFormatting.GREEN), true);

            MoneyMod.LOGGER.warn("[BANK DELETED] Admin {} deleted bank {} ({} accounts migrated to {})",
                    player.getName().getString(), prefix, migratedCount, targetPrefix);
        } else {
            source.sendFailure(Component.translatable(
                    "commands.bubusteinmoneymod.bank.delete.not_found", prefix
            ).withStyle(ChatFormatting.RED));
        }

        return Command.SINGLE_SUCCESS;
    }
}
