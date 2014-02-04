package common.game.exceptions;

public class NoMoreTilesException extends Exception
{
	private static final long serialVersionUID = 1001678086375333717L;
	private final String message;
	
	public NoMoreTilesException(String msg)
	{
		message = msg;
	}
	
	public String getMessage()
	{
		return message;
	}
}
