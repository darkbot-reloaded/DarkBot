package eu.darkbot.api.entities.other;

/**
 * Represents in-game {@link eu.darkbot.api.entities.Entity}'s effects.
 */
// TODO: 01.11.2020 add more effects
public enum Effect {
    UNDEFINED(-1),
    LOCATOR(1),
    PET_SPAWN(2),
    ENERGY_LEECH(11),
    NPC_ISH(16),
    BOX_COLLECTING(20),
    BOOTY_COLLECTING(21),
    DRAW_FIRE(36),
    ISH(84),
    STICKY_BOMB(56),
    POLARITY_POSITIVE(65),
    POLARITY_NEGATIVE(66),
    INFECTION(85);

    private final int id;

    Effect(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
