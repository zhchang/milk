package milk.utils;

public class StringUtils
{
	public static boolean isEmptyOrNull(String text)
	{
		if(text!=null&&!text.equals(""))
		{
			return false;
		}
		return true;
	}
}
