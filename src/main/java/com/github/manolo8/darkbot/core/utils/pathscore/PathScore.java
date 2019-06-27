//package com.github.manolo8.darkbot.core.utils.pathscore;
//
//import com.github.manolo8.darkbot.core.entities.Entity;
//import com.github.manolo8.darkbot.core.entities.Npc;
//import com.github.manolo8.darkbot.core.manager.MapManager;
//
//import java.utils.List;
//import java.utils.Random;
//
//public class PathScore {
//
//    private final Entity main;
//    public final ScorePoint[] points;
//    private final List<Npc> npcs;
//    private final Random random;
//
//    private final int size;
//    private final int space;
//    private final int fix;
//
//    private int width;
//    private int height;
//
//    private Npc target;
//
//    public PathScore(Entity main, List<Npc> npcs, int size, int space) {
//        this.main = main;
//        this.npcs = npcs;
//        this.size = size;
//        this.space = space;
//        this.fix = size * space / 2;
//        this.random = new Random();
//        this.points = new ScorePoint[size * size];
//
//        for (int i = 0; i < points.length; i++)
//            points[i] = new ScorePoint(0, 0);
//    }
//
//    public void recalculate() {
//
//        width = MapManager.internalWidth;
//        height = MapManager.internalHeight;
//
//        for (int i = 0; i < points.length; i++) {
//            ScorePoint point = points[i];
//
//            calculatePointPosition(point, i);
//            calculateMobInRange(point);
//            calculateDistanceFromHero(point);
//            calculateTargetInRange(point);
//            calculateCenterDistance(point);
//            calculateDistanceOutOfMap(point);
//        }
//    }
//
//    private void calculatePointPosition(ScorePoint point, int i) {
//        point.x = (i % size * space) + (int) main.locationInfo.x - fix;
//        point.y = (i / size * space) + (int) main.locationInfo.y - fix;
//    }
//
//    private void calculateCenterDistance(ScorePoint point) {
//        point.distanceOutOfMap = (int) point.distance(5000, 5000);
//    }
//
//    private void calculateDistanceFromHero(ScorePoint point) {
//        point.distanceHero = (int) point.distance(main.locationInfo);
//    }
//
//    private void calculateMobInRange(ScorePoint point) {
//        point.mobsInRange = 0;
//        for (Npc npc : npcs) {
//            double distance = point.distance(npc.locationInfo);
//            if (distance < npc.npcInfo.radius)
//                point.mobsInRange += distance / npc.npcInfo.radius;
//        }
//    }
//
//    private void calculateTargetInRange(ScorePoint point) {
//        point.targetDistance = (int) point.distance(target.locationInfo);
//    }
//
//    private void calculateDistanceOutOfMap(ScorePoint point) {
//        int x = point.x;
//        int y = point.y;
//
//        if (x < 0 && y < 0) {
//            point.distanceOutOfMap = (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
//        } else if (x > width && y > height) {
//            point.distanceOutOfMap = (int) Math.sqrt(Math.pow(x - width, 2) + Math.pow(y - height, 2));
//        } else if (x < 0 && y > height) {
//            point.distanceOutOfMap = (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y - height, 2));
//        } else if (x > width && y < 0) {
//            point.distanceOutOfMap = (int) Math.sqrt(Math.pow(x - width, 2) + Math.pow(y, 2));
//        } else if (x > width) {
//            point.distanceOutOfMap = x - width;
//        } else if (y > height) {
//            point.distanceOutOfMap = y - height;
//        } else if (x < 0) {
//            point.distanceOutOfMap = -x;
//        } else if (y < 0) {
//            point.distanceOutOfMap = -y;
//        } else {
//            point.distanceOutOfMap = 0;
//        }
//
//    }
//
//    public ScorePoint findBest() {
//
//        int score = 0;
//        ScorePoint best = null;
//
//        for (ScorePoint point : points) {
//
//            int cs = point.score(target.npcInfo.radius);
//
//            if (point.distanceHero < space * 2)
//                continue;
//
//            if (best == null || cs >= score) {
//
//                if (best != null && cs == score && best.targetDistance < point.targetDistance)
//                    continue;
//
//                score = cs;
//                best = point;
//            }
//
//        }
//
//
//        return best;
//    }
//
//    public void setTarget(Npc target) {
//        this.target = target;
//    }
//}
