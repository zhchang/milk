package milk.ui;

import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;


public class MilkSpriteImpl extends Layer implements MilkSprite
{

		
	private MilkImage image;
	private int refPixelX;
	private int refPixelY;
	private int frameSequenceIndex;
	private int[] frameSequence;
	private int transform;

	private int collisionX;
	private int collisionY;
	private int collisionWidth;
	private int collisionHeight;
	private int transformedCollisionX;
	private int transformedCollisionY;
	private int transformedCollisionWidth;
	private int transformedCollisionHeight;

	private int frameHeight;
	private int frameWidth;
	private int rawFrameCount;
	private boolean isSingleFrame;
	private int transformedRefX;
	private int transformedRefY;
	private int numberOfColumns;
	private int column;
	private int row;

	public MilkSpriteImpl(MilkImage image) {
		setImage(image, image.getWidth(), image.getHeight());
	}

	public MilkSpriteImpl(MilkImage image, int frameWidth, int frameHeight) {
		setImage(image, frameWidth, frameHeight);
	}

	public MilkSpriteImpl( MilkSpriteImpl s)
	{
			this.image = s.image;
			this.frameWidth = s.frameWidth;
			this.frameHeight = s.frameHeight;
			this.numberOfColumns = s.numberOfColumns;
			this.width = s.width;
			this.height = s.height;
			this.xPosition = s.xPosition;
			this.yPosition = s.yPosition;
			this.frameSequenceIndex = s.frameSequenceIndex;
			if (s.frameSequence != null) {
				this.frameSequence = new int[s.frameSequence.length];
				System.arraycopy(s.frameSequence, 0, this.frameSequence, 0, this.frameSequence.length);
			}
			this.refPixelX = s.refPixelX;
			this.refPixelY = s.refPixelY;
			this.transformedRefX = s.transformedRefX;
			this.transformedRefY = s.transformedRefY;
			this.transform = s.transform;
			this.collisionX = s.collisionX;
			this.collisionY = s.collisionY;
			this.collisionWidth = s.collisionWidth;
			this.collisionHeight = s.collisionHeight;
			this.transformedCollisionX = s.transformedCollisionX;
			this.transformedCollisionY = s.transformedCollisionY;
			this.transformedCollisionWidth = s.transformedCollisionWidth;
			this.transformedCollisionHeight = s.transformedCollisionHeight;
			this.isSingleFrame = s.isSingleFrame;
		
	}

	public void defineReferencePixel(int refX, int refY)
	{
		this.refPixelX = refX;
		this.refPixelY = refY;
		applyTransform();
	}

	/**
	 * Sets this Sprite's position such that its reference pixel is located
	 * at (x,y) in the painter's coordinate system.
	 * 
	 * @param x the horizontal location at which to place the reference pixel
	 * @param y the vertical location at which to place the reference pixel
	 * @see #defineReferencePixel(int, int)
	 * @see #getRefPixelX()
	 * @see #getRefPixelY()
	 */
	public void setRefPixelPosition(int x, int y)
	{
		
			this.xPosition = x - this.transformedRefX;
			this.yPosition = y - this.transformedRefY;
	
	}

	/**
	 * Gets the horizontal position of this Sprite's reference pixel
	 * in the painter's coordinate system.
	 * 
	 * @return the horizontal location of the reference pixel
	 * @see #defineReferencePixel(int, int)
	 * @see #setRefPixelPosition(int, int)
	 * @see #getRefPixelY()
	 */
	public int getRefPixelX()
	{
		return this.transformedRefX + this.xPosition;
	}

	/**
	 * Gets the vertical position of this Sprite's reference pixel
	 * in the painter's coordinate system.
	 * 
	 * @return the vertical location of the reference pixel
	 * @see #defineReferencePixel(int, int)
	 * @see #setRefPixelPosition(int, int)
	 * @see #getRefPixelX()
	 */
	public int getRefPixelY()
	{
		return this.transformedRefY + this.yPosition;
	}

