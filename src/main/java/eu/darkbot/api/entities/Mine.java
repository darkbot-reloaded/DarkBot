package eu.darkbot.api.entities;

public interface Mine extends Entity {

    /**
     * @return mine type id
     */
    int getTypeId();

    default Mine.Type getType() {
        return Type.get(getTypeId());
    }

    /**
     * Represents {@link Mine} types.
     */
    enum Type {
        UNKNOWN,
        STANDARD(1, 10, 11, 18),
        EMP(2),
        ANTI_SHIELD(3),
        DIRECT_DAMAGE(4),
        PIRATE(5),
        TITANIC(6),
        SLOWDOWN(7),
        PLAGUE(17),
        MEGA(19),
        CURCUBITOR(20);

        private final int[] ids;

        Type(int... ids) {
            this.ids = ids;
        }

        private static Type get(int typeId) {
            for (Type type : values())
                for (int id : type.ids)
                    if (id == typeId) return type;

            return UNKNOWN;
        }
    }
}
