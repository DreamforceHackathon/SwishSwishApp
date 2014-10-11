package com.partlycloudy.swishswish;

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

import java.io.*;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotifyServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(NotifyServlet.class.getSimpleName());

    private static final String[] CAT_UTTERANCES = {
            "<em class='green'>Purr...</em>",
            "<em class='red'>Hisss... scratch...</em>",
            "<em class='yellow'>Meow...</em>"
    };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    protected void execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try{
            System.out.println("Headers-----------");
            Enumeration headers = request.getHeaderNames();
            while (headers.hasMoreElements()){
                String header = "" + headers.nextElement();
                System.out.println(header + ":" + request.getHeader(header));
            }
            System.out.println("Remote address:"+request.getRemoteAddr());
            System.out.println("Remote host:"+request.getRemoteHost());
            System.out.println("Remote IP Address:" + request.getHeader("HTTP_X_FORWARDED_FOR"));

            System.out.println("Parameters-----------");
            Enumeration params = request.getParameterNames();
            while (params.hasMoreElements()){
                String param = "" + params.nextElement();
                System.out.println(param + ":" + request.getParameter(param));
            }


            String notificationString = getBody(request);

            System.out.println("Body----------------");
            System.out.println(notificationString);

            JsonFactory jsonFactory = new JacksonFactory();

            // If logging the payload is not as important, use
            // jacksonFactory.fromInputStream instead.
            Notification notification = jsonFactory.fromString(notificationString, Notification.class);

            LOG.info("Got a notification with ID: " + notification.getItemId());

            // Figure out the impacted user and get their credentials for API calls
            String userId = notification.getUserToken();
            Credential credential = AuthUtil.getCredential(userId);
            Mirror mirrorClient = MirrorClient.getMirror(credential);


            if (notification.getCollection().equals("locations")) {
                LOG.info("Notification of updated location");
                Mirror glass = MirrorClient.getMirror(credential);
                // item id is usually 'latest'
                Location location = glass.locations().get(notification.getItemId()).execute();

                LOG.info("New location is " + location.getLatitude() + ", " + location.getLongitude());
                MirrorClient.insertTimelineItem(
                        credential,
                        new TimelineItem()
                                .setText("Java Quick Start says you are now at " + location.getLatitude()
                                        + " by " + location.getLongitude())
                                .setNotification(new NotificationConfig().setLevel("DEFAULT")).setLocation(location)
                                .setMenuItems(Lists.newArrayList(new MenuItem().setAction("NAVIGATE"))));

                // This is a location notification. Ping the device with a timeline item
                // telling them where they are.
            } else if (notification.getCollection().equals("timeline")) {
                // Get the impacted timeline item
                TimelineItem timelineItem = mirrorClient.timeline().get(notification.getItemId()).execute();
                LOG.info("Notification impacted timeline item with ID: " + timelineItem.getId());

                // If it was a share, and contains a photo, update the photo's caption to
                // acknowledge that we got it.
                if (notification.getUserActions().contains(new UserAction().setType("SHARE"))
                        && timelineItem.getAttachments() != null && timelineItem.getAttachments().size() > 0) {
                    LOG.info("It was a share of a photo. Updating the caption on the photo.");

                    String caption = timelineItem.getText();
                    if (caption == null) {
                        caption = "";
                    }

                    // Create a new item with just the values that we want to patch.
                    TimelineItem itemPatch = new TimelineItem();
                    itemPatch.setText("Java Quick Start got your photo! " + caption);

                    // Patch the item. Notice that since we retrieved the entire item above
                    // in order to access the caption, we could have just changed the text
                    // in place and used the update method, but we wanted to illustrate the
                    // patch method here.
                    mirrorClient.timeline().patch(notification.getItemId(), itemPatch).execute();
                } else if (notification.getUserActions().contains(new UserAction().setType("REPLY"))) {
                    LOG.info("Reply action");

                } else if (notification.getUserActions().contains(new UserAction().setType("LAUNCH"))) {
                    LOG.info("It was a note taken with the 'take a note' voice command. Processing it.");

                    // Grab the spoken text from the timeline card and update the card with
                    // an HTML response (deleting the text as well).
                    String noteText = timelineItem.getText();
                    String utterance = CAT_UTTERANCES[new Random().nextInt(CAT_UTTERANCES.length)];

                    timelineItem.setText(null);
                    timelineItem.setHtml(makeHtmlForCard("<p class='text-auto-size'>"
                            + "Oh, did you say " + noteText + "? " + utterance + "</p>"));
                    timelineItem.setMenuItems(Lists.newArrayList(
                            new MenuItem().setAction("DELETE")));

                    mirrorClient.timeline().update(timelineItem.getId(), timelineItem).execute();
                } else {
                    LOG.warning("I don't know what to do with this notification, so I'm ignoring it.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.setContentType("text/html");
            Writer writer = response.getWriter();
            writer.append("OK");
            writer.close();
        }
    }


    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
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

    /**
     * Wraps some HTML content in article/section tags and adds a footer
     * identifying the card as originating from the Java Quick Start.
     *
     * @param content the HTML content to wrap
     * @return the wrapped HTML content
     */
    private static String makeHtmlForCard(String content) {
        return "<article class='auto-paginate'>" + content
                + "<footer><p>Java Quick Start</p></footer></article>";
    }
}