	/**
	 * Selects the current frame in the frame sequence.  
	 * <p>
	 * The current frame is rendered when 
	 * <A HREF="../../../../de/enough/polish/ui/game/Sprite.html#paint(javax.microedition.lcdui.Graphics)"><CODE>paint(Graphics)</CODE></A>
	 * is called.
	 * <p>
	 * The index provided refers to the desired entry in the frame sequence,
	 * not the index of the actual frame itself.
	 * 
	 * @param sequenceIndex the index of of the desired entry in the frame  sequence
	 * @throws IndexOutOfBoundsException if frameIndex is less than 0
	 *									 or if frameIndex is equal to or greater than the length of the current frame sequence (or the number of raw frames for the default sequence)
	 * @see #setFrameSequence(int[])
	 * @see #getFrame()
	 */
	public void setFrame(int sequenceIndex)
	{
		this.frameSequenceIndex = sequenceIndex;
		updateFrame();
	}
	
	/**
	 * Gets the current index in the frame sequence.
	 * The index returned refers to the current entry in the frame sequence,
	 * not the index of the actual frame that is displayed.
	 * 
	 * @return the current index in the frame sequence
	 * @see #setFrameSequence(int[])
	 * @see #setFrame(int)
	 */
	public final int getFrame()
	{
		return this.frameSequenceIndex;
	}
	
	/**
	 * Gets the number of raw frames for this Sprite.  The value returned
	 * reflects the number of frames; it does not reflect the length of the
	 * Sprite's frame sequence.  However, these two values will be the same
	 * if the default frame sequence is used.
	 * 
	 * @return the number of raw frames for this Sprite
	 * @see #getFrameSequenceLength()
	 */
	public int getRawFrameCount()
	{
		return this.rawFrameCount;
	}
	
	/**
	 * Gets the number of elements in the frame sequence.  The value returned
	 * reflects the length of the Sprite's frame sequence; it does not reflect
	 * the number of raw frames.  However, these two values will be the same
	 * if the default frame sequence is used.
	 * 
	 * @return the number of elements in this Sprite's frame sequence
	 * @see #getRawFrameCount()
	 */
	public int getFrameSequenceLength()
	{
		if (this.frameSequence == null)
			return this.rawFrameCount;
		return this.frameSequence.length;
	}

	/**
	 * Selects the next frame in the frame sequence.
	 * 
	 * The frame sequence is considered to be circular, i.e. if
	 * <A HREF="../../../../javax/microedition/lcdui/game/Sprite.html#nextFrame()"><CODE>nextFrame()</CODE></A> is called when at the end of the sequence,
	 * this method will advance to the first entry in the sequence.
	 * 
	 * @see #setFrameSequence(int[])
	 * @see #prevFrame()
	 */
	public void nextFrame()
	{
		this.frameSequenceIndex++;
		if (this.frameSequenceIndex >= this.getFrameSequenceLength()) {
			this.frameSequenceIndex = 0;
		}
		updateFrame();
	}
	
	/**
	 * Selects the previous frame in the frame sequence.  <p>
	 * 
	 * The frame sequence is considered to be circular, i.e. if
	 * <A HREF="../../../../javax/microedition/lcdui/game/Sprite.html#prevFrame()"><CODE>prevFrame()</CODE></A> is called when at the start of the sequence,
	 * this method will advance to the last entry in the sequence.
	 * 
	 * @see #setFrameSequence(int[])
	 * @see #nextFrame()
	 */
	public void prevFrame()
	{
		this.frameSequenceIndex--;
		if (this.frameSequenceIndex < 0 ) {
			this.frameSequenceIndex = this.getFrameSequenceLength() - 1;
		}
		updateFrame();
	}
	
