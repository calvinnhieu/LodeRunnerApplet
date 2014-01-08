import java.awt.*;
import java.applet.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Font;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
// HOW TO PLAY:
// ARROW KEYS TO MOVE
// Z TO SHOOT BLOCK ON LEFT
// X TO SHOOT BLOCK ON RIGHT
// F1 TO SUICIDE
// P TO UNPAUSE
// SPACEBAR TO PAUSE

// OBJECTIVE:
// COLLECT ALL THE GOLD AND REACH EXIT DOOR

// TRAP ENEMIES IN HOLES BY SHOOTING THE GROUND AND STEAL THEIR GOLD
// ENEMIES WILL EVENTUALLY GET OUT OF THE HOLE...

// BUGS
// ... STUPID AI'S &  YOU CAN'T LAND ON EDGES OF PLATFORMS WHEN FALLING
// SOMETIMES YOU SHOOT AND FALL IN
// IF YOU GO DOWN A LADDER ALL THE WAY, YOU CAN'T GO BACK UP UNLESS YOU MOVE LEFT OR RIGHT FIRST
// YOU CAN'T ALWAYS SHOOT

// NEXT STEPS...
// MAKE AI SMARTER 
// MORE LEVELS/ LEVEL SELECT

// EDITTING GRID @ LINE 1061ISH


public class Main extends Applet implements Runnable, KeyListener {

	private static final long serialVersionUID = 1L;
	static boolean startup = true;
	static int tileSize;
	static int counter;
	static double enemyMoveCounter;
	static boolean door;
	static boolean[] level;
	static boolean pause;
	static Font pauseFont = new Font ("Comic Sans", Font.ITALIC, 20);
	static Font original = new Font ("Dialog", Font.PLAIN, 12);
	static Font editor = new Font ("Dialog", Font.PLAIN, 7);
	static boolean pauseAnimation;
	static int pauseCounter;
	
	static int closestLadderX;
	static int lastLadderX;
	static boolean validLadder;
	
	AudioClip getCashMoney;
	AudioClip die;
	AudioClip shoot;
	AudioClip track;
	AudioClip respawn;
	AudioClip lobby;
	AudioClip walk;
	
	
	Graphics dbg;
	Image dbImage;
	player player1;
	player[] enemy;
	tile [][] tileArray;
	
