package us.xingkong.gcubusstudentclient.gbn;

/**
 * @author 饶翰新
 *
 */
public interface NetHandler {
	public void sendMessage(int what, Object obj);
	public void handlerMessage(int what, Object obj);
}