	private void updateFrame() {
		int frameIndex = (this.frameSequence == null) ? this.frameSequenceIndex : this.frameSequence[this.frameSequenceIndex];
		int c = frameIndex % this.numberOfColumns;
		int r = frameIndex / this.numberOfColumns;
			int numberOfRows = this.rawFrameCount / this.numberOfColumns;
			
			this.column = c;
			this.row = r;
			switch (this.transform ) {
				case TRANS_NONE:
					this.column = c;
					this.row = r;
					break;
				case TRANS_MIRROR_ROT180:
					this.column = c;
					this.row = (numberOfRows-1) - r;
					break;
				case TRANS_MIRROR:
					this.column = (this.numberOfColumns -1) - c;
					this.row = r;
					break;
				case TRANS_ROT180:
					this.column = (this.numberOfColumns -1) - c;
					this.row = (numberOfRows-1) - r;
					break;
				case TRANS_MIRROR_ROT270:
					this.column = c;
					this.row = r;
					break;
				case TRANS_ROT90:
					this.column = (numberOfRows -1) - r;
					this.row = c;
					break;
				case TRANS_ROT270:
					this.column = r;
					this.row = (this.numberOfColumns -1) - c;
					break;
				case TRANS_MIRROR_ROT90:
					this.row = (numberOfRows-1) - r;
					this.row = (this.numberOfColumns -1) - c;
					break;
			}
		
	}
	
	public final void paint( MilkGraphics g)
	{
		if (this.isSingleFrame && this.transform == 0) {
			g.drawImage( this.image, this.xPosition, this.yPosition, MilkGraphics.TOP | MilkGraphics.LEFT );
			return;
		}
			if (this.rawFrameCount == 1) {
				g.drawImage( this.image, this.xPosition, this.yPosition, MilkGraphics.TOP | MilkGraphics.LEFT );							
			} else { 
				// there are several frames contained in the base-image:
				int x = this.xPosition;
				int y = this.yPosition;
				//System.out.print("painting sprite at " + x + ", " + y );
				//save the current clip position:
				int clipX = g.getClipX();
				int clipY = g.getClipY();
				int clipWidth = g.getClipWidth();
				int clipHeight = g.getClipHeight();
				g.clipRect( x, y, this.width, this.height );
				x -= this.column * this.width;
				y -= this.row * this.height;
			
				g.drawImage( this.image, x, y, MilkGraphics.TOP | MilkGraphics.LEFT );			
				
				// reset original clip:
				g.setClip( clipX, clipY, clipWidth, clipHeight );
			}
		
	}

	public void setFrameSequence(int[] sequence)
	{
		int frameIndex = 0;
		this.frameSequence = null;

		if (sequence != null) {
			int[] newSequence = new int[ sequence.length ];
			System.arraycopy( sequence, 0, newSequence, 0, sequence.length );
			this.frameSequence = newSequence;
			frameIndex = this.frameSequence[ 0 ];
		}

		this.frameSequenceIndex = 0;
		this.column = frameIndex % this.numberOfColumns;
		this.row = frameIndex / this.numberOfColumns;
	}
	
//	public final int getWidth() {
//		return this.frameWidth;
//	}
//
//	public final int getHeight() {
//		return this.frameHeight;
//	}
	
	public void setImage( MilkImage image, int frameWidth, int frameHeight)
	{
		this.image = image;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.width = frameWidth;
		this.height = frameHeight;
		
		this.numberOfColumns = image.getWidth() / frameWidth;
		int rows = image.getHeight() / frameHeight;

		int oldRawFrameCount = this.rawFrameCount;
		this.rawFrameCount = this.numberOfColumns * rows;
		this.isSingleFrame = (this.rawFrameCount == 1);
		if (this.rawFrameCount < oldRawFrameCount) {
			this.frameSequenceIndex = 0;
			// set default frame sequence:
			this.frameSequence = null;
			this.column = 0;
			this.row = 0;
		} else {
			int frameIndex = (this.frameSequence == null) ? this.frameSequenceIndex : this.frameSequence[this.frameSequenceIndex];
			this.column = frameIndex % this.numberOfColumns;
			this.column = frameIndex / this.numberOfColumns;
		}

		this.collisionX = 0;
		this.collisionY = 0;
		this.collisionWidth = frameWidth;
		this.collisionHeight = frameHeight;

		// computes tranformed* values and reposition the sprite.
		int oldRefX = this.transformedRefX, oldRefY = this.transformedRefY;
		applyTransform();
		this.xPosition += oldRefX - this.transformedRefX;
		this.yPosition += oldRefY - this.transformedRefY;

	}
	
