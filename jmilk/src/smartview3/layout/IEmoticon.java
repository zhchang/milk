package smartview3.layout;

public interface IEmoticon {

	// whether matches second charater onwards
	public abstract int matchTail(String content, int start);

	public abstract String getTag();

	public abstract int getId();

	public abstract int getHeight();

	public abstract int getWidth();

	public abstract int getXSpacing();

}