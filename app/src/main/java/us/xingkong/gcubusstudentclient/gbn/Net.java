package us.xingkong.gcubusstudentclient.gbn;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * @author 饶翰新
 *
 */
public class Net {

	/**
	 * 测试服务器地址
	 */
	public static final String SERVER_TEST = "http://dustlight.cn/gcubus/index.php";

	private final String API_COMMAND = "com";
	private final String API_Longitude = "lon";
	private final String API_Latitude = "lat";
	private final String API_Code = "cod";
	private final String API_BusID = "bid";
	private final String API_Username = "u";
	private final String API_Password = "p";

	private final String COMMAND_Login = "lgin";
	private final String COMMAND_Logout = "lgot";
	private final String COMMAND_UpdatePoint = "up";
	private final String COMMAND_GetPoint = "gp";
	private final String COMMAND_GetVaildbusID = "gvid";

	private String SERVER;
	private String code;
	private Boolean flag;

	
	/**
	 * 初始化
	 * @param Server 服务器地址，Net.SERVER_TEST 为测试地址
	 */
	public Net(String Server) {
		setServer(Server);
		this.code = null;
		UNFLAG();
	}

	/**
	 * 设置服务器地址
	 * @param Server 服务器地址，Net.SERVER_TEST 为测试地址
	 */
	public void setServer(String Server) {
		this.SERVER = Server;
	}

	/**
	 * 获取服务器地址
	 * @return 服务器地址
	 */
	public String getServer() {
		return this.SERVER;
	}

	private String parseurl(String key, String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(key, "utf-8") + "=" + URLEncoder.encode(value, "utf-8");
	}

	private String connect(String url) throws IOException {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		InputStream in = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuffer buffer = new StringBuffer();
		int b;
		while ((b = reader.read()) != -1) {
			buffer.append((char) b);
		}

		return buffer.toString();
	}

	private void FLAG() {
		flag = true;
	}

	private void UNFLAG() {
		flag = false;
	}

	private Boolean IsFLAG() {
		return flag;
	}

	/**
	 * 司机登陆
	 * @param username 司机用户名
	 * @param password 司机用户密码
	 * @param Listener 回调监听器，可null
	 * @return 若当前无任务则执行此方法，标记任务状态并且返回真，有正在执行的任务则不执行此方法且返回假。
	 */
	public Boolean Login(final String username,final String password,final NetListener Listener) {
		if (IsFLAG())
			return false;

		FLAG();
		Thread nt = new Thread() {
			@Override
			public void run() {
				super.run();
				NetException ne = null;
				Object data = null;
				try {

					String result = connect(getServer() + "?" + parseurl(API_COMMAND, COMMAND_Login) + "&"
							+ parseurl(API_Username, username) + "&" + parseurl(API_Password, password));

					if (result.length() <= 0) {
						if (ne == null) {
							ne = new NetException("登陆失败,无返回值", 1);
						}
					} else {
						try {
							int err_code = Integer.valueOf(result.toString());
							if (ne == null) {
								ne = new NetException("登陆失败", err_code);
							}
						} catch (NumberFormatException e) {
							code = result;
							data = "SUCCESS";
						}
					}

				} catch (IOException e) {
					if (ne == null) {
						ne = new NetException("连接登陆服务器失败\n" + e.toString(), NetException.ERR_NetWork);
					}
				}
				// UNFLAG();
				UNFLAG();
				if (Listener != null)
					Listener.done(data, ne);

			}
		};
		nt.start();
		return true;
	}

	/**
	 * 上传司机位置
	 * @param longitude 经度
	 * @param latitude 纬度
	 * @param Listener 回调监听器，可null
	 * @return 若当前无任务则执行此方法，标记任务状态并且返回真，有正在执行的任务则不执行此方法且返回假。
	 */
	public Boolean UpdatePoint(final double longitude,final double latitude,final NetListener Listener) {
		if (IsFLAG())
			return false;

		FLAG();
		Thread nt = new Thread() {
			@Override
			public void run() {
				super.run();
				NetException ne = null;
				Object data = null;
				try {

					String result = connect(getServer() + "?" + parseurl(API_COMMAND, COMMAND_UpdatePoint) + "&"
							+ parseurl(API_Longitude, String.valueOf(longitude)) + "&"
							+ parseurl(API_Latitude, String.valueOf(latitude)) + "&" + parseurl(API_Code, code));

					if (result.length() <= 0) {
						if (ne == null) {
							ne = new NetException("上传失败,无返回值", 1);
						}

					} else {
						try {
							int err_code = Integer.valueOf(result);
							if (ne == null) {
								ne = new NetException("上传失败", err_code);
							}
						} catch (NumberFormatException e) {
							code = result;
							data = "SUCCESS";
						}
					}

				} catch (IOException e) {
					if (ne == null) {
						ne = new NetException("连接上传服务器失败\n" + e.toString(), NetException.ERR_NetWork);
					}
				}
				UNFLAG();
				if (Listener != null)
					Listener.done(data, ne);
			}
		};
		nt.start();
		return true;
	}

