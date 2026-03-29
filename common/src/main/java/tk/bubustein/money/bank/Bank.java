package tk.bubustein.money.bank;

import java.util.UUID;

public class Bank {
    private UUID ownerUuid;
    private String prefix;    // 4 litere (BSTN etc.)
    private String name;      // nume bancă afișat
    private boolean active = true;

    public Bank(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }

    public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
