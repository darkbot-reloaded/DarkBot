package com.github.manolo8.darkbot.config.actions.values;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.Value;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.Values;
import com.github.manolo8.darkbot.config.actions.tree.ParsingNode;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.manager.StarManager;
import com.github.manolo8.darkbot.core.objects.Map;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@ValueData(name = "map", description = "Creates a map constant", example = "map(5-2)")
public class MapConstant implements Value<Map>, Parser {

    private static final Set<String> ACCESSIBLE_MAPS =
            new HashSet<>(StarManager.getInstance().getAccessibleMaps());

    private Map map;

    @Override
    public @Nullable Map get(Main main) {
        return map;
    }

    @Override
    public String toString() {
        return "map(" + map.name + ")";
    }

    @Override
    public void parse(ParsingNode node) throws SyntaxException {
        node.requireParamSize(1, getClass());
        String name = node.getParamStr(0);

        map = StarManager.getAllMaps().stream()
                .filter(m -> m.id > 0)
                .filter(m -> m.name.equalsIgnoreCase(name) ||
                        (m.shortName != null && m.shortName.equalsIgnoreCase(name)))
                .findFirst().orElse(null);

        if (map == null)
            throw new SyntaxException("Unknown map: '" + name + "'", node.getParam(0), Values.getMeta(getClass()),
                    StarManager.getAllMaps().stream()
                            .filter(m -> m.id > 0)
                            .filter(m -> HeroManager.instance.map == m || (name.isEmpty() ?
                                    ACCESSIBLE_MAPS.contains(m.name) :
                                    m.name.toLowerCase(Locale.ROOT).contains(name)))
                            .map(m -> m.shortName != null ? m.shortName : m.name)
                            .distinct()
                            .toArray(String[]::new));
    }

}
