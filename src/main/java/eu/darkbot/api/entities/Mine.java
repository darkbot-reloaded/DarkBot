package eu.darkbot.api.entities;

public interface Mine extends Entity {

    /**
     * @return mine type id
     */
    int getTypeId();

    default Mine.Type getType() {
        return Type.of(getTypeId());
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
        TITANIC(6), //emperor sibeleon's mine?
        SLOWDOWN(7),
        INFECTION(17),
        MEGA(19), //pet's mega mine
        CURCUBITOR(20);

        private final int[] ids;

        Type(int... ids) {
            this.ids = ids;
        }

        private static Type of(int typeId) {
            for (Type type : values())
                for (int id : type.ids)
                    if (id == typeId) return type;

            return UNKNOWN;
        }
    }
}
