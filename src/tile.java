
public class tile {
        int xpos;
        int ypos;
        int type; // 0 = air, 1 = ground, 2 = bars, 3 = ladders, 4 = destroyed ground
        int gold;
        boolean destroyed;
        boolean thin;
        boolean thinDestroyed;
        boolean indestructible;
        
        public tile (int x, int y, int t) {
                xpos = x;
                ypos = y;
                type = t;
                gold = 0;
                destroyed = false;
                thinDestroyed = false;
                indestructible = false;
        }
}
