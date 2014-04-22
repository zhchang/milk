package milk.ui2;

public interface MilkGraphics {

	int LEFT = 4;// Graphics.LEFT;
	int RIGHT = 8;// Graphics.RIGHT;
	int TOP = 16;// Graphics.TOP;
	int BOTTOM = 32;// Graphics.BOTTOM;
	int HCENTER = 1;// Graphics.HCENTER;
	int VCENTER = 2;// Graphics.VCENTER;

	MilkFont getFont();

	void setFont(MilkFont font);

	void setColor(int color);

	void setClip(int x, int y, int w, int h);

	void drawLine(int x, int y, int x1, int y1);

	void drawString(String input, int x, int y, int anchor);

	void drawSubstring(String input, int offset, int length, int x, int y,
			int anchor);

	int getClipWidth();

	int getClipHeight();

	int getClipX();

	int getClipY();

	void drawRect(int x, int y, int w, int h);

	void fillRect(int x, int y, int w, int h);

	void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle);

	void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle);

	void drawRoundRect(int x, int y, int w, int h, int aw, int ah);

	void fillRoundRect(int x, int y, int w, int h, int aw, int ah);

	void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3);

	void clipRect(int x, int y, int w, int h);

	void drawImage(MilkImage image, int x, int y, int anchor);

	void drawImage(MilkImage image, int x, int y, int anchor, int alpha);

	void drawRGB(int[] rgbData, int offset, int scanlength, int x, int y,
			int width, int height, boolean processAlpha);

	void translate(int x, int y);

	int getTranslateX();

	int getTranslateY();

	void resize(int ratio);// percent of resize ratio

	void save();

	void restore(); // recert to orginal ratio
}
