package com.partlycloudy.swishswish;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Location;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.Notification;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.api.services.mirror.model.UserAction;
import com.google.common.collect.Lists;

public class Notify2Servlet extends HttpServlet {

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		execute(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		execute(request, response);
	}

	private static String accessToken;

	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			System.out.println("Headers-----------");
			Enumeration headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String header = "" + headers.nextElement();
				System.out.println(header + ":" + request.getHeader(header));
			}
			System.out.println("Remote address:" + request.getRemoteAddr());
			System.out.println("Remote host:" + request.getRemoteHost());
			System.out.println("Remote IP Address:"
					+ request.getHeader("HTTP_X_FORWARDED_FOR"));

			System.out.println("Parameters-----------");
			Enumeration params = request.getParameterNames();
			while (params.hasMoreElements()) {
				String param = "" + params.nextElement();
				System.out.println(param + ":" + request.getParameter(param));
			}

			// --form
			// client_id=3MVG9xOCXq4ID1uEhB4bY6EMA35p31pb76VNjO_2lEqnD8P05DDyfPJgcqtSbiv_DqXzK5YeTEQVye2wAncwM
			// --form client_secret=493891164069775110 --form
			// grant_type=password --form username=pavlonator@partlycloudy.com
			// --form password=puxnY3321_sf
			// https://login.salesforce.com/services/oauth2/token

			// url
			// https://login.salesforce.com/services/oauth2/token?client_id=3MVG9xOCXq4ID1uEhB4bY6EMA35p31pb76VNjO_2lEqnD8P05DDyfPJgcqtSbiv_DqXzK5YeTEQVye2wAncwM&client_secret=493891164069775110&grant_type=password&username=pavlonator@partlycloudy.com&password=puxnY3321_sf
			if (accessToken == null) {
				String urlParameters = "client_id=3MVG9xOCXq4ID1uEhB4bY6EMA35p31pb76VNjO_2lEqnD8P05DDyfPJgcqtSbiv_DqXzK5YeTEQVye2wAncwM&client_secret=493891164069775110&grant_type=password&username=pavlonator@partlycloudy.com&password=puxnY3321_sf";
				String requestUrl = "https://login.salesforce.com/services/oauth2/token";
				String contentType = "application/x-www-form-urlencoded";
				String respJson = interract(urlParameters, requestUrl,
						contentType);
				response.getWriter().write("respJson:"+respJson);
				int begin = respJson.indexOf("access_token") + 15;
				int end = respJson.indexOf('\"', begin);
				accessToken = respJson.substring(begin, end);
			}
			System.out.println(accessToken);
			response.getWriter().write("accessToken:"+accessToken + " ");
			
			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.setContentType("text/html");
			Writer writer = response.getWriter();
			writer.append("OK");
			writer.close();
		}
	}

	private String interract(String urlParameters, String requestUrl,
			String contentType) throws MalformedURLException, IOException,
			ProtocolException {
		URL url = new URL(requestUrl);
		HttpURLConnection connection = (HttpURLConnection) url
				.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				contentType);
		connection.setRequestProperty("charset", "utf-8");
		connection.setRequestProperty("Content-Length",
				"" + Integer.toString(urlParameters.getBytes().length));
		connection.setUseCaches(false);

		DataOutputStream wr = new DataOutputStream(
				connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		String respJson = getBody(connection.getInputStream());
		connection.disconnect();
		return respJson;
	}

	public static String getBody(InputStream inputStream) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

}
