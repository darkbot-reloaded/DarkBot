package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.types.suppliers.OptionList;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.objects.Map;
import eu.darkbot.api.API;
import eu.darkbot.api.config.annotations.Dropdown;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StarManager implements API.Singleton {
    public static final String[] HOME_MAPS = new String[]{"1-1", "2-1", "3-1"};
    public static final String[] OUTPOST_HOME_MAPS = new String[]{"1-8", "2-8", "3-8"};
    public static final String[] BASE_MAPS = new String[]{"1-1", "1-8", "2-1", "2-8", "3-1", "3-8"};
    public static final String[] BL_MAPS = new String[]{"1BL", "2BL", "3BL"};

    private static StarManager INSTANCE;
    private final Graph<Map, Portal> starSystem;

    private static int INVALID_MAP_ID = -999;

    private StarManager() {
        if (INSTANCE == null) INSTANCE = this;
        // https://www.darkorbit.com/spacemap/graphics/maps-config.xml

        StarBuilder mapBuild = new StarBuilder();
        mapBuild.addMap(-1, "Loading", "?")
                .addMap(-2, "Home Map").addPortal(0, 0, "1-1").addPortal(0, 0, "2-1").addPortal(0, 0, "3-1")
                .addGG(-3, "GG Escort").accessOnlyBy(54, 10500, 6500, BASE_MAPS)
                .addGG(-4, "GG Eternal").accessBy(54, BL_MAPS)
                .addGG(-5, "Labyrinth").accessBy(83, HOME_MAPS)
                .addGG(-6, "GG Astral").accessOnlyBy(86, 10500, 5800, HOME_MAPS);
                // MMO
        mapBuild.addMap(1, "1-1").addPortal(18500, 11500, "1-2").addPortal(10500, 6750, "Experiment Zone 1")
                .addMap(2, "1-2").addPortal(2000, 2000, "1-1").addPortal(18500, 2000, "1-3").addPortal(18500, 11500, "1-4")
                .addMap(3, "1-3").addPortal(2000, 11500, "1-2").addPortal(18500, 11500, "1-4").addPortal(18500, 2000, "2-3")
                .addMap(4, "1-4").addPortal(2000, 2000, "1-2").addPortal(18500, 2000, "1-3").addPortal(19000, 6000, "4-1").addPortal(18500, 11500, "3-4")
                .addMap(17, "1-5").addPortal(19000, 6000, "4-4").addPortal(10000, 12000, "4-5").addPortal(2000, 2000, "1-6").addPortal(2000, 11500, "1-7").addPortal(10500, 6750, "Experiment Zone 2-1")
                .addMap(18, "1-6").addPortal(18500, 11500, "1-5").addPortal(2000, 11500, "1-8")
                .addMap(19, "1-7").addPortal(2000, 2000, "1-8").addPortal(18500, 2000, "1-5")
                .addMap(20, "1-8").addPortal(18500, 2000, "1-6").addPortal(18500, 11500, "1-7").addPortal(11084, 11084, "1BL");
                // EIC
        mapBuild.addMap(5, "2-1").addPortal(2000, 11500, "2-2").addPortal(10500, 6750, "Experiment Zone 1")
                .addMap(6, "2-2").addPortal(2000, 11500, "2-3").addPortal(18500, 11500, "2-4").addPortal(18500, 2000, "2-1")
                .addMap(7, "2-3").addPortal(2000, 11500, "1-3").addPortal(18500, 11500, "2-4").addPortal(18500, 2000, "2-2")
                .addMap(8, "2-4").addPortal(2000, 2000, "2-2").addPortal(18500, 2000, "2-3").addPortal(2000, 11500, "3-3").addPortal(10000, 12000, "4-2")
                .addMap(21, "2-5").addPortal(2000, 11500, "4-4").addPortal(18500, 11500, "4-5").addPortal(2000, 2000, "2-6").addPortal(18500, 2000, "2-7").addPortal(10500, 6750, "Experiment Zone 2-2")
                .addMap(22, "2-6").addPortal(2000, 11500, "2-5").addPortal(18500, 2000, "2-8")
                .addMap(23, "2-7").addPortal(2000, 11500, "2-5").addPortal(18500, 2000, "2-8")
                .addMap(24, "2-8").addPortal(2000, 11500, "2-6").addPortal(18500, 11500, "2-7").addPortal(11084, 11084, "2BL");
                // VRU
        mapBuild.addMap(9, "3-1").addPortal(2000, 2000, "3-2").addPortal(10500, 6750, "Experiment Zone 1")
                .addMap(10, "3-2").addPortal(18500, 2000, "3-3").addPortal(2000, 2000, "3-4").addPortal(18500, 11500, "3-1")
                .addMap(11, "3-3").addPortal(2000, 2000, "2-4").addPortal(2000, 11500, "3-4").addPortal(18500, 11500, "3-2")
                .addMap(12, "3-4").addPortal(2000, 2000, "1-4").addPortal(10000, 1500, "4-3").addPortal(18500, 2000, "3-3").addPortal(18500, 11500, "3-2")
                .addMap(25, "3-5").addPortal(2000, 2000, "4-4").addPortal(16500, 1500, "4-5").addPortal(2000, 11500, "3-6").addPortal(18500, 11500, "3-7").addPortal(10500, 6750, "Experiment Zone 2-3")
                .addMap(26, "3-6").addPortal(2000, 2000, "3-5").addPortal(18500, 11500, "3-8")
                .addMap(27, "3-7").addPortal(2000, 11500, "3-5").addPortal(18500, 11500, "3-8")
                .addMap(28, "3-8").addPortal(2000, 2000, "3-7").addPortal(2000, 11500, "3-6").addPortal(11084, 11084, "3BL");
                // B-MAPS
        mapBuild.addMap(13, "4-1").addPortal(1500, 6000, "1-4").addPortal(18500, 2000, "4-2").addPortal(18500, 11500, "4-3").addPortal(10500, 6750, "4-4")
                .addMap(14, "4-2").addPortal(10000, 1500, "2-4").addPortal(2000, 11500, "4-1").addPortal(18500, 11500, "4-3").addPortal(10500, 6750, "4-4")
                .addMap(15, "4-3").addPortal(19000, 6000, "3-4").addPortal(2000, 2000, "4-2").addPortal(2000, 11500, "4-1").addPortal(10500, 6750, "4-4")
                .addMap(16, "4-4").addPortal(7000, 13500, "1-5").addPortal(28000, 1376, "2-5").addPortal(28000, 25124, "3-5").addPortal(19200, 13500, "4-1").addPortal(21900, 11941, "4-2").addPortal(21900, 14559, "4-3")
                .addMap(29, "4-5").addPortal(7000, 13500, "1-5").addPortal(28000, 1376, "2-5").addPortal(28000, 25624, "3-5").addPortal(12200, 13300, "5-1").addPortal(25000, 6300, "5-1").addPortal(25000, 20700, "5-1");
                // Pirates
        mapBuild.addMap(91, "5-1").addPortal(5200, 6800, "5-2").addPortal(2900, 13500, "5-2").addPortal(5200, 20600, "5-2")
                .addMap(92, "5-2").addPortal(2800, 3600, "5-3").addPortal(1300, 6750, "5-3").addPortal(2800, 10900, "5-3")
                    .addPortal(19300, 1400, "5-4", 1).addPortal(19300, 3400, "5-4", 2).addPortal(19300, 5400, "5-4", 3)
                .addMap(93, "5-3").addPortal(2000, 9500, "4-4", 1).addPortal(2000, 13500, "4-4", 2).addPortal(2000, 17500, "4-4", 3)
                // Workaround, make 5-4 exit in 5-2 to avoid being used as a shortcut from 5-2 to 4-4
                .addMap(94, "5-4").addPortal(3000, 2000, "5-2", 1).addPortal(3000, 3500, "5-2", 2).addPortal(3000, 5000, "5-2", 3);
                // BL
        mapBuild.addMap(306, "1BL").addPortal( 786, 11458, "1-8").addPortal( 7589,  1456, "2BL").addPortal(20072, 11732, "3BL")
                .addMap(307, "2BL").addPortal(9893,   862, "1BL").addPortal(  593,  5884, "2-8").addPortal(20377,  7996, "3BL")
                .addMap(308, "3BL").addPortal(1545, 12210, "1BL").addPortal(19400, 11854, "2BL").addPortal(14027,  3181, "3-8");
                // EX
        mapBuild.addMap(401, "Experiment Zone 1", "EZ 1").addPortal(1200, 1100, "Home Map")
                .addMap(402, "Experiment Zone 2-1", "EZ 2-1").addPortal(1200, 1100, "1-5")
                .addMap(403, "Experiment Zone 2-2", "EZ 2-2").addPortal(1200, 1100, "2-5")
                .addMap(404, "Experiment Zone 2-3", "EZ 2-3").addPortal(1200, 1100, "3-5");
                // GG
        mapBuild.addGG(51, "GG α").accessBy(2, HOME_MAPS).exitBy(1)
                .addGG(52, "GG β").accessBy(3, HOME_MAPS).exitBy(1)
                .addGG(53, "GG γ").accessBy(4, HOME_MAPS).exitBy(1)
                .addGG(54, "GG NC") // New Client GG        (No access)
                .addGG(55, "GG δ").accessBy(5, HOME_MAPS).exitBy(1)
                .addGG(56, "GG Orb")// The forgotten gate   (No access)
                .addGG(57, "GG Y6").accessBy(16, HOME_MAPS) // Year 6 (Anniversary)
                .addGG(58, "HSG")   // High Score Gate      (No access)
                .addGG(70, "GG ε").accessBy(53, HOME_MAPS).exitBy(1)
                .addGG(71, "GG ζ 1").accessOnlyBy(54, HOME_MAPS).exitBy(1)
                .addGG(72, "GG ζ 2").accessOnlyBy(54, "GG ζ 1").exitBy(1)
                .addGG(73, "GG ζ 3").accessBy(54, "GG ζ 2").exitBy(1)
                .addGG(74, "GG κ").accessBy(70, HOME_MAPS) // TODO: Find portal exit ids for kappa, lambda, kronos
                .addGG(75, "GG λ").accessBy(71, HOME_MAPS)
                .addGG(76, "GG Kronos", "GG K").accessBy(72, HOME_MAPS)
                .addGG(77, "GG Cold Wave Easy", "GG Cold E").accessBy(73, "1-4", "2-4", "3-4")
                .addGG(78, "GG Cold Wave Hard", "GG Cold H").accessBy(73, "1-4", "2-4", "3-4")
                .addGG(203, "GG Hades", "Hades").accessBy(74, HOME_MAPS).exitBy(1)
                .addGG(223, "Devolarium Attack", "DA").accessOnlyBy(34, HOME_MAPS)
                .addGG(225, "GG PET Attack Easy", "GG PET E") // (No access)
                .addGG(226, "GG PET Attack Hard", "GG PET H") // (No access)
                .addGG(228, "Permafrost Fissure", "GG PF")   // (No access), missing type ID (HOME_MAPS)
                .addGG(300, "GG ς 1").accessOnlyBy(82, HOME_MAPS)
                .addGG(301, "GG ς 2").accessOnlyBy(82, "GG ς 1")
                .addGG(302, "GG ς 3").accessOnlyBy(82, "GG ς 2")
                .addGG(303, "GG ς 4").accessOnlyBy(82, "GG ς 3")
                .addGG(304, "GG ς 5").accessBy(82, "GG ς 4")
                .addGG(200, "LoW").accessOnlyBy(34, "1-3", "2-3", "3-3")
                .addGG(229, "Quarantine Zone", "QZ").accessOnlyBy(84, "1-7", "2-7", "3-7")
                .addGG(227, "GG VoT 1").accessOnlyBy(81, "1-4", "2-4", "3-4")
                .addGG(230, "GG VoT 2").accessOnlyBy(81, "GG VoT 1")
                .addGG(231, "GG VoT 3").accessOnlyBy(81, "GG VoT 2")
                .addGG(232, "GG VoT 4").accessOnlyBy(81, "GG VoT 3")
                .addGG(233, "GG VoT 5").accessOnlyBy(81, "GG VoT 4")
                .addGG(234, "GG VoT 6").accessOnlyBy(81, "GG VoT 5")
                .addGG(235, "GG VoT 7").accessOnlyBy(81, "GG VoT 6")
                .addGG(236, "GG VoT 8").accessBy(81, "GG VoT 7")
                .addGG(305, "Compromising Invasion", "CI") // (No access)
                .addGG(410, "GoP Normal 1", "GoP N").accessOnlyBy(24, OUTPOST_HOME_MAPS)
                .addGG(411, "GoP Normal 2", "GoP N").accessOnlyBy(24, "GoP Normal 1")
                .addGG(412, "GoP Normal 3", "GoP N").accessOnlyBy(24, "GoP Normal 2")
                .addGG(413, "GoP Normal 4", "GoP N").accessOnlyBy(24, "GoP Normal 3")
                .addGG(414, "GoP Normal 5", "GoP N").accessOnlyBy(24, "GoP Normal 4")
                .addGG(415, "GoP Normal Final", "GoP N").accessOnlyBy(24, "GoP Normal 5").exitBy(1)
                .addGG(469, "Plutus' Trove of Riches Normal", "PToR N").accessOnlyBy(24, "GoP Normal Final").exitBy(1)
                .addGG(450, "GoP Easy 1", "GoP E").accessOnlyBy(235, HOME_MAPS)
                .addGG(451, "GoP Easy 2", "GoP E").accessOnlyBy(235, "GoP Easy 1")
                .addGG(452, "GoP Easy 3", "GoP E").accessOnlyBy(235, "GoP Easy 2")
                .addGG(453, "GoP Easy 4", "GoP E").accessOnlyBy(235, "GoP Easy 3")
                .addGG(454, "GoP Easy 5", "GoP E").accessOnlyBy(235, "GoP Easy 4")
                .addGG(455, "GoP Easy Final", "GoP E").accessOnlyBy(235, "GoP Easy 5").exitBy(1)
                .addGG(470, "Plutus' Trove of Riches Easy", "PToR E").accessOnlyBy(235, "GoP Easy Final").exitBy(1)
                .addGG(471, "Treacherous Domain Easy", "TD E").accessOnlyBy(238, HOME_MAPS)
                .addGG(472, "Treacherous Domain Normal", "TD N").accessOnlyBy(238, OUTPOST_HOME_MAPS);
                // Special (No direct access)
        mapBuild.addMap(42, "???")
                .addMap(61, "MMO Invasion", "MMO Inv").addMap(62, "EIC Invasion", "EIC Inv").addMap(63, "VRU Invasion", "VRU Inv")
                .addMap(64, "MMO Invasion", "MMO Inv").addMap(65, "EIC Invasion", "EIC Inv").addMap(66, "VRU Invasion", "VRU Inv")
                .addMap(67, "MMO Invasion", "MMO Inv").addMap(68, "EIC Invasion", "EIC Inv").addMap(69, "VRU Invasion", "VRU Inv")
                .addMap(81, "TDM I").addMap(82, "TDM II")
                .addMap(101, "JP").addMap(102, "JP").addMap(103, "JP").addMap(104, "JP")
                .addMap(105, "JP").addMap(106, "JP").addMap(107, "JP").addMap(108, "JP")
                .addMap(109, "JP").addMap(110, "JP").addMap(111, "JP")
                .addMap(112, "UBA").addMap(113, "UBA").addMap(114, "UBA").addMap(115, "UBA").addMap(116, "UBA")
                .addMap(117, "UBA").addMap(118, "UBA").addMap(119, "UBA").addMap(120, "UBA").addMap(121, "UBA")
                .addMap(201, "SC-1").addMap(202, "SC-2") // Sector Control
                .addMap(224, "Custom Tournament", "CT")
                .addMap(150, "R-Zone 1", "R-Z").addMap(151, "R-Zone 2", "R-Z")
                .addMap(152, "R-Zone 3", "R-Z").addMap(153, "R-Zone 4", "R-Z")
                .addMap(154, "R-Zone 5", "R-Z").addMap(155, "R-Zone 6", "R-Z")
                .addMap(156, "R-Zone 7", "R-Z").addMap(157, "R-Zone 8", "R-Z")
                .addMap(158, "R-Zone 9", "R-Z").addMap(159, "R-Zone 10", "R-Z")
                .addMap(420, "WarGame 1", "WG").addMap(421, "WarGame 2", "WG").addMap(422, "WarGame 3", "WG")
                .addMap(423, "WarGame 4", "WG").addMap(424, "WarGame 5", "WG").addMap(425, "WarGame 6", "WG");

        var currMonth = LocalDate.now().getMonth();

        if (currMonth == Month.DECEMBER || currMonth == Month.JANUARY) {
            // Frozen laberynth
            mapBuild.addMap(430, "ATLAS A"  ).exitBy(55)
                    .addMap(431, "ATLAS B"  ).exitBy(55)
                    .addMap(432, "ATLAS C"  ).exitBy(55)
                    .addMap(433, "Cygni"    ).exitBy(55)
                    .addMap(434, "Helvetios").exitBy(55)
                    .addMap(435, "Eridani"  ).exitBy(55)
                    .addMap(436, "Sirius"   ).exitBy(55)
                    .addMap(437, "Sadatoni" ).exitBy(55)
                    .addMap(438, "Persei"   ).exitBy(55)
                    .addMap(439, "Volantis" ).exitBy(55)
                    .addMap(440, "Alcyone"  ).exitBy(55)
                    .addMap(441, "Auriga"   ).exitBy(55)
                    .addMap(442, "Bootes"   ).exitBy(55)
                    .addMap(443, "Aquila"   ).exitBy(55)
                    .addMap(444, "Orion"    ).exitBy(55)
                    .addMap(445, "Maia"     ).exitBy(55);
        } else {
            // Mimesis escort maps
            mapBuild.addGG(430, "Escort VRU 1", "ESC-V1").exitBy(1)
                    .addGG(431, "Escort VRU 2", "ESC-V2").exitBy(1)
                    .addGG(432, "Escort VRU 3", "ESC-V3").exitBy(1)
                    .addGG(1439, "Escort VRU 4", "ESC-V4").exitBy(1)
                    .addGG(1440, "Escort VRU 5", "ESC-V5").exitBy(1)
                    .addGG(1441, "Escort VRU 6", "ESC-V6").exitBy(1)
                    .addGG(1442, "Escort VRU 7", "ESC-V7").exitBy(1)
                    .addGG(1443, "Escort VRU 8", "ESC-V8").exitBy(1)
                    .addGG(1444, "Escort VRU 9", "ESC-V9").exitBy(1)
                    .addGG(1445, "Escort VRU 10", "ESC-V10").exitBy(1)
                    .addGG(433, "Escort MMO 1", "ESC-M1").exitBy(1)
                    .addGG(434, "Escort MMO 2", "ESC-M2").exitBy(1)
                    .addGG(435, "Escort MMO 3", "ESC-M3").exitBy(1)
                    .addGG(1446, "Escort MMO 4", "ESC-M4").exitBy(1)
                    .addGG(1447, "Escort MMO 5", "ESC-M5").exitBy(1)
                    .addGG(1448, "Escort MMO 6", "ESC-M6").exitBy(1)
                    .addGG(1449, "Escort MMO 7", "ESC-M7").exitBy(1)
                    .addGG(1450, "Escort MMO 8", "ESC-M8").exitBy(1)
                    .addGG(1451, "Escort MMO 9", "ESC-M9").exitBy(1)
                    .addGG(1452, "Escort MMO 10", "ESC-M10").exitBy(1)
                    .addGG(436, "Escort EIC 1", "ESC-E1").exitBy(1)
                    .addGG(437, "Escort EIC 2", "ESC-E2").exitBy(1)
                    .addGG(438, "Escort EIC 3", "ESC-E3").exitBy(1)
                    .addGG(1453, "Escort EIC 4", "ESC-E4").exitBy(1)
                    .addGG(1454, "Escort EIC 5", "ESC-E5").exitBy(1)
                    .addGG(1455, "Escort EIC 6", "ESC-E6").exitBy(1)
                    .addGG(1456, "Escort EIC 7", "ESC-E7").exitBy(1)
                    .addGG(1457, "Escort EIC 8", "ESC-E8").exitBy(1)
                    .addGG(1458, "Escort EIC 9", "ESC-E9").exitBy(1)
                    .addGG(1459, "Escort EIC 10", "ESC-E10").exitBy(1);

            // Eternal gate event
            mapBuild.addGG(439, "Eternal Gate", "GG ∞")
                    .addGG(440, "Eternal Gate", "GG ∞")
                    .addGG(441, "Eternal Gate", "GG ∞")
                    .addGG(442, "Eternal Gate", "GG ∞")
                    .addGG(443, "Eternal Gate", "GG ∞")
                    .addGG(444, "Eternal Gate", "GG ∞")
                    .addGG(445, "Eternal Gate", "GG ∞");
        }

        // Eternal BL maps
        mapBuild.addGG(460, "Eternal Blacklight", "GG ∞ BL")
                .addGG(461, "Eternal Blacklight", "GG ∞ BL")
                .addGG(462, "Eternal Blacklight", "GG ∞ BL")
                .addGG(463, "Eternal Blacklight", "GG ∞ BL")
                .addGG(464, "Eternal Blacklight", "GG ∞ BL")
                .addGG(465, "Eternal Blacklight", "GG ∞ BL");

        mapBuild.addGG(466, "Astral Ascension", "GG Astral")
                .addGG(467, "Astral Ascension", "GG Astral")
                .addGG(468, "Astral Ascension", "GG Astral");
        starSystem = mapBuild.build();

        //org.jgrapht.io.DOTExporter<Map, Portal> exporter = new org.jgrapht.io.DOTExporter<>(m -> (m.id < 0 ? "00" : "") + Math.abs(m.id), Map::toString, Portal::toString);
        //Writer writer = new StringWriter();
        //exporter.exportGraph(starSystem, writer);
        //System.out.println(writer.toString());
    }

    public Portal getOrCreate(int id, int type, int x, int y) {
        Portal portal = starSystem.outgoingEdgesOf(Main.INSTANCE.hero.map).stream()
                .filter(p -> p.matches(x, y, type))
                .findFirst()
                .orElse(null);
        // We have no idea of the portal this is, just add it
        if (portal == null) return new Portal(id, type, x, y);
        if (portal.removed) { // Reuse portal, assign id and return
            portal.id = id;
            return portal;
        } else return new Portal(id, type, x, y, portal.target, portal.factionId); // Create a copy portal
    }

    public Portal next(HeroManager hero, Map target) {
        DijkstraShortestPath<Map, Portal> path = new DijkstraShortestPath<>(starSystem);
        return hero.main.mapManager.entities.portals.stream()
                .filter(p -> !p.removed && p.target != null)
                .filter(p -> target.gg || !p.target.gg)
                .min(Comparator.<Portal>comparingDouble(p -> path.getPaths(p.target).getWeight(target))
                                .thenComparing(p -> p.factionId == -1 ? 0 : p.factionId == hero.playerInfo.factionId ? -1 : 1)
                                .thenComparing(p -> hero.locationInfo.distance(p.locationInfo))).orElse(null);
    }

    public Stream<Map> mapSet() {
        return starSystem.vertexSet().stream();
    }

    public Collection<Map> getMaps() {
        return starSystem.vertexSet();
    }

    public Map byId(int id) {
        return starSystem.vertexSet().stream().filter(m -> m.id == id).findAny()
                .orElseGet(() -> addMap(new Map(id, "Unknown map " + id, false, false)));
    }

    public Map byName(String name) {
        return starSystem.vertexSet().stream().filter(m -> m.name.equals(name)).findAny()
                .orElseGet(() -> addMap(new Map(--INVALID_MAP_ID, name, false, false)));
    }

    private Map addMap(Map map) {
        starSystem.addVertex(map);
        return map;
    }

    public List<String> getAccessibleMaps() {
        return starSystem.vertexSet().stream()
                .filter(m -> !m.gg && m.id > 0)
                .filter(m -> starSystem.inDegreeOf(m) > 0)
                .map(m -> m.name).sorted().collect(Collectors.toList());
    }

    public List<Integer> getAccessibleMapIds() {
        return starSystem.vertexSet().stream()
                .filter(m -> !m.gg && isAccessible(m))
                .sorted(Comparator.comparing(map -> map.name))
                .map(m -> m.id).collect(Collectors.toList());
    }

    public Collection<String> getGGMaps() {
        return starSystem.vertexSet().stream()
                .filter(m -> m.gg)
                .filter(m -> starSystem.inDegreeOf(m) > 0 &&
                        (starSystem.outDegreeOf(m) == 0 || starSystem.containsEdge(m, m)))
                .map(m -> m.name)
                .sorted().collect(Collectors.toList());
    }

    public boolean isAccessible(Map map) {
        return map.id > 0 && starSystem.inDegreeOf(map) > 0;
    }

    public static Collection<Map> getAllMaps() {
        return getInstance().starSystem.vertexSet();
    }

    public static StarManager getInstance() {
        if (INSTANCE == null) INSTANCE = new StarManager();
        return INSTANCE;
    }

    @Deprecated
    public static class MapList extends OptionList<Integer> {

        @Override
        public Integer getValue(String text) {
            return getInstance().byName(text).id;
        }

        @Override
        public String getText(Integer value) {
            return getInstance().byId(value).name;
        }

        @Override
        public List<String> getOptions() {
            return getInstance().getAccessibleMaps();
        }
    }

    public static class MapOptions implements Dropdown.Options<Integer> {

        private final StarManager star;
        private final List<Integer> accessibleMaps;

        public MapOptions(StarManager star) {
            this.star = star;
            this.accessibleMaps = star.getAccessibleMapIds();
        }

        @Override
        public List<Integer> options() {
            return accessibleMaps;
        }

        @Override
        public @NotNull String getText(Integer option) {
            if (option == null) return "";
            return star.byId(option).getName();
        }

    }

}
