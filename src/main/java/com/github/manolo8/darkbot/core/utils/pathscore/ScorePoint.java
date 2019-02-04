//package com.github.manolo8.darkbot.core.utils.pathscore;
//
//import com.github.manolo8.darkbot.core.objects.LocationInfo;
//
//import static java.lang.Math.pow;
//import static java.lang.Math.sqrt;
//
//public class ScorePoint {
//
//    public int x;
//    public int y;
//
//    //(=distance/range)
//    public double mobsInRange;
//
//    public int targetDistance;
//
//    public int distanceOutOfMap;
//
//    public int distanceHero;
//
//    public ScorePoint(int x, int y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    public int score(double radius) {
//        int score = 0;
//
//        score -= mobsInRange > 0 ? mobsInRange + 2 * 5.5 : 0;
//
//        if (targetDistance - 50 < radius)
//            if (targetDistance + 50 > radius)
//                score /= 2;
//            else
//                score -= targetDistance * 10;
//        else
//            score -= (targetDistance - radius) * 5;
//
//        score -= distanceHero < 500 ? 0 : distanceHero - 500;
//        score -= distanceOutOfMap * 100;
//
//        return score;
//    }
//
//    public double distance(double x, double y) {
//        return sqrt(pow(this.x - x, 2) + pow(this.y - y, 2));
//    }
//
//    public double distance(LocationInfo o) {
//        return sqrt(pow(x - o.destinationX, 2) + pow(y - o.destinationY, 2));
//    }
//}
