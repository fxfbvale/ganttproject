// Copyright (C) 2017 BarD Software
package biz.ganttproject.impex.xlsx;

import biz.ganttproject.impex.xlsx.GanttXLSXExport;
import biz.ganttproject.core.model.task.TaskDefaultColumn;
import biz.ganttproject.core.option.BooleanOption;
import biz.ganttproject.core.time.*;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.test.task.TaskTestCase;

import java.awt.*;
import java.io.IOException;
import java.util.Set;
import java.util.Date;
import java.io.File;


/**
 * @author vale
 */
public class GPXLSXExportTest extends TaskTestCase {

  private final int GEORGIAN_CALENDAR = 1900;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TaskDefaultColumn.setLocaleApi(null);
  }


  public void testProject() throws Exception {
    TaskManager taskManager = getTaskManager();

    Task task1 = createTask();
    Task task2 = createTask();
    Task task3 = createTask();

    GanttCalendar start = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 3));
    GanttCalendar end = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 5));

    task1.setStart(start);
    task1.setEnd(end);

    start = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 5));
    end = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 10));

    task2.setStart(start);
    task2.setEnd(end);

    start = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 12));
    end = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 15));

    task3.setStart(start);
    task3.setEnd(end);

    //edit tasks
    //also i need a get in the Exportet to have access to the data structures

    GanttXLSXExport exporter = new GanttXLSXExport(taskManager);
    File outputFile = new File("outputFile");
    exporter.createChart(outputFile);

    //testing project duration
    assertEquals(15 - 3, exporter.getProjDuration());
    //testing reamining work, and since there was no work done this showd stay the same
    assertEquals(5-3 + 10-5 + 15-12, exporter.getTotalEffort());
    assertEquals(10.0, exporter.getRemainingEffort());


    task1.setCompletionPercentage(100);
    task2.setCompletionPercentage(100);

    exporter = new GanttXLSXExport(taskManager);
    outputFile = new File("outputFile");
    exporter.createChart(outputFile);

    assertEquals(5-3 + 10-5 + 15-12, exporter.getTotalEffort());
    assertEquals(15-12.0, exporter.getRemainingEffort());

    assertEquals(false, exporter.isGoodPace());
  }


  public void testWorksheet() throws Exception {
    TaskManager taskManager = getTaskManager();

    Task task1 = createTask();
    Task task2 = createTask();
    Task task3 = createTask();

    GanttCalendar start = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 3));
    GanttCalendar end = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 5));

    task1.setStart(start);
    task1.setEnd(end);

    start = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 5));
    end = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 10));

    task2.setStart(start);
    task2.setEnd(end);

    start = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 12));
    end = CalendarFactory.createGanttCalendar(new Date(2022 - GEORGIAN_CALENDAR, 10, 15));

    task3.setStart(start);
    task3.setEnd(end);

    //edit tasks
    //also i need a get in the Exportet to have access to the data structures

    GanttXLSXExport exporter = new GanttXLSXExport(taskManager);
    File outputFile = new File("outputFile");
    exporter.createChart(outputFile);

    assertEquals("Day1", exporter.getWorksheet().getCells().get(0,2).getValue());
    assertEquals(10 - (10/12.0), exporter.getWorksheet().getCells().get(2,2).getValue());
    assertEquals(10.0, exporter.getWorksheet().getCells().get(1,2).getValue());
  }

}