	public void init () {
		if (startup) {
			int width;
			int height;
		
			width = 750;
			height = 450;
			tileSize = 15;
			counter = 0;
			enemyMoveCounter = 0;
			door = false;
			pause = true;
			pauseAnimation = true;
			pauseCounter = 0;
			
			level = new boolean [4];
			level[0] = true;
			level[1] = false;
			level[2] = false;
			level[3] = false;
			
			closestLadderX = 1000;
			lastLadderX = 1000;
			validLadder = true;
			
			getCashMoney = getAudioClip(getDocumentBase(), "Coin_Collection.wav");
			die = getAudioClip(getDocumentBase(), "Death.wav");
			shoot = getAudioClip(getDocumentBase(), "Gun.wav");
			track = getAudioClip(getDocumentBase(), "Level_Theme.wav");
			respawn = getAudioClip(getDocumentBase(), "Regeneration.wav");
			lobby = getAudioClip(getDocumentBase(), "Menu_Theme.wav");
			walk = getAudioClip(getDocumentBase(), "Walking.wav");
		
			setSize (width, height);
			startup = false;
			
			lobby.loop();
		}
		// divide map into tiles (like grid)
		tileArray = new tile [30][50];
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				tileArray [yTile][xTile] = new tile (xTile*tileSize, yTile*tileSize, 0);
			}
		}
		
		player1 = new player (0, 0);/*WHEN EDITTING...NEED TO INITIALIZE PLAYER...*/
		
		// TEST LEVEL 0 -------------------------------------------------------------------------------------------------------------------------
		if (level[0]) {
			player1 = new player (30, 45);
			
			enemy = new player [5];
			enemy [0] = new player (690, 165);
			enemy [1] = new player (100, 405);
			enemy [2] = new player (300, 405);
			enemy [3] = new player (565, 270);
			enemy [4] = new player (565, 390);
			for (int i = 0; i < enemy.length; i++) {
				enemy[i].direction[0] = true;
			}

/* -----------------------------TILE GENERATOR----------------------------- 
	    **CREATES A BODY OF TILES USING "tileGenerator" METHOD @ LINE 162**

    REQUIRES 5 INPUTS: 1. FIRST X TILE (CLOSEST TO THE LEFT)
		       2. FIRST Y TILE (CLOSEST TO THE TOP)
		       3. FINAL X TILE 
		       4. FINAL Y TILE
		       5. TYPE OF TILE (0 = air, 1 = ground, 2 = bars, 3 = ladders, 4 = destroyed ground, 5 = gold)
		       6. ALLOWED TO FALL THROUGH? (TRUE/FALSE)

*/
			// define Ground tiles (left)
			tileGenerator(0, 28, 23, 29, 1, false); // base platform Left
			tileGenerator(0, 3, 0, 27, 1, false); // left wall
			tileGenerator(0, 4, 12, 4, 1, true); // skinny platform (top)
			tileGenerator(3, 11, 12, 15, 1, false); // fat platform 
			tileGenerator(13, 4, 20, 15, 1, false); // tall platform
			tileGenerator(15, 23, 21, 23, 1, true); // skinny platform (bot)
			tileGenerator(15, 19, 21, 19, 1, true); // skinny platform (2nd bot)
			tileGenerator(24, 27, 49, 29, 1, false); // base platform right
			tileGenerator(49, 11, 49, 26, 1, false); // right wall
			tileGenerator(21, 5, 31, 5, 1, true); // skinny platform middle (top)
			tileGenerator(21, 11, 31, 11, 1, true); // skinny platform middle (middle)
			tileGenerator(30, 19, 44, 19, 1, true); // skinny platform far right
			tileGenerator(30, 23, 44, 24, 1, false); // 2x2 far right
			tileGenerator(44, 12, 48, 13, 1, false); // far right platform (middle)
		
			// define Bar tiles (left) (ordered left to right)
			tileGenerator(1, 17, 12, 17, 2, false);
			tileGenerator(32, 4, 35, 4, 2, false);
			tileGenerator(35, 6, 38, 6, 2, false);
			tileGenerator(38, 8, 41, 8, 2, false);
			tileGenerator(41, 10, 44, 10, 2, false);
			tileGenerator(31, 14, 43, 14, 2, false);
		
			// define Ladder tiles (left) (ordered left to right)
			tileGenerator(4, 11, 4, 27, 3, false);
			tileGenerator(11, 4, 11, 10, 3, false);
			tileGenerator(15, 23, 15, 27, 3, false);
			tileGenerator(17, 4, 17, 18, 3, false);
			tileGenerator(20, 19, 20, 22, 3, false);
			tileGenerator(23, 5, 23, 10, 3, false);
			tileGenerator(29, 5, 29, 10, 3, false);
			tileGenerator(30, 15, 30, 26, 3, false);
			tileGenerator(35, 19, 35, 22, 3, false);
			tileGenerator(40, 19, 40, 22, 3, false);
			tileGenerator(44, 23, 44, 26, 3, false);
		
			//stray tiles
			tileArray[4][26].type = 1;
			tileArray[10][31].type = 1;
			tileArray[26][24].type = 1;     
			
			// define indestructible
			tileArray[4][10].indestructible = true;
			tileArray[4][12].indestructible = true;
			tileArray[4][16].indestructible = true;
			tileArray[4][18].indestructible = true;
			tileArray[5][22].indestructible = true;
			tileArray[5][24].indestructible = true;
			tileArray[5][28].indestructible = true;
			tileArray[5][30].indestructible = true;
			tileArray[11][3].indestructible = true;
			tileArray[11][5].indestructible = true;
			tileArray[11][10].indestructible = true;
			tileArray[11][11].indestructible = true;
			tileArray[11][12].indestructible = true;
			tileArray[11][22].indestructible = true;
			tileArray[11][23].indestructible = true;
			tileArray[11][24].indestructible = true;
			tileArray[11][28].indestructible = true;
			tileArray[11][29].indestructible = true;
			tileArray[11][30].indestructible = true;
			tileArray[19][15].indestructible = true;
			tileArray[19][16].indestructible = true;
			tileArray[19][17].indestructible = true;
			tileArray[19][18].indestructible = true;
			tileArray[19][19].indestructible = true;
			tileArray[19][20].indestructible = true;
			tileArray[19][21].indestructible = true;
			tileArray[19][34].indestructible = true;
			tileArray[19][36].indestructible = true;
			tileArray[19][39].indestructible = true;
			tileArray[19][41].indestructible = true;
			tileArray[23][16].indestructible = true;
			tileArray[23][17].indestructible = true;
			tileArray[23][18].indestructible = true;
			tileArray[23][19].indestructible = true;
			tileArray[23][20].indestructible = true;
			tileArray[23][21].indestructible = true;
			tileArray[23][34].indestructible = true;
			tileArray[23][35].indestructible = true;
			tileArray[23][36].indestructible = true;
			tileArray[23][39].indestructible = true;
			tileArray[23][40].indestructible = true;
			tileArray[23][41].indestructible = true;
			tileArray[23][43].indestructible = true;
			tileArray[27][29].indestructible = true;
			tileArray[27][30].indestructible = true;
			tileArray[27][31].indestructible = true;
			tileArray[27][43].indestructible = true;
			tileArray[27][44].indestructible = true;
			tileArray[27][45].indestructible = true;
			tileArray[28][3].indestructible = true;
			tileArray[28][4].indestructible = true;
			tileArray[28][5].indestructible = true;
			tileArray[28][14].indestructible = true;
			tileArray[28][15].indestructible = true;
			tileArray[28][16].indestructible = true;
			

		
			// define Gold tiles (ordered to collect)
			tileArray[3][10].gold = 2;
			tileArray[13][2].gold = 1;
			tileArray[19][13].gold = 1;
			tileArray[4][35].gold = 1;
			tileArray[6][38].gold = 1;
			tileArray[8][41].gold = 1;
			tileArray[10][44].gold = 1;
			tileArray[11][48].gold = 1;
			tileArray[17][34].gold = 2;             
			tileArray[17][41].gold = 2;
			tileArray[26][43].gold = 3;
		}
		
		
		// LEVEL 1 -------------------------------------------------------------------------------------------------------------------------
		if (level[1]) {
			player1.shots = 0;
			player1.xpos = 700;
			player1.ypos = 390;
			
			enemy = new player [5];
			enemy [0]= new player (585, 30);
			enemy [1]= new player (405, 150);
			enemy [2]= new player (285, 315);
			enemy [3] = new player (220, 150);
			enemy [4] = new player (345, 315);
			for (int i = 0; i < enemy.length; i++) {
				enemy[i].direction[0] = true;
			}
			
			// define Ground
			tileGenerator(0, 27, 49, 29, 1, false); // Base
			tileGenerator(4, 24, 38, 24, 1, true); // skinny above base
			tileGenerator(0, 0, 0, 27, 1, false); // Left wall
			tileGenerator(49, 2, 49, 27, 1, false); // Right wall
			tileGenerator(39, 4, 39, 26, 1, false); // tallest
			tileGenerator(41, 4, 41, 25, 1, false); // 2nd tallest (right)
			tileGenerator(38, 3, 41, 3, 1, true); // skinny top (right)
			tileGenerator(42, 8, 48, 8, 1, true); // skinny far right (higher)
			tileGenerator(42, 17, 48, 17, 1, true); // skinny far right (lower)
			tileGenerator(7, 8, 35, 8, 1, true); // 1st lateral
			tileGenerator(7, 11, 35, 11, 1, true); // 2nd lateral
			tileGenerator(4, 14, 38, 14, 1, true); // middle lateral of cross
			tileGenerator(4, 16, 38, 16, 1, true); // laterals....
			tileGenerator(4, 18, 38, 18, 1, true);
			tileGenerator(4, 20, 38, 20, 1, true);
			tileGenerator(4, 22, 38, 22, 1, true); // ....
			tileGenerator(20, 5, 22, 23, 1, false); // vertical of cross
			tileGenerator(1, 12, 3, 25, 1, false); // far left (door platform)      
			
			// define Bars
			// 1st structure (right)
			tileGenerator(42, 2, 48, 2, 2, false);
			tileGenerator(44, 11, 48, 11, 2, false);
			tileGenerator(42, 14, 46, 14, 2, false);
			tileGenerator(44, 20, 48, 20, 2, false);
			tileGenerator(42, 23, 46, 23, 2, false);
			// cross structure
			tileGenerator(7, 2, 37, 2, 2, false); // toppp
			tileGenerator(7, 4, 19, 4, 2, false); // top left
			tileGenerator(23, 4, 35, 4, 2, false); // top right
			tileGenerator(20, 7, 22, 7, 2, false); // 1st x3
			tileGenerator(20, 10, 22, 10, 2, false); // 2nd x3
			
			// define Ladders
			tileGenerator(40, 3, 40, 26, 3, false); // tallest
			tileGenerator(1, 12, 1, 26, 3, false); // ladder to door (left)
			tileGenerator(7, 5, 7, 13, 3, false); // right in cross top
			tileGenerator(19, 5, 19, 13, 3, false); // 2nd in cross top
			tileGenerator(23, 5, 23, 13, 3, false); // 3rd in cross top
			tileGenerator(35, 5, 35, 13, 3, false); // 4th in cross top
			tileGenerator(38, 14, 38, 23, 3, false); // 1st in cross bot
			tileGenerator(4, 14, 4, 23, 3, false); // 2nd in cross bot
			
			// define stray

			// define Air
			tileGenerator(20, 15, 22, 15, 0, false); 
			tileGenerator(20, 19, 22, 19, 0, false); 
			tileGenerator(20, 23, 22, 23, 0, false); 
			
			// define indestructible
			tileArray[3][38].indestructible = true;
			tileArray[3][39].indestructible = true;
			tileArray[3][41].indestructible = true;
			tileArray[12][2].indestructible = true;
			tileArray[12][3].indestructible = true;
			tileArray[14][5].indestructible = true;
			tileArray[14][6].indestructible = true;
			tileArray[14][7].indestructible = true;
			tileArray[14][8].indestructible = true;
			tileArray[14][18].indestructible = true;
			tileArray[14][19].indestructible = true;
			tileArray[14][23].indestructible = true;
			tileArray[14][24].indestructible = true;
			tileArray[14][34].indestructible = true;
			tileArray[14][35].indestructible = true;
			tileArray[14][36].indestructible = true;
			tileArray[14][37].indestructible = true;
			tileArray[24][4].indestructible = true;
			tileArray[24][5].indestructible = true;
			tileArray[24][37].indestructible = true;
			tileArray[24][38].indestructible = true;
			tileArray[27][2].indestructible = true;
			tileArray[27][3].indestructible = true;
			tileArray[27][40].indestructible = true;
			tileArray[27][41].indestructible = true;
			
			// define Gold
			tileArray[10][46].gold = 1;
			tileArray[13][44].gold = 1;
			tileArray[19][46].gold = 1;
			tileArray[22][44].gold = 1;
			tileArray[4][21].gold = 2;
			tileArray[7][21].gold = 2;
			tileArray[10][21].gold = 2;
			tileArray[17][13].gold = 3;
			tileArray[17][29].gold = 3;
			tileArray[21][13].gold = 3;
			tileArray[21][29].gold = 3;
		}
		
		// LEVEL 2 -------------------------------------------------------------------------------------
		if (level[2]) {
			player1.shots = 0;
			player1.xpos = 30;
			player1.ypos = 165;
			
			enemy = new player [5];
			enemy [0]= new player (45, 405);
			enemy [1]= new player (270, 240);
			enemy [2]= new player (525, 150);
			enemy [3] = new player (315, 285);
			enemy [4] = new player (450, 240);
			for (int i = 0; i < enemy.length; i++) {
				enemy[i].direction[0] = true;
			}
			
			// define Ground
			tileGenerator(0, 28, 49, 29, 1, false); // base
			tileGenerator(49, 2, 49, 27, 1, false); // right wall
			tileGenerator(45, 3, 47, 25, 1, false); // right wall fat
			
			tileGenerator(0, 12, 2, 20, 1, false); // left 1st box
			tileGenerator(12, 12, 13, 20, 1, false); // right 1st box
			tileGenerator(9, 9, 11, 12, 1, false); // left 2nd box
			tileGenerator(21, 9, 22, 17, 1, false); // right 2nd box
			tileGenerator(18, 6, 20, 9, 1, false); // left 3rd box
			tileGenerator(30, 6, 31, 14, 1, false); // right 3rd box
			tileGenerator(27, 3, 29, 6, 1, false); // left 4th box
			tileGenerator(39, 3, 40, 11, 1, false); // right 4th box
			tileGenerator(3, 12, 8, 12, 1, true); // top 1st box
			tileGenerator(3, 20, 11, 20, 1, true); // bot 1st box
			tileGenerator(12, 9, 17, 9, 1, true); // top 2nd box
			tileGenerator(14, 17, 20, 17, 1, true); // bot 2nd box
			tileGenerator(21, 6, 26, 6, 1, true); // top 3rd box
			tileGenerator(23, 14, 29, 14, 1, true); // bot 3rd box
			tileGenerator(30, 3, 38, 3, 1, true); // top 4th box
			tileGenerator(32, 11, 38, 11, 1, true); // bot 4th box
			tileGenerator(18, 20, 24, 20, 1, true); // 5th box top
			tileGenerator(27, 20, 29, 23, 1, true); // 5th box right
			tileGenerator(16, 20, 17, 23, 1, false); // 5th box left
			tileGenerator(25, 17, 26, 20, 1, false); // 6th box left
			tileGenerator(36, 17, 38, 23, 1, true); // 6th box right
			tileGenerator(27, 17, 33, 17, 1, true); // 6th box top
			tileGenerator(36, 14, 44, 14, 1, true); // 7th box top
			tileGenerator(34, 14, 35, 17, 1, false); // 7th box left
			
			tileGenerator(1, 23, 3, 24, 1, false); // above door
			tileGenerator(0, 11, 0, 27, 1, false); // left wall
			tileGenerator(4, 23, 44, 25, 1, false); // 2nd bottom
			tileGenerator(4, 26, 25, 26, 1, false); // sliver
			
			
			// define Bars
			tileGenerator(14, 11, 18, 11, 2, false); // sq2
			tileGenerator(23, 8, 27, 8, 2, false); // sq3
			tileGenerator(32, 5, 36, 5, 2, false); // sq4
			tileGenerator(30, 19, 33, 19, 2, false); // sq5
			tileGenerator(39, 16, 42, 16, 2, false); // sq5
			tileGenerator(41, 2, 44, 2, 2, false); // outside top right
			
			// define Ladders
			tileGenerator(8, 9, 8, 11, 3, false); // top layer
			tileGenerator(17, 6, 17, 8, 3, false); // top layer
			tileGenerator(26, 3, 26, 5, 3, false); // top layer
			tileGenerator(44, 3, 44, 13, 3, false); // right
			tileGenerator(48, 3, 48, 27, 3, false); // rightest
			tileGenerator(16, 20, 16, 22, 3, false); // 2nd layer
			tileGenerator(25, 17, 25, 19, 3, false); // 2nd layer
			tileGenerator(34, 14, 34, 16, 3, false); // 2nd layer
			tileGenerator(3, 12, 3, 19, 3, false); // sq1
			tileGenerator(12, 9, 12, 11, 3, false); // sq2 short
			tileGenerator(14, 12, 14, 16, 3, false); // sq2 long
			tileGenerator(21, 6, 21, 8, 3, false); // sq3 short
			tileGenerator(23, 9, 23, 13, 3, false); // sq3 long
			tileGenerator(30, 3, 30, 5, 3, false); // sq4 short
			tileGenerator(32, 6, 32, 10, 3, false); // sq4 long
			tileGenerator(18, 20, 18, 22, 3, false); // sq5
			tileGenerator(27, 17, 27, 19, 3, false); // sq6 short
			tileGenerator(30, 20, 30, 22, 3, false); // sq6 long
			tileGenerator(36, 14, 36, 16, 3, false); // sq7 short
			tileGenerator(39, 17, 39, 22, 3, false); // sq7 long
			
			// define indestructible
			tileArray[12][2].indestructible = true;
			tileArray[12][4].indestructible = true;
			tileArray[12][7].indestructible = true;
			tileArray[12][8].indestructible = true;
			tileArray[12][12].indestructible = true;
			tileArray[12][13].indestructible = true;
			tileArray[9][9].indestructible = true;
			tileArray[9][11].indestructible = true;
			tileArray[9][13].indestructible = true;
			tileArray[9][16].indestructible = true;
			tileArray[9][17].indestructible = true;
			tileArray[9][21].indestructible = true;
			tileArray[9][22].indestructible = true;
			tileArray[6][18].indestructible = true;
			tileArray[6][20].indestructible = true;
			tileArray[6][22].indestructible = true;
			tileArray[6][25].indestructible = true;
			tileArray[6][26].indestructible = true;
			tileArray[6][30].indestructible = true;
			tileArray[6][31].indestructible = true;
			tileArray[3][27].indestructible = true;
			tileArray[3][29].indestructible = true;
			tileArray[3][31].indestructible = true;
			tileArray[3][45].indestructible = true;
			tileArray[3][47].indestructible = true;
			tileArray[11][32].indestructible = true;
			tileArray[11][33].indestructible = true;
			tileArray[14][23].indestructible = true;
			tileArray[14][24].indestructible = true;
			tileArray[14][35].indestructible = true;
			tileArray[14][37].indestructible = true;
			tileArray[14][43].indestructible = true;
			tileArray[14][44].indestructible = true;
			tileArray[17][14].indestructible = true;
			tileArray[17][15].indestructible = true;
			tileArray[17][26].indestructible = true;
			tileArray[17][28].indestructible = true;
			tileArray[17][33].indestructible = true;
			tileArray[17][34].indestructible = true;
			tileArray[17][37].indestructible = true;
			tileArray[17][38].indestructible = true;
			tileArray[20][3].indestructible = true;
			tileArray[20][4].indestructible = true;
			tileArray[20][17].indestructible = true;
			tileArray[20][19].indestructible = true;
			tileArray[20][28].indestructible = true;
			tileArray[20][29].indestructible = true;
			tileArray[23][15].indestructible = true;
			tileArray[23][16].indestructible = true;
			tileArray[23][18].indestructible = true;
			tileArray[23][19].indestructible = true;
			tileArray[23][30].indestructible = true;
			tileArray[23][31].indestructible = true;
			tileArray[23][39].indestructible = true;
			tileArray[23][40].indestructible = true;
			
			// define Gold
			tileArray[19][7].gold = 1;
			tileArray[19][9].gold = 1;
			tileArray[19][11].gold = 1;
			tileArray[11][19].gold = 1;
			tileArray[13][19].gold = 1;
			tileArray[15][19].gold = 2;
			tileArray[8][28].gold = 1;
			tileArray[10][28].gold = 1;
			tileArray[12][28].gold = 2;
			tileArray[5][37].gold = 1;
			tileArray[7][37].gold = 1;
			tileArray[9][37].gold = 2;
			tileArray[22][22].gold = 1;
			tileArray[22][24].gold = 1;
			tileArray[22][26].gold = 1;
			tileArray[19][34].gold = 1;
			tileArray[21][34].gold = 2;
			tileArray[16][43].gold = 1;
			tileArray[18][43].gold = 1;
			tileArray[20][43].gold = 2;
			tileArray[27][4].gold = 1;
			tileArray[22][2].gold = 3;
			tileArray[11][42].gold = 3;
		}
		
		addKeyListener(this);
	}
	
	
	
	public void start () {
		Thread th = new Thread(this);
	th.start();
	}
	
	
	
	
	
	// RUN *----------------------------------------------------------------
	public void run () {
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		
		while (true) {
		if (!level[3]) {
			if (!pause) {
				playerAdjust (player1);
				player1.interact[1] = interactGround(player1);
				player1.interact[5] = interactRightWall(player1);
				player1.interact[6] = interactLeftWall(player1);
				player1.interact[2] = interactBars(player1);
				player1.interact[3] = interactLadder(player1);
				player1.interact[7] = interactLadder2(player1);
				player1.interact[0] = interactAir(player1);
				respawn(player1);
				collectGold(player1);
				playerMove(player1);
			
			
				for (int i = 0; i < enemy.length; i++) {
					enemyAI(enemy[i]);
					playerAdjust (enemy[i]);
					enemy[i].interact[1] = interactGround(enemy[i]);
					enemy[i].interact[5] = interactRightWall(enemy[i]);
					enemy[i].interact[6] = interactLeftWall(enemy[i]);
					enemy[i].interact[2] = interactBars(enemy[i]);
					enemy[i].interact[3] = interactLadder(enemy[i]);
					enemy[i].interact[7] = interactLadder2(enemy[i]);
					enemy[i].interact[0] = interactAir(enemy[i]);
					checkKill(enemy[i]);
						if (!enemy[i].gold) {
							enemyCollectGold(enemy[i]);
						}
					dropGold(enemy[i]);
					playerMove(enemy[i]);
				}
			
				if (player1.shots < 20) {
					if (player1.shoot[0]) {
						shootRight(player1);
					}
					if (player1.shoot[1]) {
						shootLeft(player1);
					}
				}
			
				if (!door) {
					door = checkDoor();
				}
			
				checkWin(player1);
			}
		}
			repaint();
			
			
			try {
				Thread.sleep(15);
			}
			catch (Exception e) {}
			

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);        
			
		}
		
		
	}
	
	
	
	
	
	
	// PAINT ------------------------------------------------------------
	public void paint (Graphics g) {
		Color random = new Color (0,0,0);
		Color random2 = new Color (0,0,0);
		g.setFont(original);
		g.setColor(Color.black);
		g.fillRect(0,0,750,450);
		
		// draw Ground
		g.setColor(Color.white);
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[yTile][xTile].type == 1) {
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].xpos+tileSize-4, tileArray[yTile][xTile].ypos+7);
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+tileSize);
				}
			}
		}
		
		// draw indestructible Ground
		g.setColor(Color.gray); 
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[yTile][xTile].indestructible) {
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].xpos+tileSize-4, tileArray[yTile][xTile].ypos+7);
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+tileSize);
				}
			}
		}
		
		
		
		if (level[0]) {
			g.setColor(Color.green);
		}
		if (level[1]) {
			g.setColor(Color.pink);
		}
		if (level[2]) {
			g.setColor(Color.cyan);
		}
		// draw Bars
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[yTile][xTile].type == 2) {
					g.drawLine(tileArray[yTile][xTile].xpos, tileArray[yTile][xTile].ypos, tileArray[yTile][xTile].xpos+tileSize, tileArray[yTile][xTile].ypos);
					g.drawLine(tileArray[yTile][xTile].xpos, tileArray[yTile][xTile].ypos, tileArray[yTile][xTile].xpos+tileSize, tileArray[yTile][xTile].ypos+2);
				}
			}
		}
		
		// draw Ladders
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[yTile][xTile].type == 3) {
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].xpos+tileSize-4, tileArray[yTile][xTile].ypos+7);
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+10);
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+14, tileArray[yTile][xTile].xpos+tileSize-4, tileArray[yTile][xTile].ypos+14);
					g.drawLine (tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+14, tileArray[yTile][xTile].xpos+4, tileArray[yTile][xTile].ypos+17);
				}
			}
		}
		
		// draw Gold
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[yTile][xTile].gold > 0) {
					g.setColor(Color.yellow);
					if (tileArray[yTile][xTile].gold == 1) {
						g.fillOval(tileArray[yTile][xTile].xpos+3, tileArray[yTile][xTile].ypos+3, tileSize-6, tileSize-6);
					}
					if (tileArray[yTile][xTile].gold == 2) {
						int gold2X[] = {tileArray[yTile][xTile].xpos+5, tileArray[yTile][xTile].xpos+8, tileArray[yTile][xTile].xpos+11, tileArray[yTile][xTile].xpos+8};
						int gold2Y[] = {tileArray[yTile][xTile].ypos+10, tileArray[yTile][xTile].ypos, tileArray[yTile][xTile].ypos+10, tileArray[yTile][xTile].ypos+15};
						g.fillPolygon(gold2X, gold2Y, 4);
					}
					if (tileArray[yTile][xTile].gold == 3) {
						int gold3X[] = {tileArray[yTile][xTile].xpos, tileArray[yTile][xTile].xpos+5, tileArray[yTile][xTile].xpos+7, tileArray[yTile][xTile].xpos+9, tileArray[yTile][xTile].xpos+14, tileArray[yTile][xTile].xpos+10, tileArray[yTile][xTile].xpos+12, tileArray[yTile][xTile].xpos+7, tileArray[yTile][xTile].xpos+3, tileArray[yTile][xTile].xpos+4};
						int gold3Y[] = {tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+2, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+9, tileArray[yTile][xTile].ypos+14, tileArray[yTile][xTile].ypos+11, tileArray[yTile][xTile].ypos+14, tileArray[yTile][xTile].ypos+9};
						g.fillPolygon(gold3X, gold3Y, 10);
					}
					
					g.setColor(Color.red);
					if (tileArray[yTile][xTile].gold == 1) {
						g.drawOval(tileArray[yTile][xTile].xpos+3, tileArray[yTile][xTile].ypos+3, tileSize-6, tileSize-6);
					}
					if (tileArray[yTile][xTile].gold == 2) {
						int gold2X[] = {tileArray[yTile][xTile].xpos+5, tileArray[yTile][xTile].xpos+8, tileArray[yTile][xTile].xpos+11, tileArray[yTile][xTile].xpos+8};
						int gold2Y[] = {tileArray[yTile][xTile].ypos+10, tileArray[yTile][xTile].ypos, tileArray[yTile][xTile].ypos+10, tileArray[yTile][xTile].ypos+15};
						g.drawPolygon(gold2X, gold2Y, 4);
					}
					if (tileArray[yTile][xTile].gold == 3) {
						int gold3X[] = {tileArray[yTile][xTile].xpos, tileArray[yTile][xTile].xpos+5, tileArray[yTile][xTile].xpos+7, tileArray[yTile][xTile].xpos+9, tileArray[yTile][xTile].xpos+14, tileArray[yTile][xTile].xpos+10, tileArray[yTile][xTile].xpos+12, tileArray[yTile][xTile].xpos+7, tileArray[yTile][xTile].xpos+3, tileArray[yTile][xTile].xpos+4};
						int gold3Y[] = {tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+2, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+7, tileArray[yTile][xTile].ypos+9, tileArray[yTile][xTile].ypos+14, tileArray[yTile][xTile].ypos+11, tileArray[yTile][xTile].ypos+14, tileArray[yTile][xTile].ypos+9};
						g.drawPolygon(gold3X, gold3Y, 10);
					}
				}
			}
		}
		
		// draw Door
		if ((door && level[0]) || level[1]) {
			g.setColor(Color.white);
			g.drawRect(690, 378, 10, 30);
			g.drawRect(720, 378, 10, 30);
			g.drawRect(690, 368, 40, 10);
			if ((!door && level[1]) || (door && level[0])) {
				if (counter < 230) {
					random = new Color (counter, counter, counter);
					counter ++;
				}
				else if (counter > 0) {
					random = new Color (counter, 0, counter);
					counter --;
				}
				g.setColor(random);
			}
			else if (door && level[1]) {
				g.setColor(Color.darkGray);
			}
			g.fillRect(700, 378, 20, 30);
		}
		if ((door && level[1]) || level[2]) {
			g.setColor(Color.white);
			g.drawRect(15, 150, 10, 30);
			g.drawRect(45, 150, 10, 30);
			g.drawRect(15, 140, 40, 10);
			if ((!door && level[2]) || (door && level[1])) {
				if (counter < 230) {
					random2 = new Color (counter, counter, counter);
					counter ++;
				}
				else if (counter > 0) {
					random2 = new Color (0, counter, counter);
					counter --;
				}
				g.setColor(random2);
			}
			else if (door && level[2]) {
				g.setColor(Color.darkGray);
			}
			g.fillRect(25, 150, 20, 30);
		}
		if ((door && level[2]) || level[3]) {
			g.setColor(Color.white);
			g.drawRect(15, 390, 10, 30);
			g.drawRect(45, 390, 10, 30);
			g.drawRect(15, 380, 40, 10);
			if ((!door && level[3]) || (door && level[2])) {
				if (counter < 230) {
					random = new Color (counter, counter, counter);
					counter ++;
				}
				else if (counter > 0) {
					random = new Color (counter, counter, 0);
					counter --;
				}
				g.setColor(random);
			}
			else if (door && level[3]) {
				g.setColor(Color.darkGray);
			}
			g.fillRect(25, 390, 20, 30);
		}

		// enemy
		for (int i = 0; i < enemy.length; i++) {
			g.setColor(Color.red);
			if (enemy[i].deadCounter > 0) {
				g.drawLine (enemy[i].xpos+4, enemy[i].ypos+7, enemy[i].xpos+tileSize-4, enemy[i].ypos+7);
				g.drawLine (enemy[i].xpos+4, enemy[i].ypos+7, enemy[i].xpos+4, enemy[i].ypos+tileSize);
			}
			// FALLING
			else if (enemy[i].falling) {
				g.fillRect (enemy[i].xpos+4, enemy[i].ypos, tileSize-8, tileSize-10); // head
				g.fillRect (enemy[i].xpos+6, enemy[i].ypos+5, 3, 10); // body
				if (enemy[i].fallAnimation <= 10) {
					// arms 1
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos+8);
					g.drawLine (enemy[i].xpos, enemy[i].ypos+3, enemy[i].xpos+2, enemy[i].ypos+8); // left
					g.drawLine (enemy[i].xpos+12, enemy[i].ypos+8, enemy[i].xpos+14, enemy[i].ypos+3); // right
					// legs 1
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+19, enemy[i].xpos+4, enemy[i].ypos+16); // left
					g.drawLine (enemy[i].xpos+4, enemy[i].ypos+16, enemy[i].xpos+6, enemy[i].ypos+15); // left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+15, enemy[i].xpos+11, enemy[i].ypos+16); // right
					g.drawLine (enemy[i].xpos+11, enemy[i].ypos+16, enemy[i].xpos+13, enemy[i].ypos+19); // right
					
					enemy[i].fallAnimation++;
				}
				else if (enemy[i].fallAnimation <= 20) {
					// arms 2
					g.setColor(Color.red);
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos+8);
					g.drawLine (enemy[i].xpos, enemy[i].ypos+13, enemy[i].xpos+2, enemy[i].ypos+8); // left
					g.drawLine (enemy[i].xpos+12, enemy[i].ypos+8, enemy[i].xpos+14, enemy[i].ypos+13); // right
					// legs 2
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					g.drawLine (enemy[i].xpos+5, enemy[i].ypos+19, enemy[i].xpos+4, enemy[i].ypos+16); // left
					g.drawLine (enemy[i].xpos+4, enemy[i].ypos+16, enemy[i].xpos+6, enemy[i].ypos+15); // left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+15, enemy[i].xpos+11, enemy[i].ypos+16); // right
					g.drawLine (enemy[i].xpos+11, enemy[i].ypos+16, enemy[i].xpos+10, enemy[i].ypos+19); // right
					
					enemy[i].fallAnimation++;
					if (enemy[i].fallAnimation == 20) {
						enemy[i].fallAnimation = 1;
					}
				}
			}
			// BAR RIGHT
			else if (enemy[i].lastDirection == 0 && enemy[i].interact[2]) {
				g.fillRect (enemy[i].xpos+4, enemy[i].ypos, tileSize-8, tileSize-10); // head
				g.fillRect (enemy[i].xpos+6, enemy[i].ypos+5, 2, 10); // body
				if (enemy[i].barAnimation <= 25) {
					// arms 1
					g.setColor(Color.red);
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+1, enemy[i].xpos+2, enemy[i].ypos+7); // left
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+7, enemy[i].xpos+6, enemy[i].ypos+9); // left
					g.drawLine (enemy[i].xpos+8, enemy[i].ypos+9, enemy[i].xpos+13, enemy[i].ypos+8); // right
					g.drawLine (enemy[i].xpos+13, enemy[i].ypos+8, enemy[i].xpos+15, enemy[i].ypos); // right
					enemy[i].barAnimation++;
				}
				else if (enemy[i].barAnimation <= 40) {
					// arms 2 
					g.drawLine (enemy[i].xpos+3, enemy[i].ypos+14, enemy[i].xpos+4, enemy[i].ypos+9); // left                               
					g.drawLine (enemy[i].xpos+4, enemy[i].ypos+9, enemy[i].xpos+6, enemy[i].ypos+9); // left
					g.drawLine (enemy[i].xpos+8, enemy[i].ypos+9, enemy[i].xpos+11, enemy[i].ypos+8); // right
					g.drawLine (enemy[i].xpos+11, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos); //right
					enemy[i].barAnimation++;
					if (enemy[i].barAnimation == 40) {
						enemy[i].barAnimation = 1;
					}
				}
				if (enemy[i].gold) {
					g.setColor(Color.yellow);
				}
				g.drawLine (enemy[i].xpos+4, enemy[i].ypos+20, enemy[i].xpos+6, enemy[i].ypos+17); // left
				g.drawLine (enemy[i].xpos+6, enemy[i].ypos+17, enemy[i].xpos+6, enemy[i].ypos+15); // left
				g.drawLine (enemy[i].xpos+7, enemy[i].ypos+15, enemy[i].xpos+7, enemy[i].ypos+20); // right
			}
			// BAR LEFT
			else if (enemy[i].lastDirection == 1 && enemy[i].interact[2]) {
				g.fillRect (enemy[i].xpos+4, enemy[i].ypos, tileSize-8, tileSize-10); // head
				g.fillRect (enemy[i].xpos+7, enemy[i].ypos+5, 2, 10); // body
				if (enemy[i].barAnimation <= 25) {
					g.setColor(Color.red);
					// arms 1 
					g.drawLine (enemy[i].xpos-1, enemy[i].ypos, enemy[i].xpos+2, enemy[i].ypos+8); // left
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+8, enemy[i].xpos+6, enemy[i].ypos+9); // left
					g.drawLine (enemy[i].xpos+8, enemy[i].ypos+9, enemy[i].xpos+12, enemy[i].ypos+7); // right
					g.drawLine (enemy[i].xpos+12, enemy[i].ypos+7, enemy[i].xpos+12, enemy[i].ypos+1); // right
					enemy[i].barAnimation++;
				}
				else if (enemy[i].barAnimation <= 40) {
					// arms 2 
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos, enemy[i].xpos+3, enemy[i].ypos+8); // left
					g.drawLine (enemy[i].xpos+3, enemy[i].ypos+8, enemy[i].xpos+6, enemy[i].ypos+9); // left
					g.drawLine (enemy[i].xpos+8, enemy[i].ypos+9, enemy[i].xpos+11, enemy[i].ypos+9); // right
					g.drawLine (enemy[i].xpos+11, enemy[i].ypos+9, enemy[i].xpos+12, enemy[i].ypos+14); // right
					enemy[i].barAnimation++;
					if (enemy[i].barAnimation == 40) {
						enemy[i].barAnimation = 1;
					}
				}
				if (enemy[i].gold) {
					g.setColor(Color.yellow);
				}
				g.drawLine (enemy[i].xpos+7, enemy[i].ypos+15, enemy[i].xpos+7, enemy[i].ypos+19); // left
				g.drawLine (enemy[i].xpos+8, enemy[i].ypos+15, enemy[i].xpos+8, enemy[i].ypos+17); // right
				g.drawLine (enemy[i].xpos+8, enemy[i].ypos+17, enemy[i].xpos+10, enemy[i].ypos+20); // right
			}
			// WALK RIGHT
			else if (enemy[i].lastDirection == 0) {
				g.setColor(Color.red);
				g.fillRect (enemy[i].xpos+4, enemy[i].ypos, tileSize-8, tileSize-10); // head
				g.fillRect (enemy[i].xpos+6, enemy[i].ypos+5, 2, 10); // body
				if (enemy[i].walkAnimation <= 25) {
					// arms 1 
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos+8);
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+8, enemy[i].xpos+2, enemy[i].ypos+11); // left
					g.drawLine (enemy[i].xpos+12, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos+5); // right
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					// legs 1
					g.drawLine(enemy[i].xpos+5, enemy[i].ypos+18, enemy[i].xpos+6, enemy[i].ypos+15); // left
					g.drawLine(enemy[i].xpos+8, enemy[i].ypos+15, enemy[i].xpos+9, enemy[i].ypos+18); // right
					enemy[i].walkAnimation++;
				}
				else if (enemy[i].walkAnimation <= 50) {
					g.setColor(Color.red);
					// arms 2 
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+10, enemy[i].xpos+4, enemy[i].ypos+12); // left                              
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+10, enemy[i].xpos+6, enemy[i].ypos+8); // left
					g.drawLine (enemy[i].xpos+8, enemy[i].ypos+8, enemy[i].xpos+10, enemy[i].ypos+10); // right
					g.drawLine (enemy[i].xpos+10, enemy[i].ypos+10, enemy[i].xpos+12, enemy[i].ypos+8); //right
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					// legs 2
					g.drawLine(enemy[i].xpos+3, enemy[i].ypos+18, enemy[i].xpos+6, enemy[i].ypos+15); // left
					g.drawLine(enemy[i].xpos+7, enemy[i].ypos+15, enemy[i].xpos+10, enemy[i].ypos+18); // right
					enemy[i].walkAnimation++;
					if (enemy[i].walkAnimation == 50) {
						enemy[i].walkAnimation = 1;
					}
				}
			}
			// WALK LEFT
			else if (enemy[i].lastDirection == 1) {
				g.setColor(Color.red);
				g.fillRect (enemy[i].xpos+4, enemy[i].ypos, tileSize-8, tileSize-10); // head
				g.fillRect (enemy[i].xpos+7, enemy[i].ypos+5, 2, 10); // body
				if (enemy[i].walkAnimation <= 25) {
					// arms 1 
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos+8);
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+5, enemy[i].xpos+2, enemy[i].ypos+8); // left
					g.drawLine (enemy[i].xpos+12, enemy[i].ypos+8, enemy[i].xpos+12, enemy[i].ypos+11); // right
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					// legs 1
					g.drawLine (enemy[i].xpos+6, enemy[i].ypos+18, enemy[i].xpos+7, enemy[i].ypos+15); // left
					g.drawLine (enemy[i].xpos+8, enemy[i].ypos+15, enemy[i].xpos+9, enemy[i].ypos+18); // right
					enemy[i].walkAnimation++;
				}
				else if (enemy[i].walkAnimation <= 50) {
					g.setColor(Color.red);
					// arms 2 
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos+8, enemy[i].xpos+4, enemy[i].ypos+11); // left
					g.drawLine (enemy[i].xpos+4, enemy[i].ypos+11, enemy[i].xpos+7, enemy[i].ypos+7); // left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+7, enemy[i].xpos+13, enemy[i].ypos+9); // right
					g.drawLine (enemy[i].xpos+13, enemy[i].ypos+9, enemy[i].xpos+10, enemy[i].ypos+13); // right
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					// legs 2
					g.drawLine(enemy[i].xpos+4, enemy[i].ypos+18, enemy[i].xpos+7, enemy[i].ypos+15); // left
					g.drawLine(enemy[i].xpos+8, enemy[i].ypos+15, enemy[i].xpos+11, enemy[i].ypos+18); // right
					enemy[i].walkAnimation++;
					if (enemy[i].walkAnimation == 50) {
						enemy[i].walkAnimation = 1;
					}
				}
			}
			// GO DOWN OR UP
			else if (enemy[i].lastDirection == 3 || enemy[i].lastDirection == 2) {
				g.setColor(Color.red);
				g.fillRect (enemy[i].xpos+4, enemy[i].ypos, tileSize-8, tileSize-10); // head
				g.fillRect (enemy[i].xpos+6, enemy[i].ypos+5, 3, 10); // body
				// arms 1
				if (enemy[i].ladderAnimation <= 20) {
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos, enemy[i].xpos+1, enemy[i].ypos+5); // left
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos+5, enemy[i].xpos+6, enemy[i].ypos+7); // left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+7, enemy[i].xpos+13, enemy[i].ypos+7); // right
					g.drawLine (enemy[i].xpos+13, enemy[i].ypos+7, enemy[i].xpos+13, enemy[i].ypos+3); // right
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					// legs 1
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+19, enemy[i].xpos+2, enemy[i].ypos+16); //left
					g.drawLine (enemy[i].xpos+2, enemy[i].ypos+16, enemy[i].xpos+6, enemy[i].ypos+15); //left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+14, enemy[i].xpos+13, enemy[i].ypos+13); //right
					g.drawLine (enemy[i].xpos+13, enemy[i].ypos+13, enemy[i].xpos+13, enemy[i].ypos+15); // right
					enemy[i].ladderAnimation ++;
				}
				else if (enemy[i].ladderAnimation <= 40) {
					g.setColor(Color.red);
					// arms 2
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos+3, enemy[i].xpos+1, enemy[i].ypos+7); // left
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos+7, enemy[i].xpos+6, enemy[i].ypos+7); // left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+7, enemy[i].xpos+14, enemy[i].ypos+5); // right
					g.drawLine (enemy[i].xpos+14, enemy[i].ypos+5, enemy[i].xpos+14, enemy[i].ypos); // right
					if (enemy[i].gold) {
						g.setColor(Color.yellow);
					}
					// legs 2
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos+16, enemy[i].xpos+1, enemy[i].ypos+13); // left
					g.drawLine (enemy[i].xpos+1, enemy[i].ypos+13, enemy[i].xpos+6, enemy[i].ypos+14); // left
					g.drawLine (enemy[i].xpos+9, enemy[i].ypos+14, enemy[i].xpos+12, enemy[i].ypos+16); // right
					g.drawLine (enemy[i].xpos+12, enemy[i].ypos+16, enemy[i].xpos+12, enemy[i].ypos+19); // right
					enemy[i].ladderAnimation++;
					if (enemy[i].ladderAnimation == 40) {
						enemy[i].ladderAnimation = 1;
					}
				}
			}
		}
			
		// player
		g.setColor(Color.white);
		// FALLING
		if (player1.falling) {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+6, player1.ypos+5, 3, 10); // body
			if (player1.fallAnimation <= 10) {
				// arms 1
				g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+12, player1.ypos+8);
				g.drawLine (player1.xpos, player1.ypos+3, player1.xpos+2, player1.ypos+8); // left
				g.drawLine (player1.xpos+12, player1.ypos+8, player1.xpos+14, player1.ypos+3); // right
				// legs 1
				g.drawLine (player1.xpos+2, player1.ypos+19, player1.xpos+4, player1.ypos+16); // left
				g.drawLine (player1.xpos+4, player1.ypos+16, player1.xpos+6, player1.ypos+15); // left
				g.drawLine (player1.xpos+9, player1.ypos+15, player1.xpos+11, player1.ypos+16); // right
				g.drawLine (player1.xpos+11, player1.ypos+16, player1.xpos+13, player1.ypos+19); // right
				
				player1.fallAnimation++;
			}
			else if (player1.fallAnimation <= 20) {
				// arms 2
				g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+12, player1.ypos+8);
				g.drawLine (player1.xpos, player1.ypos+13, player1.xpos+2, player1.ypos+8); // left
				g.drawLine (player1.xpos+12, player1.ypos+8, player1.xpos+14, player1.ypos+13); // right
				// legs 2
				g.drawLine (player1.xpos+5, player1.ypos+19, player1.xpos+4, player1.ypos+16); // left
				g.drawLine (player1.xpos+4, player1.ypos+16, player1.xpos+6, player1.ypos+15); // left
				g.drawLine (player1.xpos+9, player1.ypos+15, player1.xpos+11, player1.ypos+16); // right
				g.drawLine (player1.xpos+11, player1.ypos+16, player1.xpos+10, player1.ypos+19); // right
				
				player1.fallAnimation++;
				if (player1.fallAnimation == 20) {
					player1.fallAnimation = 1;
				}
			}
		}
		// BAR RIGHT
		else if (player1.lastDirection == 0 && player1.interact[2]) {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+6, player1.ypos+5, 2, 10); // body
			g.drawLine (player1.xpos+4, player1.ypos+20, player1.xpos+6, player1.ypos+17); // left
			g.drawLine (player1.xpos+6, player1.ypos+17, player1.xpos+6, player1.ypos+15); // left
			g.drawLine (player1.xpos+7, player1.ypos+15, player1.xpos+7, player1.ypos+20); // right
			if (player1.moving[0]) {
				if (player1.barAnimation <= 25) {
					// arms 1 
					g.drawLine (player1.xpos+2, player1.ypos+1, player1.xpos+2, player1.ypos+7); // left
					g.drawLine (player1.xpos+2, player1.ypos+7, player1.xpos+6, player1.ypos+9); // left
					g.drawLine (player1.xpos+8, player1.ypos+9, player1.xpos+13, player1.ypos+8); // right
					g.drawLine (player1.xpos+13, player1.ypos+8, player1.xpos+15, player1.ypos); // right
					player1.barAnimation++;
				}
				else if (player1.barAnimation <= 40) {
					// arms 2 
					g.drawLine (player1.xpos+3, player1.ypos+14, player1.xpos+4, player1.ypos+9); // left                           
					g.drawLine (player1.xpos+4, player1.ypos+9, player1.xpos+6, player1.ypos+9); // left
					g.drawLine (player1.xpos+8, player1.ypos+9, player1.xpos+11, player1.ypos+8); // right
					g.drawLine (player1.xpos+11, player1.ypos+8, player1.xpos+12, player1.ypos); //right
					player1.barAnimation++;
					if (player1.barAnimation == 40) {
						player1.barAnimation = 1;
					}
				}
			}
			else {
				g.drawLine (player1.xpos+2, player1.ypos+1, player1.xpos+2, player1.ypos+7); // left
				g.drawLine (player1.xpos+2, player1.ypos+7, player1.xpos+6, player1.ypos+9); // left
				g.drawLine (player1.xpos+8, player1.ypos+9, player1.xpos+13, player1.ypos+8); // right
				g.drawLine (player1.xpos+13, player1.ypos+8, player1.xpos+15, player1.ypos); // right
			}
			
		}
		// BAR LEFT
		else if (player1.lastDirection == 1 && player1.interact[2]) {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+7, player1.ypos+5, 2, 10); // body
			g.drawLine (player1.xpos+7, player1.ypos+15, player1.xpos+7, player1.ypos+19); // left
			g.drawLine (player1.xpos+8, player1.ypos+15, player1.xpos+8, player1.ypos+17); // right
			g.drawLine (player1.xpos+8, player1.ypos+17, player1.xpos+10, player1.ypos+20); // right
			if (player1.moving[1]) {
				if (player1.barAnimation <= 25) {
					// arms 1 
					g.drawLine (player1.xpos-1, player1.ypos, player1.xpos+2, player1.ypos+8); // left
					g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+6, player1.ypos+9); // left
					g.drawLine (player1.xpos+8, player1.ypos+9, player1.xpos+12, player1.ypos+7); // right
					g.drawLine (player1.xpos+12, player1.ypos+7, player1.xpos+12, player1.ypos+1); // right
					player1.barAnimation++;
				}
				else if (player1.barAnimation <= 40) {
					// arms 2 
					g.drawLine (player1.xpos+1, player1.ypos, player1.xpos+3, player1.ypos+8); // left
					g.drawLine (player1.xpos+3, player1.ypos+8, player1.xpos+6, player1.ypos+9); // left
					g.drawLine (player1.xpos+8, player1.ypos+9, player1.xpos+11, player1.ypos+9); // right
					g.drawLine (player1.xpos+11, player1.ypos+9, player1.xpos+12, player1.ypos+14); // right
					player1.barAnimation++;
					if (player1.barAnimation == 40) {
						player1.barAnimation = 1;
					}
				}
			}
			else {
				g.drawLine (player1.xpos-1, player1.ypos, player1.xpos+2, player1.ypos+8); // left
				g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+6, player1.ypos+9); // left
				g.drawLine (player1.xpos+8, player1.ypos+9, player1.xpos+12, player1.ypos+7); // right
				g.drawLine (player1.xpos+12, player1.ypos+7, player1.xpos+12, player1.ypos+1); // right
			}
		}
		// WALK RIGHT
		else if (player1.lastDirection == 0) {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+6, player1.ypos+5, 2, 10); // body
			if (player1.moving[0]) {
				if (player1.walkAnimation <= 25) {
					// arms 1 
					g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+12, player1.ypos+8);
					g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+2, player1.ypos+11); // left
					g.drawLine (player1.xpos+12, player1.ypos+8, player1.xpos+12, player1.ypos+5); // right
					// legs 1
					
					g.drawLine(player1.xpos+5, player1.ypos+18, player1.xpos+6, player1.ypos+15); // left
					g.drawLine(player1.xpos+7, player1.ypos+15, player1.xpos+8, player1.ypos+18); // right
					player1.walkAnimation++;
				}
				else if (player1.walkAnimation <= 50) {
					// arms 2 
					g.drawLine (player1.xpos+2, player1.ypos+10, player1.xpos+4, player1.ypos+12); // left                          
					g.drawLine (player1.xpos+2, player1.ypos+10, player1.xpos+6, player1.ypos+8); // left
					g.drawLine (player1.xpos+8, player1.ypos+8, player1.xpos+10, player1.ypos+10); // right
					g.drawLine (player1.xpos+10, player1.ypos+10, player1.xpos+12, player1.ypos+8); //right
					// legs 2
					g.drawLine(player1.xpos+3, player1.ypos+18, player1.xpos+6, player1.ypos+15); // left
					g.drawLine(player1.xpos+7, player1.ypos+15, player1.xpos+10, player1.ypos+18); // right
					player1.walkAnimation++;
					if (player1.walkAnimation == 50) {
						player1.walkAnimation = 1;
					}
				}
			}
			else {
				g.drawLine (player1.xpos+2, player1.ypos+10, player1.xpos+4, player1.ypos+12); // left                          
				g.drawLine (player1.xpos+2, player1.ypos+10, player1.xpos+6, player1.ypos+8); // left
				g.drawLine (player1.xpos+8, player1.ypos+8, player1.xpos+10, player1.ypos+10); // right
				g.drawLine (player1.xpos+10, player1.ypos+10, player1.xpos+12, player1.ypos+8); //right
				g.drawLine(player1.xpos+5, player1.ypos+18, player1.xpos+6, player1.ypos+15); // left
				g.drawLine(player1.xpos+7, player1.ypos+15, player1.xpos+8, player1.ypos+18); // right
			}
		}
		// WALK LEFT
		else if (player1.lastDirection == 1) {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+7, player1.ypos+5, 2, 10); // body
			if (player1.moving[1]) {
				if (player1.walkAnimation <= 25) {
					// arms 1 
					g.drawLine (player1.xpos+2, player1.ypos+8, player1.xpos+12, player1.ypos+8);
					g.drawLine (player1.xpos+2, player1.ypos+5, player1.xpos+2, player1.ypos+8); // left
					g.drawLine (player1.xpos+12, player1.ypos+8, player1.xpos+12, player1.ypos+11); // right
					// legs 1
					g.drawLine (player1.xpos+6, player1.ypos+18, player1.xpos+7, player1.ypos+15); // left
					g.drawLine (player1.xpos+8, player1.ypos+15, player1.xpos+9, player1.ypos+18); // right
					player1.walkAnimation++;
				}
				else if (player1.walkAnimation <= 50) {
					// arms 2 
					g.drawLine (player1.xpos+1, player1.ypos+8, player1.xpos+4, player1.ypos+11); // left
					g.drawLine (player1.xpos+4, player1.ypos+11, player1.xpos+7, player1.ypos+7); // left
					g.drawLine (player1.xpos+9, player1.ypos+7, player1.xpos+13, player1.ypos+9); // right
					g.drawLine (player1.xpos+13, player1.ypos+9, player1.xpos+10, player1.ypos+13); // right
					
					// legs 2
					g.drawLine(player1.xpos+4, player1.ypos+18, player1.xpos+7, player1.ypos+15); // left
					g.drawLine(player1.xpos+8, player1.ypos+15, player1.xpos+11, player1.ypos+18); // right
					player1.walkAnimation++;
					if (player1.walkAnimation == 50) {
						player1.walkAnimation = 1;
					}
				}
			}
			else {
				g.drawLine (player1.xpos+1, player1.ypos+8, player1.xpos+4, player1.ypos+11); // left
				g.drawLine (player1.xpos+4, player1.ypos+11, player1.xpos+7, player1.ypos+7); // left
				g.drawLine (player1.xpos+9, player1.ypos+7, player1.xpos+13, player1.ypos+9); // right
				g.drawLine (player1.xpos+13, player1.ypos+9, player1.xpos+10, player1.ypos+13); // right
				g.drawLine (player1.xpos+6, player1.ypos+18, player1.xpos+7, player1.ypos+15); // left
				g.drawLine (player1.xpos+8, player1.ypos+15, player1.xpos+9, player1.ypos+18); // right
			}
		}
		// GO DOWN OR UP
		else if ((player1.lastDirection == 3 || player1.lastDirection == 2) && player1.interact[3]) {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+6, player1.ypos+5, 3, 10); // body
			if (player1.moving[2] || player1.moving[3]) {
				if (player1.ladderAnimation <= 20) {
					// arms 1
					g.drawLine (player1.xpos+1, player1.ypos, player1.xpos+1, player1.ypos+5); // left
					g.drawLine (player1.xpos+1, player1.ypos+5, player1.xpos+6, player1.ypos+7); // left
					g.drawLine (player1.xpos+9, player1.ypos+7, player1.xpos+13, player1.ypos+7); // right
					g.drawLine (player1.xpos+13, player1.ypos+7, player1.xpos+13, player1.ypos+3); // right
					// legs 1
					g.drawLine (player1.xpos+2, player1.ypos+19, player1.xpos+2, player1.ypos+16); //left
					g.drawLine (player1.xpos+2, player1.ypos+16, player1.xpos+6, player1.ypos+15); //left
					g.drawLine (player1.xpos+9, player1.ypos+14, player1.xpos+13, player1.ypos+13); //right
					g.drawLine (player1.xpos+13, player1.ypos+13, player1.xpos+13, player1.ypos+15); // right
					player1.ladderAnimation ++;
				}
				else if (player1.ladderAnimation <= 40) {
					// arms 2
					g.drawLine (player1.xpos+1, player1.ypos+3, player1.xpos+1, player1.ypos+7); // left
					g.drawLine (player1.xpos+1, player1.ypos+7, player1.xpos+6, player1.ypos+7); // left
					g.drawLine (player1.xpos+9, player1.ypos+7, player1.xpos+14, player1.ypos+5); // right
					g.drawLine (player1.xpos+14, player1.ypos+5, player1.xpos+14, player1.ypos); // right
					// legs 2
					g.drawLine (player1.xpos+1, player1.ypos+16, player1.xpos+1, player1.ypos+13); // left
					g.drawLine (player1.xpos+1, player1.ypos+13, player1.xpos+6, player1.ypos+14); // left
					g.drawLine (player1.xpos+9, player1.ypos+14, player1.xpos+12, player1.ypos+16); // right
					g.drawLine (player1.xpos+12, player1.ypos+16, player1.xpos+12, player1.ypos+19); // right
					player1.ladderAnimation++;
					if (player1.ladderAnimation == 40) {
						player1.ladderAnimation = 1;
					}
				}
			}
			else {
				g.drawLine (player1.xpos+1, player1.ypos, player1.xpos+1, player1.ypos+5); // left
				g.drawLine (player1.xpos+1, player1.ypos+5, player1.xpos+6, player1.ypos+7); // left
				g.drawLine (player1.xpos+9, player1.ypos+7, player1.xpos+13, player1.ypos+7); // right
				g.drawLine (player1.xpos+13, player1.ypos+7, player1.xpos+13, player1.ypos+3); // right
				g.drawLine (player1.xpos+2, player1.ypos+19, player1.xpos+2, player1.ypos+16); //left
				g.drawLine (player1.xpos+2, player1.ypos+16, player1.xpos+6, player1.ypos+15); //left
				g.drawLine (player1.xpos+9, player1.ypos+14, player1.xpos+13, player1.ypos+13); //right
				g.drawLine (player1.xpos+13, player1.ypos+13, player1.xpos+13, player1.ypos+15); // right
			}
		}
		else {
			g.fillRect (player1.xpos+4, player1.ypos, tileSize-8, tileSize-10); // head
			g.fillRect (player1.xpos+6, player1.ypos+5, 2, 10); // body
			g.drawLine (player1.xpos+2, player1.ypos+10, player1.xpos+4, player1.ypos+12); // left                          
			g.drawLine (player1.xpos+2, player1.ypos+10, player1.xpos+6, player1.ypos+8); // left
			g.drawLine (player1.xpos+8, player1.ypos+8, player1.xpos+10, player1.ypos+10); // right
			g.drawLine (player1.xpos+10, player1.ypos+10, player1.xpos+12, player1.ypos+8); //right
			g.drawLine(player1.xpos+5, player1.ypos+18, player1.xpos+6, player1.ypos+15); // left
			g.drawLine(player1.xpos+7, player1.ypos+15, player1.xpos+8, player1.ypos+18); // right
		}
		
		// level
		g.setColor(Color.cyan);
		for (int i = 0; i < level.length; i++) {
			if (level[i]) {
				g.drawString("Level: " + (i+1), 20, 15);
			}
		}
		// lives
		g.setColor(Color.red);
		g.drawString("Live(s): " + player1.lives, 120, 15);
		//shots
		g.setColor(Color.gray);
		g.drawString("Shots: " + (20 - player1.shots), 230, 15);
		// score
		g.setColor(Color.yellow);
		g.drawString("Score: " + player1.score, 330, 15);
		//shots
		g.setColor(Color.gray);
		g.drawString("Shots: ", 230, 15);
		
		if (level[3]) {
			g.setColor(Color.white);
			g.drawString("RAAGGGHHHHHH YOU LICK DIRTY HOBO BAAAALLLLSSSSSSS !^&@%#&!@*@&$^#*(&#$%@)", 100, 100);
		}
		
		// PAUSE 
		if (pause) {
			g.setFont(pauseFont);
			if (pauseAnimation) {
				g.setColor(Color.lightGray);
				g.drawString("PAUSE", 328, 217);
				g.setColor(Color.white);
				g.drawString("PAUSE", 330, 215);
			}
			if (pauseAnimation && pauseCounter == 75) {
				pauseAnimation = false;
				pauseCounter = 0;
			}
			if (!pauseAnimation && pauseCounter == 75) {
				pauseAnimation = true;
				pauseCounter = 0;
			}
			pauseCounter++;
		}
		
		 /* // EDITTING GRID
		g.setColor(Color.cyan);
		g.setFont(editor);
		for (int a = 0; a < 50; a++) {
			for (int b = 0; b < 30; b++) {
				g.drawString("0     1     2     3      4      5     6      7     8     9    10    11   12   13     14    15   16   17    18   19    20   21   22    23    24    25   26    27   28   29    30   31    32   33    34    35   36   37    38    39    40   41    42   43   44    45    46   47    48   49", 7,7);
				g.drawString("1", 7, 15*1 +7);
				g.drawString("2", 7, 15*2 +7);
				g.drawString("3", 7, 15*3 +7);
				g.drawString("4", 7, 15*4 +7);
				g.drawString("5", 7, 15*5 +7);
				g.drawString("6", 7, 15*6 +7);
				g.drawString("7", 7, 15*7 +7);
				g.drawString("8", 7, 15*8 +7);
				g.drawString("9", 7, 15*9 +7);
				g.drawString("10", 7, 15*10 +7);
				g.drawString("11", 7, 15*11 +7);
				g.drawString("12", 7, 15*12 +7);
				g.drawString("13", 7, 15*13 +7);
				g.drawString("14", 7, 15*14 +7);
				g.drawString("15", 7, 15*15 +7);
				g.drawString("16", 7, 15*16 +7);
				g.drawString("17", 7, 15*17 +7);
				g.drawString("18", 7, 15*18 +7);
				g.drawString("19", 7, 15*19 +7);
				g.drawString("20", 7, 15*20 +7);
				g.drawString("21", 7, 15*21 +7);
				g.drawString("22", 7, 15*22 +7);
				g.drawString("23", 7, 15*23 +7);
				g.drawString("24", 7, 15*24 +7);
				g.drawString("25", 7, 15*25 +7);
				g.drawString("26", 7, 15*26 +7);
				g.drawString("27", 7, 15*27 +7);
				g.drawString("28", 7, 15*28 +7);
				g.drawString("29", 7, 15*29 +7);
				g.drawLine (a*tileSize, 0, a*tileSize, 450);
				g.drawLine (0, b*tileSize, 750, b*tileSize);
				
			}
		}//*/
		
	}
	
	// TILE GENERATOR METHOD
	public void tileGenerator (int firstX, int firstY, int lastX, int lastY, int type, boolean t) {
	    for (int yTile = firstY; yTile <= lastY; yTile++) {
		for (int xTile = firstX; xTile <= lastX; xTile++) {
			tileArray[yTile][xTile].type = type;
			tileArray[yTile][xTile].thin = t;
		}
	    }       
	}
	
	
	
	
	// PLAYER INTERACT METHODS
	// INTERACT GROUND
	public boolean interactGround (player p) {
	    for (int yTile = 0; yTile <= 29; yTile++) {
		    for (int xTile = 0; xTile <= 49; xTile++) {
			    if (tileArray[yTile][xTile].type == 1 && !p.interact[0]) {
				    if ((p.xpos <= tileArray[yTile][xTile].xpos + tileSize && p.xpos + tileSize >= tileArray[yTile][xTile].xpos) && p.ypos+tileSize <= (tileArray[yTile][xTile].ypos+5) && p.ypos+tileSize >= (tileArray[yTile][xTile].ypos-5)) {
					p.interact[1] = true;
					return p.interact[1];
				    }
			    }
			    else {
				p.interact[1] = false;
			    }
		    }
	    }
	return p.interact[1];
	}

	// INTERACT WALLS
		 public boolean interactRightWall (player p) {
		    if (p.lastDirection == 0 && tileArray[p.ypos/15][(p.xpos/15)+1].type == 1) {
			p.interact[5] = true;
		    }
		    else {
			p.interact[5] = false;
		    }
		    return p.interact[5];
		 }
		 
		 public boolean interactLeftWall (player p) {
		    if (p.lastDirection == 1 && tileArray[p.ypos/15][(p.xpos/15)].type == 1) {
			p.interact[6] = true;
		    }
		    else {
			p.interact[6] = false;
		    }
		    return p.interact[6];
		 }
		 
	// INTERACT BARS
	public boolean interactBars(player p){
		for (int yTile = 0; yTile <= 29; yTile++) {
		for (int xTile = 0; xTile <= 49; xTile++) {
			if (tileArray[yTile][xTile].type == 2) {
				if (p.xpos-tileSize <= tileArray[yTile][xTile].xpos && p.xpos+tileSize >= tileArray[yTile][xTile].xpos && p.ypos <= tileArray[yTile][xTile].ypos+3 && p.ypos >= tileArray[yTile][xTile].ypos-3) {
					p.interact[2] = true;
					return p.interact[2];
				}
			}
			else {
				p.interact[2] = false;
			}
		}
	    }
	    return p.interact[2];
	}

	// INTERACT LADDERS
	public boolean interactLadder (player p) {
	    for (int yTile = 0; yTile <= 29; yTile++) {
		for (int xTile = 0; xTile <= 49; xTile++) {
			if (tileArray[yTile][xTile].type == 3 && !p.interact[0]) {
				if (p.xpos >= (tileArray[yTile][xTile].xpos-5) && p.xpos <= (tileArray[yTile][xTile].xpos+5) && p.ypos <= tileArray[yTile][xTile].ypos && p.ypos >= tileArray[yTile][xTile].ypos - tileSize) {
					p.interact[3] = true;
					return p.interact[3];
				}
			}
			else {
				p.interact[3] = false;
			}
		}
	    }
	    return p.interact[3];
	}
	
	public boolean interactLadder2 (player p) {
		for (int yTile = 0; yTile <= 29; yTile++) {
		for (int xTile = 0; xTile <= 49; xTile++) {
			if (tileArray[yTile][xTile].type == 3 && !p.interact[0]) {
				if (p.xpos >= (tileArray[yTile][xTile].xpos-15) && p.xpos <= (tileArray[yTile][xTile].xpos+15) && p.ypos <= tileArray[yTile][xTile].ypos && p.ypos >= tileArray[yTile][xTile].ypos - tileSize) {
					p.interact[7] = true;
					return p.interact[7];
				}
			}
			else {
				p.interact[7] = false;
			}
		}
	    }
	    return p.interact[7];
	}

	// INTERACT AIR
	public boolean interactAir (player p) {
		if (p.moving[3] && tileArray[p.ypos/15][p.xpos/15].type == 2) {
		p.interact[0] = false;
		return p.interact[0];
	    }
		else if ((p.lastDirection == 0 || p.lastDirection == 3 || p.lastDirection == 2) && tileArray[(p.ypos/15)+1][p.xpos/15].type == 0 && !p.interact[3] && (p.xpos%15 == 1 || p.xpos%15 == 0) && !p.interact[2] && !(tileArray[(p.ypos/15)+1][p.xpos/15].destroyed) && !(tileArray[(p.ypos/15)+1][p.xpos/15].thinDestroyed)) {
		p.interact[0] = true;
	    }
	    else if ((p.lastDirection == 1 || p.lastDirection == 3 || p.lastDirection == 2) && tileArray[(p.ypos/15)+1][(p.xpos/15+1)].type == 0 && !p.interact[3] && (p.xpos%15 == 14 || p.xpos%15 == 0) && !p.interact[2] && !(tileArray[(p.ypos/15)+1][(p.xpos/15+1)].destroyed) && !(tileArray[(p.ypos/15)+1][(p.xpos/15+1)].thinDestroyed)) {
		p.interact[0] = true;
	    }
	    else if (p.falling && tileArray[(p.ypos/15)+1][p.xpos/15].type == 2){
		p.interact[0] = true;
		p.direction[3] = false;
	    }
	    else if ((p.lastDirection == 0 || p.lastDirection == 3 || p.lastDirection == 2) && tileArray[(p.ypos/15)+1][p.xpos/15].type == 0 && !p.interact[3] && (p.xpos%15 == 1 || p.xpos%15 == 0) && !p.interact[2] && tileArray[(p.ypos/15)+1][p.xpos/15].destroyed) {
		p.interact[0] = true;
	    }
	    else if ((p.lastDirection == 1 || p.lastDirection == 3 || p.lastDirection == 2) && tileArray[(p.ypos/15)+1][(p.xpos/15)].type == 0 && !p.interact[3] && (p.xpos%15 == 14 || p.xpos%15 <= 3) && !p.interact[2] && tileArray[(p.ypos/15)+1][(p.xpos/15)].destroyed) {
		p.interact[0] = true;
	    }
	    else if ((p.lastDirection == 0 || p.lastDirection == 3 || p.lastDirection == 2) && tileArray[(p.ypos/15)+1][p.xpos/15].type == 0 && !p.interact[3] && (p.xpos%15 == 1 || p.xpos%15 == 0) && !p.interact[2] && tileArray[(p.ypos/15)+1][p.xpos/15].thinDestroyed) {
		p.interact[0] = true;
	    }
	    else if ((p.lastDirection == 1 || p.lastDirection == 3 || p.lastDirection == 2) && tileArray[(p.ypos/15)+1][(p.xpos/15)].type == 0 && !p.interact[3] && (p.xpos%15 == 14 || p.xpos%15 <= 1) && !p.interact[2] && tileArray[(p.ypos/15)+1][(p.xpos/15)].thinDestroyed) {
		p.interact[0] = true;
	    }
	    else if (!p.interact[1] && !p.interact[2] && !p.interact[3] && !p.interact[4] && !p.interact[5] && !p.interact[6] && !p.interact[7] && !(p.ypos%15 == 1)){
		p.interact[0] = true;
	    }
	    else {
		p.interact[0] = false;
	    }
	    return p.interact[0];
	 }
	 
	 
	  
	
	// PLAYER MOVE AND KEYS---------------------------------------
	public void playerMove (player p) {
		if (p.direction[0] && (p.interact[1] || p.interact[2] || p.interact[7]) && !p.interact[5]) { // if player is pressing right && is interacting with ground/bars/ladder(walk) and not right wall
			p.xpos = p.xpos + 1;
			p.moving[0] = true; // declares player IS moving right
			p.lastDirection = 0;
			//walk.play();
		}
		else if (p.direction[1] && (p.interact[1] || p.interact[2] || p.interact[7]) && !p.interact[6]) { // if player is pressing left && is interacting with ground/bars/ladder(walk) and not left wall
			p.xpos = p.xpos - 1;
			p.moving[1] = true; // declares player IS moving left
			p.lastDirection = 1;
			//walk.play();
		}
		else {
		    p.moving[0] = false;
		    p.moving[1] = false;
		}
		
		
		if (p.direction[2] && (p.interact[3])) { // if player is pressing up && is interacting with ladders
			p.ypos = p.ypos - 1;
			p.moving[2] = true; // declares player is moving up
			p.lastDirection = 2;
		}
		else if (p.direction[3] && (p.interact[3] || (p.interact[2] && !p.interact[1]))) { // if player is pressing down && is interacting with ladders OR (interacting with bars but not ground)
			p.ypos = p.ypos + 1;
			p.moving[3] = true; // declares playing is moving down
			p.falling = true;
			p.lastDirection = 3;
		}
		else {
		    p.moving[2] = false;
		    p.moving[3] = false;
		    p.falling = false;
		}
		
		
		if (p.interact[0]) {
		    p.ypos = p.ypos + 1;
		    p.falling = true;
		}
		else {
			p.falling = false;
		}
	}
	
	// GOLD METHODS
	public void collectGold (player p) {
		if (p.moving[1] && tileArray[p.ypos/15][p.xpos/15].gold > 0) {
			p.score += 100*tileArray[p.ypos/15][p.xpos/15].gold;
			tileArray[p.ypos/15][p.xpos/15].gold = 0;
			getCashMoney.play();
		}
		else if (p.moving[0] && tileArray[p.ypos/15][(p.xpos/15) + 1].gold > 0) {
			p.score += 100*tileArray[p.ypos/15][(p.xpos/15) + 1].gold;
			tileArray[p.ypos/15][(p.xpos/15) + 1].gold = 0;
			getCashMoney.play();
		}
		else if (p.moving[2] && tileArray[(p.ypos/15)][p.xpos/15].gold > 0) {
			p.score += 100*tileArray[(p.ypos/15)][p.xpos/15].gold;
			tileArray[p.ypos/15][p.xpos/15].gold = 0;
			getCashMoney.play();
		}
		else if (p.falling && tileArray[(p.ypos/15)+1][(p.xpos/15)+1].gold > 0 && p.xpos%15 == 14) {
			p.score += 100*tileArray[(p.ypos/15)+1][(p.xpos/15)+1].gold;
			tileArray[(p.ypos/15) + 1][(p.xpos/15)+1].gold = 0;
			getCashMoney.play();
		}
		else if (p.falling && tileArray[(p.ypos/15)+1][(p.xpos/15)].gold > 0 && (p.xpos%15 == 1 || p.xpos%15 == 0)) {
			p.score += 100*tileArray[(p.ypos/15)+1][(p.xpos/15)].gold;
			tileArray[(p.ypos/15) + 1][(p.xpos/15)].gold = 0;
			getCashMoney.play();
		}
	}
	
	public void enemyCollectGold (player p) {
		if (p.moving[1] && tileArray[p.ypos/15][p.xpos/15].gold > 0) {
			p.gold = true;
			tileArray[p.ypos/15][p.xpos/15].gold -= 1;
		}
		else if (p.moving[0] && tileArray[p.ypos/15][(p.xpos/15) + 1].gold > 0) {
			p.gold = true;
			tileArray[p.ypos/15][(p.xpos/15) + 1].gold -= 1;
		}
		else if (p.moving[2] && tileArray[(p.ypos/15)][p.xpos/15].gold > 0) {
			p.gold = true;
			tileArray[p.ypos/15][p.xpos/15].gold -= 1;
		}
		else if (p.falling && tileArray[(p.ypos/15)+1][(p.xpos/15)+1].gold > 0 && p.xpos%15 == 14) {
			p.gold = true;
			tileArray[(p.ypos/15) + 1][(p.xpos/15)+1].gold -= 1;
		}
		else if (p.falling && tileArray[(p.ypos/15)+1][(p.xpos/15)].gold > 0 && (p.xpos%15 == 1 || p.xpos%15 == 0)) {
			p.gold = true;
			tileArray[(p.ypos/15) + 1][(p.xpos/15)].gold -= 1;
		}
	}
	
	public void dropGold (player p) {
		if (tileArray[p.ypos/15][p.xpos/15].destroyed) {
			tileArray[p.ypos/15][p.xpos/15].type = 1;
			if (p.deadCounter == 0){
				die.play();
			}
			p.deadCounter++;
			if (p.gold) {
				p.gold = false;
				tileArray[(p.ypos/15)-1][p.xpos/15].gold += 1;
			}
		}
		if (p.deadCounter >= 200) {
			p.ypos = p.ypos - 15;
			p.deadCounter = 0;
		}
	}
	
	// KEYBOARD METHODS
	public void keyPressed (KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			player1.direction[0] = true;
			player1.direction[1] = false;
			player1.direction[2] = false;
			player1.direction[3] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			player1.direction[0] = false;
			player1.direction[1] = true;
			player1.direction[2] = false;
			player1.direction[3] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			player1.direction[0] = false;
			player1.direction[1] = false;
			player1.direction[2] = true;
			player1.direction[3] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			player1.direction[0] = false;
			player1.direction[1] = false;
			player1.direction[2] = false;
			player1.direction[3] = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_F1) {
			player1.lives--;
			if (level[0]) {
				player1.xpos = 30;
				player1.ypos = 45;
			}
			if (level[1]) {
				player1.xpos = 100;
				player1.ypos = 400;
			}
			if (level[2]) {
				player1.xpos = 30;
				player1.ypos = 165;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_X) {
			if (!pause) {
				player1.shoot[0] = true;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_Z) {
		    if (!pause) {
				player1.shoot[1] = true;
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			if (!pause) {
			pause = true;
			lobby.loop();
			track.stop();
		    }
		}
		if (e.getKeyCode() == KeyEvent.VK_P) {
			if (pause) {
			pause = false;
				lobby.stop();
				track.loop();
		    }
		}
		if (e.getKeyCode() == KeyEvent.VK_A) {
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			player1.direction[0] = false;
			if (player1.interact[1] && player1.interact[3] && player1.xpos%15 <= 7) {
			    player1.xpos = player1.xpos - (player1.xpos%15);
			}
			else if (player1.interact[1] && player1.ypos%15 >= 8 && player1.xpos%15 <= 15) {
			    player1.xpos = player1.xpos + (15 - player1.xpos%15);
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			player1.direction[1] = false;
			if (player1.interact[1] && player1.interact[3] && player1.xpos%15 <= 7) {
			    player1.xpos = player1.xpos - (player1.xpos%15);
			}
			else if (player1.interact[1] && player1.xpos%15 >= 8 && player1.xpos%15 <= 15) {
			    player1.xpos = player1.xpos + (15 - player1.xpos%15);
			}
		}
		if (player1.interact[3] && e.getKeyCode() == KeyEvent.VK_UP) {
			player1.direction[2] = false;
		}
		if (player1.interact[3] && e.getKeyCode() == KeyEvent.VK_DOWN) {
			player1.direction[3] = false;
		}
	}
	
	
	
	//ENEMY THINKING METHOD
	public void enemyAI (player p) {
		
		if (p.ypos+3 >= player1.ypos && p.ypos <= player1.ypos+3) {
			chasePlayer(p);
		}
		else if (p.xpos%15 == 0){
			closestLadder(p);
		}
		else if (!p.moving[0] && !p.moving[1] && !p.moving[2] && !p.moving[3]) {
			randomMove(p);
		}
		
	}
	
	// 1ST PRIORITY PATH
	public void chasePlayer(player p) {
		if (p.xpos < player1.xpos && p.interact[3] && tileArray[p.ypos/15][p.xpos/15].type == 0) {
			p.direction[0] = true;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = false;

			return;
		}
		else if (p.xpos > player1.xpos && p.interact[3] && tileArray[p.ypos/15][(p.xpos/15)+1].type == 0) {
			p.direction[0] = false;
			p.direction[1] = true;
			p.direction[2] = false;
			p.direction[3] = false;

			return;
		}
		else if (p.xpos < player1.xpos) {
			p.direction[0] = true;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = false;

			return;
		}
		else if (p.xpos > player1.xpos) {
			p.direction[0] = false;
			p.direction[1] = true;
			p.direction[2] = false;
			p.direction[3] = false;

			return;
		}
	}
	
	// 2ND PRIORITY PATH
	public void closestLadder(player p) {
		closestLadderX = 1000;
		if (p.ypos > player1.ypos && (p.interact[1] || p.interact[2]) && (!p.interact[3] || p.exception)) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[p.ypos/15][xTile].type == 3) {
					if (Math.abs((xTile*15) - p.xpos) < Math.abs(closestLadderX - p.xpos) && !(xTile*15 == lastLadderX)) {
						validLadder = true;
						if (xTile*15 < p.xpos) {
							for (int x = xTile; x < (p.xpos/15); x++) {
								if (tileArray[p.ypos/15][x].type == 1) {
									validLadder = false;
								}
							}
						}
						if (xTile*15 > p.xpos) {
							for (int x = xTile; x < (p.xpos/15); x--) {
								if (tileArray[p.ypos/15][x].type == 1) {
									validLadder = false;
								}
							}
						}
						if (validLadder) {
							closestLadderX = xTile*15;
						}
					}
				}
				p.exception = false;
			}
			if (p.xpos < closestLadderX) {
				p.direction[0] = true;
				p.direction[1] = false;
				p.direction[2] = false;
				p.direction[3] = false;
				return;
			}
			else if (p.xpos > closestLadderX) {
				p.direction[0] = false;
				p.direction[1] = true;
				p.direction[2] = false;
				p.direction[3] = false;
				return;
			}
		}
		if (p.ypos < player1.ypos && (p.interact[1] || p.interact[2]) && (!p.interact[3] || p.exception)) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[(p.ypos/15)+1][xTile].type == 3) {
					if (Math.abs((xTile*15) - p.xpos) < Math.abs(closestLadderX - p.xpos) && !(xTile*15 == lastLadderX)) {
						validLadder = true;
						if (xTile*15 < p.xpos) {
							for (int x = xTile; x < (p.xpos/15); x++) {
								if (tileArray[p.ypos/15][x].type == 1) {
									validLadder = false;
								}
							}
						}
						if (xTile*15 > p.xpos) {
							for (int x = xTile; x < (p.xpos/15); x--) {
								if (tileArray[p.ypos/15][x].type == 1) {
									validLadder = false;
								}
							}
						}
						if (validLadder) {
							closestLadderX = xTile*15;
						}
					}
				}
				p.exception = false;
			}
			if (p.xpos < closestLadderX) {
				p.direction[0] = true;
				p.direction[1] = false;
				p.direction[2] = false;
				p.direction[3] = false;
				return;
			}
			else if (p.xpos > closestLadderX) {
				p.direction[0] = false;
				p.direction[1] = true;
				p.direction[2] = false;
				p.direction[3] = false;
				return;
			}
		}
			
		if (p.ypos < player1.ypos && p.interact[3] && p.xpos%15 == 0) {
			goDownLadder(p);
		}
		else if (p.ypos > player1.ypos && p.interact[3]) {
			goUpLadder(p);
		}
		return;         
	}
	
	public void goUpLadder(player p) {
		if (p.ypos >= player1.ypos && tileArray[(p.ypos/15-1)][p.xpos/15].type == 3) {
			p.direction[0] = false;
			p.direction[1] = false;
			p.direction[2] = true;
			p.direction[3] = false;
			lastLadderX = p.xpos;
			return;
		}
		if (!(tileArray[(p.ypos/15-1)][p.xpos/15].type == 3) && (p.moving[0] || p.moving[1])) {
			p.direction[0] = false;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = false;
			p.exception = true;
			enemyAI (p);
		}
		return;
	}
	
	public void goDownLadder (player p) {
		if (p.ypos <= player1.ypos && tileArray[(p.ypos/15)+1][(p.xpos/15)].type == 3) {
			p.direction[0] = false;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = true;
			lastLadderX = p.xpos;
			return;
		}
		if (!(tileArray[(p.ypos/15+1)][p.xpos/15].type == 3) && (p.moving[0] || p.moving[1])) {
			p.direction[0] = false;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = false;
			p.exception = true;
			enemyAI (p);
		}
		return;
	}
	
	// 3RD PRIORITY PATH
	public void randomMove(player p) {
		if (p.ypos > player1.ypos && p.interact[3]) {
			p.direction[0] = false;
			p.direction[1] = false;
			p.direction[2] = true;
			p.direction[3] = false;
		}
		else if (p.ypos < player1.ypos && (p.interact[3] || p.interact[2])) {
			p.direction[0] = false;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = true;
		}
		else if (p.xpos < player1.xpos) {
			p.direction[0] = true;
			p.direction[1] = false;
			p.direction[2] = false;
			p.direction[3] = false;
		}
		else if (p.xpos > player1.xpos) {
			p.direction[0] = false;
			p.direction[1] = true;
			p.direction[2] = false;
			p.direction[3] = false;
		}
		//enemyAI(p);
	}
	
	
	// PLAYER SHOOT METHOD
	public void shootRight (player p) {
		if(!pause) {
		if (p.interact[1] && p.xpos%15 < 4 && tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].type == 1 && (tileArray[p.ypos/15][(p.xpos/15) + 1].type == 0)) {
			if (!(tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].indestructible)) {
				tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].type = 0;
				if (!(tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].thin)) {
					tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].destroyed = true;
					p.shots++;
				}
				else if (tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].thin) {
					tileArray[(p.ypos/15) + 1][(p.xpos/15) + 1].thinDestroyed = true;
					p.shots++;
				}
			}
		}
		else if (p.interact[1] && p.xpos%15 > 10 && tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].type == 1 && tileArray[p.ypos/15][(p.xpos/15) + 2].type == 0) {
			if (!tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].indestructible) {
				tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].type = 0;
				if (!(tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].thin)) {
					tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].destroyed = true;
					p.shots++;
				}
				else if (tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].thin) {
					tileArray[(p.ypos/15) + 1][(p.xpos/15) + 2].thinDestroyed = true;
					p.shots++;
				}
			}
		}
		shoot.play();
		}
	    p.shoot[0] = false;
	}
	public void shootLeft (player p) {
		if (!pause) {
			if (p.interact[1] && p.xpos%15 < 5 && tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].type == 1 && tileArray[p.ypos/15][(p.xpos/15) - 1].type == 0) {
				if (!tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].indestructible) {
					tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].type = 0;
					if (!(tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].thin)) {
						tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].destroyed = true;
						p.shots++;
					}
					else if (tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].thin) {
						tileArray[(p.ypos/15) + 1][(p.xpos/15) - 1].thinDestroyed = true;
						p.shots++;
					}
				}
			}
			else if (p.interact[1] && p.xpos%15 > 11 && tileArray[(p.ypos/15) + 1][p.xpos/15].type == 1 && tileArray[p.ypos/15][p.xpos/15].type == 0) {
				if (!tileArray[(p.ypos/15) + 1][(p.xpos/15)].indestructible) {
					tileArray[(p.ypos/15) + 1][(p.xpos/15)].type = 0;
					if (!(tileArray[(p.ypos/15) + 1][(p.xpos/15)].thin)) {
						tileArray[(p.ypos/15) + 1][(p.xpos/15)].destroyed = true;
						p.shots++;
					}
					else if (tileArray[(p.ypos/15) + 1][(p.xpos/15)].thin) {
						tileArray[(p.ypos/15) + 1][(p.xpos/15)].thinDestroyed = true;
						p.shots++;
					}
				}
			}
			shoot.play();
		}
	    p.shoot[1] = false;
	}
	
	// PLAYER POSITION RESET
	public void playerAdjust (player p) {
		if ((p.moving[0] || p.moving[1]) && p.ypos%15 <= 7) {
			p.ypos = p.ypos - (p.ypos%15);
	    }
	    else if ((p.moving[0] || p.moving[1]) && p.ypos%15 >= 8 && p.ypos%15 < 15) {
			p.ypos = p.ypos + (15 - p.ypos%15);
	    }
	    if ((p.moving[2] || p.moving[3]) && p.xpos%15 <= 7) {
		p.xpos = p.xpos - (p.xpos%15);
	    }
	    else if ((p.moving[2] || p.moving[3]) && p.xpos%15 >= 8 && p.xpos%15 < 15) {
		p.xpos = p.xpos + (15 - p.xpos%15);
	    }
	    if (p.interact[5] && p.xpos%15 < 7) {
		p.xpos = p.xpos - (p.xpos%15);
	    }
	    else if (p.interact[6] && p.xpos%15 > 8) {
		p.xpos = p.xpos + (15 - p.xpos%15);
	    }
	}
	
	
	
	public void checkKill (player p) {
		if (p.xpos < player1.xpos+13 && p.xpos+13 > player1.xpos && p.ypos+13 > player1.ypos && p.ypos < player1.ypos+13) {
			if (level[0]) {
				player1.xpos = 30;
				player1.ypos = 45;
			}
			if (level[1]) {
				player1.xpos = 700;
				player1.ypos = 390;
			}
			if (level[2]) {
				player1.xpos = 30;
				player1.ypos = 165;
			}
			player1.lives--;
			if (player1.lives < 0) {
				startup = true;
				init();
			}
		}
	}
	
	// RESPAWN IF DEAD
	public void respawn (player p) {
		if (tileArray[p.ypos/15][p.xpos/15].destroyed) {
			tileArray[p.ypos/15][p.xpos/15].destroyed = false;
			tileArray[p.ypos/15][p.xpos/15].type = 1;
			if (level[0]) {
				p.xpos = 30;
				p.ypos = 45;
				respawn.play();
			}
			if (level[1]) {
				p.xpos = 700;
				p.ypos = 390;
				respawn.play();
			}
			if (level[2]) {
				p.xpos = 30;
				p.ypos = 165;
				respawn.play();
			}
			p.lives--;
		}
		if (p.lives < 0) {
			startup = true;
			init();
		}
	}
	
	// CHECK TO OPEN ENDGAME DOOR
	public boolean checkDoor() {
		for (int i = 0; i < enemy.length; i++) {
			if (enemy[i].gold) { 
				door = false;
				return door;
			}
		}
		for (int yTile = 0; yTile <= 29; yTile++) {
			for (int xTile = 0; xTile <= 49; xTile++) {
				if (tileArray[yTile][xTile].gold > 0) {
					door = false;
					return door;
				}
			}
		}
		door = true;
		return door;
	}
	
	// CHECK IF FINISHED LEVEL
	public void checkWin (player p) {
		if (level[0] && door) {
			if (p.xpos >= 705 && p.ypos >= 378) {
				level[0] = false;
				level[1] = true;
				door = false;
				init();
			}
		}
		if (level[1] && door) {
			if (p.xpos >= 23 && p.xpos <= 45 && p.ypos <= 165) {
				level[1] = false;
				level[2] = true;
				door = false;
				init();
			}
		}
		if (level[2] && door) {
			if (p.xpos >= 20 && p.xpos <= 45 && p.ypos >= 390) {
				level[2] = false;
				level[3] = true;
				door = false;
				init();
			}
			
		}
	}
	
	
	
	
	
	public void keyTyped(KeyEvent e) {}
	

	public void update (Graphics g)
    {

	if (dbImage == null)
	{
	    dbImage = createImage (this.getSize ().width, this.getSize ().height);
	    dbg = dbImage.getGraphics ();
	}


	dbg.setColor (getBackground ());
	dbg.fillRect (0, 0, this.getSize ().width, this.getSize ().height);


	dbg.setColor (getForeground ());
	paint (dbg);


	g.drawImage (dbImage, 0, 0, this);

    }
}

