package battlecode2017;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class BotUtils {

	static Direction randomDirection() throws GameActionException {
        float k = ((float) Math.random() * (float) 2 * (float) Math.PI) - (float) Math.PI;
        return new Direction(k);
    }
	
    /**find intersection between two circles
    *
    * @param center0 center of first circle
    * @param center1 cneter of second circle
    * @param radius0 radius of 1st circle
    * @param radius1 radius of 2nd circle
    *
    * @return MapLocation[] of the intersection point(s) or null if they don't exist
    *
    * @throws GameActionException
    */
   public static MapLocation[] findCircleIntersections(MapLocation center0, MapLocation center1, float radius0, float radius1) throws GameActionException {
       float d = center0.distanceTo(center1);
       if (d > radius0+radius1) return null;
       if (d == radius0+radius1) {
           MapLocation[] onePoint = {center0.add(center0.directionTo(center1), radius0)};
           return onePoint;
       }
       if (center0.equals(center1)) return null;
       else {
           float a = (radius0*radius0 -radius1*radius1 +d*d) / (2*d);
           float h = (float) Math.sqrt((double) radius0*radius0 - a*a);
           MapLocation p2 = center0.add(center0.directionTo(center1), radius0);
           float x3_1 = p2.x + h * (center1.y - center0.y) / d;
           float y3_1 = p2.y - h * (center1.x - center0.x) / d;
           float x3_2 = p2.x - h * (center1.y - center0.y) / d;
           float y3_2 = p2.y + h * (center1.x - center0.x) / d;
           MapLocation[] twoPoints = {new MapLocation(x3_1, y3_1), new MapLocation(x3_2, y3_2)};
           return twoPoints;
       }
   }

}
