package milk.ui2;

import milk.implement.mk.MRect;

public interface MilkTiledLayer {

	void setCell(int col, int row, int tileIndex) ;

	int getCell(int col, int row) ;

	int createAnimatedTile(int tileIndex);

	int getAnimatedTile(int animatedIndex);

	void setAnimatedTile(int animatedIndex, int tileIndex) ;

	void setPosition(int x, int y);

	void paint(MilkGraphics g,MRect viewPort);
	
}
