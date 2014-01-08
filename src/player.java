
public class player {
        int xpos;
        int ypos;
        int speed;
        int health;
        int score;
        int lives;
        int shots;
        
        int deadCounter;
        
        boolean [] direction;
        int lastDirection;
        boolean [] interact;
        boolean [] moving;
        boolean [] shoot;
        boolean falling;
        boolean gold;
        boolean exception;
        boolean chase;
        boolean ladder;
        
        int walkAnimation;
        int ladderAnimation;
        int barAnimation;
        int fallAnimation;
        
        public player (int x, int y) {
                xpos = x;
                ypos = y;
                score = 0;
                lives = 3;
                shots = 0;
                
                deadCounter = 0;
                
                direction = new boolean [4];
                direction[0] = false; // right
                direction[1] = false; // left
                direction[2] = false; // up
                direction[3] = false; // down
                
                lastDirection = 0; // 0 = right, 1 = left, 2 = up, 3 = down
                
                
                interact = new boolean [8];
                interact[0] = false; // air
                interact[1] = true; // ground
                interact[2] = false; // bar
                interact[3] = false; // ladders
                interact[4] = false; // hole
                interact[5] = false; // right wall
                interact[6] = false; // left wall
                interact[7] = false; // lateral on ladders
                
                
                moving = new boolean [4];
                moving[0] = false; // right
                moving[1] = false; // left
                moving[2] = false; // up
                moving[3] = false; //down
                
                falling = false;
                gold = false; // ENEMY ONLY
                exception = false; // ENEMY ONLY
                chase = false; // ENEMY ONLY
                ladder = false; // ENEMY ONLY
                
                
                shoot = new boolean [2]; // PLAYER ONLY
                shoot[0] = false; //right // PLAYER ONLY
                shoot[1] = false; //left // PLAYER ONLY
                
                walkAnimation = 1;
                ladderAnimation = 1;
                barAnimation = 1;
                fallAnimation = 1;
                                
        }
}