	/**
	 * 获取校巴位置
	 * @param busID 校巴ID
	 * @param Listener 回调监听器，可null
	 * @return 若当前无任务则执行此方法，标记任务状态并且返回真，有正在执行的任务则不执行此方法且返回假。
	 */
	public Boolean GetPoint(final int busID,final NetListener Listener) {
		if (IsFLAG())
			return false;

		FLAG();
		Thread nt = new Thread() {
			@Override
			public void run() {
				super.run();
				NetException ne = null;
				Object data = null;
				try {
					String result = connect(getServer() + "?" + parseurl(API_COMMAND, COMMAND_GetPoint) + "&"
							+ parseurl(API_BusID, String.valueOf(busID)));

					if (result.length() <= 0) {
						if (ne == null) {
							ne = new NetException("获取失败,无返回值", 1);
						}

					} else {
						try {
							int err_code = Integer.valueOf(result);
							if (ne == null) {
								ne = new NetException("获取失败", err_code);
							}
						} catch (NumberFormatException e) {

							data = result;
						}
					}

				} catch (IOException e) {
					if (ne == null) {
						ne = new NetException("连接服务器失败\n" + e.toString(), NetException.ERR_NetWork);
					}
				}
				UNFLAG();
				if (Listener != null)
					Listener.done(data, ne);
			}
		};
		nt.start();
		return true;
	}

	/**
	 * 获取有效的校巴ID
	 * @param Listener 回调监听器，可null
	 * @return 若当前无任务则执行此方法，标记任务状态并且返回真，有正在执行的任务则不执行此方法且返回假。
	 */
	public Boolean GetVaildbusID(final NetListener Listener) {
		if (IsFLAG())
			return false;

		FLAG();
		Thread nt = new Thread() {
			@Override
			public void run() {
				super.run();
				NetException ne = null;
				Object data = null;
				try {
					String result = connect(getServer() + "?" + parseurl(API_COMMAND, COMMAND_GetVaildbusID));

					if (result.length() <= 0) {
						if (ne == null) {
							ne = new NetException("获取失败,无返回值", 1);
						}

					} else {
						try {
							int err_code = Integer.valueOf(result);
							if (ne == null) {
								ne = new NetException("获取失败", err_code);
							}
						} catch (NumberFormatException e) {

							data = result;
						}
					}

				} catch (IOException e) {
					if (ne == null) {
						ne = new NetException("连接服务器失败\n" + e.toString(), NetException.ERR_NetWork);
					}
				}
				UNFLAG();
				if (Listener != null)
					Listener.done(data, ne);
			}
		};
		nt.start();
		return true;
	}

	/**
	 * 司机登出
	 * @param Listener 回调监听器，可null
	 * @return 若当前无任务则执行此方法，标记任务状态并且返回真，有正在执行的任务则不执行此方法且返回假。
	 */
	public Boolean Logout( final NetListener Listener) {
		if (IsFLAG())
			return false;

		FLAG();
		Thread nt = new Thread() {
			@Override
			public void run() {
				super.run();
				NetException ne = null;
				Object data = null;
				try {
					String result = connect(getServer() + "?" + parseurl(API_COMMAND, COMMAND_Logout) + "&" + parseurl(API_Code, code));

					try {
						int err_code = Integer.valueOf(result);
						if (ne == null) {
							ne = new NetException("登出失败", err_code);
						}
					} catch (NumberFormatException e) {

						data = result;
					}

				} catch (IOException e) {
					if (ne == null) {
						ne = new NetException("连接服务器失败\n" + e.toString(), NetException.ERR_NetWork);
					}
				}
				
				UNFLAG();
				if (Listener != null)
					Listener.done(data, ne);
			}
		};
		nt.start();
		return true;
	}

	public static int[] getIDs(String jsonData) throws JSONException {
		JSONArray jarray = new JSONArray(jsonData);
		int[] ids =  new int[jarray.length()];
		for(int i = 0;i < jarray.length();i++)
		{
			ids[i] =  Integer.valueOf(jarray.get(i).toString());
		}
		return ids;
	}


}
