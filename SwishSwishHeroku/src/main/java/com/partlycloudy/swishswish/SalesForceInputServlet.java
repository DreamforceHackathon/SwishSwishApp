package com.partlycloudy.swishswish;

import java.io.IOException;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.mirror.model.Command;
import com.google.api.services.mirror.model.Contact;
import com.google.api.services.mirror.model.MenuItem;
import com.google.api.services.mirror.model.MenuValue;
import com.google.api.services.mirror.model.NotificationConfig;
import com.google.api.services.mirror.model.TimelineItem;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList; 
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SalesForceInputServlet  extends HttpServlet {

	  @Override
	  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		  execute(req, res);
		  
	  }
	  
	  @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		  execute(req, res);
		  
	  }

	  protected void execute(HttpServletRequest req, HttpServletResponse res) throws IOException {
		  String message = req.getParameter("message");
		  
		  List<String> users = AuthUtil.getAllUserIds();
		  TimelineItem allUsersItem = new TimelineItem();
          allUsersItem.setText(message);
          BatchRequest batch = MirrorClient.getMirror(null).batch();
          BatchCallback callback = new BatchCallback();

          // TODO: add a picture of a cat
          for (String user : users) {
            Credential userCredential = AuthUtil.getCredential(user);
            MirrorClient.getMirror(userCredential).timeline().insert(allUsersItem)
                .queue(batch, callback);
          }

          batch.execute();
          res.getWriter().println("OK");
		  
	  }

	  private final class BatchCallback extends JsonBatchCallback<TimelineItem> {
		    private int success = 0;
		    private int failure = 0;

		    @Override
		    public void onSuccess(TimelineItem item, HttpHeaders headers) throws IOException {
		      ++success;
		    }

		    @Override
		    public void onFailure(GoogleJsonError error, HttpHeaders headers) throws IOException {
		      ++failure;
		    }
		  }
}
