package re.dbn.ked.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.json.*;

public class Matrix
{
	private String access_token = "";
	private String server = "";
	private String user_id = "";
	private String room_id = "";

	private String room_history_token = "";
	private String room_filters = "";

	private HashMap<String, String> displayname_by_matrixid = new HashMap<String, String>();

	HttpsURLConnection url_conn;

	public Matrix(String server, String user_id)
	{
    	this.user_id = user_id;
		this.server = server;
	}

	public boolean login(String password)
	{
        try
        {
        	JSONObject login_payload = new JSONObject();
        	login_payload.put("type", "m.login.password");
        	login_payload.put("user", user_id);
        	login_payload.put("password", password);

        	JSONObject login_response = new JSONObject(request("POST", "/_matrix/client/api/v1/login", login_payload));

        	access_token = login_response.getString("access_token");
		}
        catch (Exception e)
        {
			e.printStackTrace();
    		return false;
		}

		return true;
	}

	public void setAccessToken(String token)
	{
		this.access_token = token;
	}

	public String getAccessToken()
	{
		return access_token;
	}

	public boolean joinRoom(String room_id)
	{
		if (user_id.equals(""))
			return false;

		this.room_id = room_id;

		// Filters: Only fetch messages from the log room and ignore messages sent by the bot
		try
		{
			room_filters = URLEncoder.encode(new JSONObject(
					"{\"room\": {\"rooms\": [\""+room_id+"\"], "
					+ "\"timeline\": {\"types\": [\"m.room.message\"], "
					+ "\"not_senders\": [\""+user_id+"\"]}}}"
			).toString(), "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

		// Send first sync (to populate room_history_token and ignore any messages sent before server start)
		try
		{
			getLastMessages();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	public boolean sendMessage(String message)
	{
		if (room_id.equals("") || access_token.equals(""))
			return false;

    	JSONObject login_payload = new JSONObject();
    	login_payload.put("msgtype", "m.text");
    	login_payload.put("body", message);

    	try {
			request("POST", "/_matrix/client/api/v1/rooms/"+room_id+"/send/m.room.message", login_payload);
		}
    	catch (Exception e)
    	{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public JSONArray getLastMessages() throws Exception
	{
		JSONArray result = new JSONArray();

		JSONObject raw_result = new JSONObject(request("GET", "/_matrix/client/r0/sync?filter="+this.room_filters
			+ (room_history_token.isEmpty() ? "" : "&since=" + this.room_history_token)
			+ "&access_token="+this.access_token
		, new JSONObject(), false));

		this.room_history_token = raw_result.getString("next_batch");

		JSONObject room_data = raw_result.getJSONObject("rooms").getJSONObject("join");

		if (room_data.has(room_id))
		{
			result = room_data
				.getJSONObject(room_id)
				.getJSONObject("timeline")
				.getJSONArray("events")
			;

		}

		return result;
	}

	public String getDisplayName(String matrixid)
	{
		return getDisplayName(matrixid, false);
	}

	public String getDisplayName(String matrixid, boolean clear_cache)
	{
		if (clear_cache)
			displayname_by_matrixid.clear();

		if (!displayname_by_matrixid.containsKey(matrixid))
		{
			try
			{
				JSONObject response = new JSONObject(get("/_matrix/client/r0/profile/"+matrixid+"/displayname"));
				displayname_by_matrixid.put(matrixid, response.getString("displayname"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				displayname_by_matrixid.put(matrixid, matrixid);
			}
		}

		return displayname_by_matrixid.get(matrixid);
	}

	public boolean isValid()
	{
		try {
			get("/_matrix/client/api/v1/rooms/"+room_id+"/state");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	protected String get(String url) throws Exception
	{
		return request("GET", url, new JSONObject());
	}

	protected String request(String proto, String url, JSONObject payload) throws Exception
	{
		return request(proto, url, payload, true);
	}

	protected String request(String proto, String url, JSONObject payload, Boolean addBearer) throws Exception
	{
		String strpayload = payload.toString();
	    StringBuilder response = new StringBuilder();

		URL url_conn = new URL (server+url);
		HttpURLConnection con = (HttpURLConnection)url_conn.openConnection();
		con.setRequestMethod(proto);

		if (addBearer)
			con.setRequestProperty("Authorization", "Bearer " + access_token);

		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);

		if (!proto.equals("GET"))
		{
			try(OutputStream os = con.getOutputStream())
			{
			    byte[] input = strpayload.getBytes("utf-8");
			    os.write(input, 0, input.length);
			}
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8")))
		{
		    String responseLine = null;
		    while ((responseLine = br.readLine()) != null)
		    {
		        response.append(responseLine.trim());
		    }
		}

		return response.toString();
	}
}
