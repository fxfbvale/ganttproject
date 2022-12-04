package biz.ganttproject.impex.googleCalendar;


import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import biz.ganttproject.core.model.task.TaskDefaultColumn;

import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.export.GoogleCalendar;

import net.sourceforge.ganttproject.resource.HumanResourceManager;

import net.sourceforge.ganttproject.task.CustomColumnsManager;

import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.test.task.TaskTestCase;
import biz.ganttproject.core.time.CalendarFactory;

import java.util.*;
import java.security.GeneralSecurityException;



public class GoogleCalendarExportTest extends TaskTestCase {



    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TaskDefaultColumn.setLocaleApi(null);
    }

    public void testEventName()  {
        HumanResourceManager hrManager = new HumanResourceManager(null, new CustomColumnsManager());
        TaskManager taskManager = getTaskManager();


        GanttTask task1 = taskManager.createTask();
        GanttTask task2 = taskManager.createTask();
        GanttTask task3 = taskManager.createTask();

        task1.setName("a");
        task2.setName("b");
        task3.setName("c");

        GoogleCalendar gc = null;
        try {
            gc = new GoogleCalendar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }


        try
        {
            Event event = gc.createEvent(task1.getName(),task1.getStart(),task1.getDisplayEnd(),task1.getCost().getValue().toPlainString(),task1.getAssignments());
            assertEquals(event.getSummary(),"a");
        }catch (Exception E) {
            E.printStackTrace(System.out);
        }

        try
        {
            Event event = gc.createEvent(task2.getName(),task2.getStart(),task2.getDisplayEnd(),task2.getCost().getValue().toPlainString(),task2.getAssignments());
            assertEquals(event.getSummary(),"b");
        }catch (Exception E) {
            E.printStackTrace(System.out);
        }

        try
        {
            Event event = gc.createEvent(task3.getName(),task3.getStart(),task3.getDisplayEnd(),task3.getCost().getValue().toPlainString(),task3.getAssignments());
            assertEquals(event.getSummary(),"c");
        }catch (Exception E) {
            E.printStackTrace(System.out);
        }



    }

    public void testEventDates()  {
        HumanResourceManager hrManager = new HumanResourceManager(null, new CustomColumnsManager());
        TaskManager taskManager = getTaskManager();



        GanttTask task1 = taskManager.createTask();
        GanttTask task2 = taskManager.createTask();
        GanttTask task3 = taskManager.createTask();

        task1.setStart(CalendarFactory.createGanttCalendar(new Date(2010-1900, Calendar.OCTOBER,1)));
        task2.setStart(CalendarFactory.createGanttCalendar(new Date(2011-1900,Calendar.OCTOBER,2)));
        task3.setStart(CalendarFactory.createGanttCalendar(new Date(2012-1900,Calendar.APRIL,1)));

        task1.setEnd(CalendarFactory.createGanttCalendar(new Date(2010-1900,Calendar.OCTOBER,2)));
        task2.setEnd(CalendarFactory.createGanttCalendar(new Date(2011-1900,Calendar.OCTOBER,3)));
        task3.setEnd(CalendarFactory.createGanttCalendar(new Date(2012-1900,Calendar.APRIL,4)));



        try
        {
            GoogleCalendar gc = gc = new GoogleCalendar();
            Event event = gc.createEvent(task1.getName(),task1.getStart(),task1.getEnd(),task1.getCost().getValue().toPlainString(),task1.getAssignments());

            assertEquals(event.getStart().getDate().toString(),"2010-10-01");
            assertEquals(event.getEnd().getDate().toString(),"2010-10-02");
        }catch (Exception E) {
            E.printStackTrace(System.out);
        }

        try
        {
            GoogleCalendar gc = gc = new GoogleCalendar();
            Event event = gc.createEvent(task2.getName(),task2.getStart(),task2.getEnd(),task2.getCost().getValue().toPlainString(),task2.getAssignments());

            //Date time adds one more month when parssing to String
            assertEquals(event.getStart().getDate().toString(),"2011-10-02");
            assertEquals(event.getEnd().getDate().toString(),"2011-10-03");
        }catch (Exception E) {
            E.printStackTrace(System.out);
        }

        try
        {
            GoogleCalendar gc = gc = new GoogleCalendar();
            Event event = gc.createEvent(task3.getName(),task3.getStart(),task3.getEnd(),task3.getCost().getValue().toPlainString(),task3.getAssignments());

            assertEquals(event.getStart().getDate().toString(),"2012-04-01");
            assertEquals(event.getEnd().getDate().toString(),"2012-04-04");
        }catch (Exception E) {
            E.printStackTrace(System.out);
        }



    }




}