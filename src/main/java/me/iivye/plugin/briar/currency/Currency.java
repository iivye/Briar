package me.iivye.plugin.briar.currency;

import java.util.UUID;

public interface Currency {
    /**
     * @return unique ID for configuration use
     */
    String getId();

    /** Initialize the currency if needed */
    void load();

    /** @return true if the currency can be loaded */
    boolean canLoad();

    /**
     * @param player player's unique ID
     * @param price  amount to check
     * @return true if the player can afford it
     */
    boolean canPlayerAfford(UUID player, double price);

    /**
     * Withdraws the specified amount from the player
     *
     * @param player player's unique ID
     * @param amount amount to withdraw
     */
    void withdraw(UUID player, double amount);

    /**
     * Adds the specified amount to the player's balance.
     *
     * @param player player's unique ID
     * @param amount amount to add
     */
    void deposit(UUID player, double amount);

    /**
     * @param amount currency amount
     * @return formatted name based on the amount
     */
    String getName(double amount);
}