	public void defineCollisionRectangle(int leftX, int topY, int cWidth, int cHeight)
	{
		this.collisionX = leftX;
		this.collisionY = topY;
		this.collisionWidth = cWidth;
		this.collisionHeight = cHeight;
		this.applyTransform();
	}

	private void applyTransform() {
		int refX, refY, colX, colY;
		
		// set the horizontal values:
		if ((this.transform & 2) == 0) {
			// Either TRANS_NONE, TRANS_MIRROR_ROT180, TRANS_MIRROR_ROT270 or TRANS_ROT90
			refX = this.refPixelX;
			colX = this.collisionX;
		} else {
			// Either TRANS_MIRROR, TRANS_ROT180, TRANS_ROT270 or TRANS_MIRROR_ROT90
			refX = this.frameWidth - this.refPixelX;
			colX = this.frameWidth - (this.collisionX + this.collisionWidth);
		}
		
		// set the vertical values:
		if ((this.transform & 1) == 0) {
			// Either TRANS_NONE, TRANS_MIRROR, TRANS_MIRROR_ROT270 or TRANS_ROT270
			refY = this.refPixelY;
			colY = this.collisionY;
		} else {
			// Either TRANS_MIRROR_ROT180, TRANS_ROT180, TRANS_ROT90 or TRANS_MIRROR_ROT90
			refY = this.frameHeight - this.refPixelY;
			colY = this.frameHeight - (this.collisionY + this.collisionHeight);
		}

		if ((this.transform & 4) == 0) {
			// Either TRANS_NONE, TRANS_MIRROR_ROT180, TRANS_MIRROR or TRANS_ROT180
			this.width = this.frameWidth;
			this.height = this.frameHeight;
			this.transformedRefX = refX;
			this.transformedRefY = refY;
			this.transformedCollisionX = colX;
			this.transformedCollisionY = colY;
			this.transformedCollisionWidth = this.collisionWidth;
			this.transformedCollisionHeight = this.collisionHeight;
		} else {
			// Either TRANS_MIRROR_ROT270, TRANS_ROT90, TRANS_ROT270 or TRANS_MIRROR_ROT90
			// the vertical and horizontal values needs to be switched:
			this.width = this.frameHeight;
			this.height = this.frameWidth;
			this.transformedRefX = refY;
			this.transformedRefY = refX;
			this.transformedCollisionX = colY;
			this.transformedCollisionY = colX;
			this.transformedCollisionWidth = this.collisionHeight;
			this.transformedCollisionHeight = this.collisionWidth;
		}
	}

	public void setTransform(int transform) {
		this.transform = transform;
		int oldRefX = this.transformedRefX, oldRefY = this.transformedRefY;
		applyTransform();
		this.xPosition += oldRefX - this.transformedRefX;
		this.yPosition += oldRefY - this.transformedRefY;
		if (this.rawFrameCount > 1) {
			updateFrame();
		}

	}


	public final boolean collidesWith( MilkSprite s1, boolean pixelLevel)
	{
		MilkSpriteImpl s=(MilkSpriteImpl)s1;
		if (!(this.isVisible && s.isVisible)) {
			return false;
		}
		int enemyX = s.xPosition + s.transformedCollisionX;
		int enemyY = s.yPosition + s.transformedCollisionY; 
		return collidesWith( enemyX, enemyY,
				s.transformedCollisionWidth,
				s.transformedCollisionHeight
				);
	}
	
	

	public final boolean collidesWith( MilkImage img, int leftX, int topY, boolean pixelLevel)
	{
		return collidesWith( leftX, topY, img.getWidth(), img.getHeight() );
	}
	
	private boolean collidesWith( int enemyX, int enemyY, int enemyWidth, int enemyHeight ) {
		int cXStart = this.xPosition + this.transformedCollisionX;
		int cXEnd = cXStart + this.transformedCollisionWidth;
		int cYStart = this.yPosition + this.transformedCollisionY;
		int cYEnd = cYStart + this.transformedCollisionHeight;
		if (cYEnd <= enemyY
				|| cYStart >= enemyY + enemyHeight
				|| cXEnd <= enemyX
				|| cXStart >= enemyX + enemyWidth ) 
		{
			return false;
		}
		return true;
	}


}
