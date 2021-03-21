package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@ValueData(name = "in-map", description = "Checks if you are on a specific map", example = "in-map(map)")
public class InMapCondition implements Condition, Parser {

    private static final Set<String> ACCESSIBLE_MAPS = new HashSet<>(StarManager.getInstance().getAccessibleMaps());

    public Map map;

    @Override
    public @NotNull Condition.Result get(Main main) {
        if (map == null) return Result.ABSTAIN;
        return Result.fromBoolean(main.hero.map.name.equals(map.name));
    }

    @Override
    public String toString() {
        return "in-map(" + map.name + ")";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);

        params[0] = params[0].trim().toLowerCase(Locale.ROOT);
        map = StarManager.getAllMaps().stream()
                .filter(m -> m.id > 0)
                .filter(m -> m.name.equalsIgnoreCase(params[0]) ||
                        (m.shortName != null && m.shortName.equalsIgnoreCase(params[0])))
                .findFirst().orElse(null);

        if (map == null)
            throw new SyntaxException("Unknown map: '" + params[0] + "'", str, Values.getMeta(getClass()),
                    StarManager.getAllMaps().stream()
                            .filter(m -> m.id > 0)
                            .filter(m -> HeroManager.instance.map == m || (params[0].isEmpty() ?
                                    ACCESSIBLE_MAPS.contains(m.name) :
                                    m.name.toLowerCase(Locale.ROOT).contains(params[0])))
                            .map(m -> m.shortName != null ? m.shortName : m.name)
                            .distinct()
                            .toArray(String[]::new));

        if (params.length != 2)
            throw new SyntaxException("Missing end separator in in-map", "", Values.getMeta(getClass()), ")");

        return params[1];
    }
}
