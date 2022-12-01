package net.sourceforge.ganttproject.export;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventAttendee;

import net.sourceforge.ganttproject.task.ResourceAssignment;

import java.util.TimeZone;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/* Class that implements the functionalities connected with the Google Calendar API */
public class GoogleCalendar {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "./tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Calendar service;

    public GoogleCalendar() throws IOException, GeneralSecurityException {

    }

    // Build a new authorized API client service.
    private static Calendar createService() throws IOException, GeneralSecurityException{
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Calendar service =
                new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        return service;
    }

    /**
     *
     * @param numbr - the number to be added a zero at the left if its less than 10.
     * @return the modified String.
     */
    private static String addZero(String numbr){
        return Integer.valueOf(numbr) < 10 ? "0" + numbr : numbr;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        System.out.println(GoogleCalendar.class.getResource("/").getPath());
        InputStream in = GoogleCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    /**
     *
     * @param name - name of the task.
     * @param beginDate - begin date of the task.
     * @param endDate - end date of the task.
     * @param cost - cost of the task.
     * @param resourceAssignments - the resources assigned to this task.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void createEvent(String name, String beginDate, String endDate, String cost, ResourceAssignment[] resourceAssignments) throws IOException, GeneralSecurityException {


        //List of the event attendees to be added to the event.
        List<EventAttendee> attendees = new ArrayList<>();

        //Add resources as attendees.
        for (int i = 0; i < resourceAssignments.length; i++) {
            EventAttendee eventAttendee = new EventAttendee();
            eventAttendee.setEmail(resourceAssignments[i].getResource().getMail());
            eventAttendee.setDisplayName(resourceAssignments[i].getResource().getName());
            attendees.add(eventAttendee);
        }

        // Build a new authorized API client service.
        service = createService();

        Event event = new Event()
                .setSummary(name)
                .setDescription("Cost: " + cost);

        String[] beginParts = beginDate.split("/");
        String beginMonth = addZero(beginParts[0]);
        String beginDay = addZero(beginParts[1]);
        String beginYear = "20" + addZero(beginParts[2]);

        //Set start time in the first day at midnight.
        DateTime startDateTime = new DateTime(beginYear+"-"+beginMonth+"-"+beginDay+"T00:00:00.00Z");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(TimeZone.getDefault().getID());
        event.setStart(start);

        String[] endParts = endDate.split("/");
        String endMonth = addZero(endParts[0]);
        String endDay = addZero(endParts[1]);
        String endYear = "20" + addZero(endParts[2]);


        //Set the end time at the last day last minute.
        DateTime endDateTime = new DateTime(endYear+"-"+endMonth+"-"+endDay+"T24:00:00.00Z");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(TimeZone.getDefault().getID());
        event.setEnd(end);

        event.setAttendees(attendees);

        //Set the reminders
        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("popup").setMinutes(10),
        };

        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";
        event = service.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
    }

    /*
    Returns the logged in user in the system.
     */
    public String getLoggedInUser() throws IOException, GeneralSecurityException{
        Calendar service = createService();

        com.google.api.services.calendar.model.Calendar calendar =
                service.calendars().get("primary").execute();

        return calendar.getSummary();

    }

    public static void login() throws IOException, GeneralSecurityException {
        createService();
    }
}