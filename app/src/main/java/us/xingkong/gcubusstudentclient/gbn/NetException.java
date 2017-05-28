package us.xingkong.gcubusstudentclient.gbn;

/**
 * @author 饶翰新
 *
 */
public class NetException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public static final int ERR_NetWork = 1;
	
	private String Err_Info;
	private int Err_Code;
	
	public NetException(String Err_Info,int Err_Code)
	{
		this.Err_Info = Err_Info;
		this.Err_Code = Err_Code;
	}
	
	public String getErroInfo()
	{
		return Err_Info;
	}

	public int getErroCode()
	{
		return Err_Code;
	}
	
	@Override
	public String toString() {
		return Err_Info + "\ncode=" + Err_Code;
	}
}
