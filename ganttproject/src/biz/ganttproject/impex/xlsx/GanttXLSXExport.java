
package biz.ganttproject.impex.xlsx;

import biz.ganttproject.core.model.task.TaskDefaultColumn;
import biz.ganttproject.core.option.BooleanOption;
import biz.ganttproject.core.time.*;
import com.aspose.cells.*;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.ResourceDefaultColumn;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskProperties;
import net.sourceforge.ganttproject.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;


/**
 * Class to export create a BurndownChart in XLSX format
 *
 * @author Vale
 */
public class GanttXLSXExport {

    private final int DAY_MILI = 86400000;
    private final int GEORGIAN_CALENDAR = 1900;


    private final TaskManager myTaskManager;
    private GanttCalendar calendar;
    private Workbook workbook;
    private Worksheet worksheet;
    private Chart chart;
    private int projDuration;
    private int totalEffort;
    private double remainingEffort;
    private boolean goodPace;



    public GanttXLSXExport(IGanttProject project) {
        this(project.getTaskManager());
    }

    GanttXLSXExport(TaskManager taskManager) {
        myTaskManager = Preconditions.checkNotNull(taskManager);
        init();
    }

    private void init(){
        this.calendar = CalendarFactory.createGanttCalendar(new Date());
        this.workbook = new Workbook();
        this.worksheet = this.workbook.getWorksheets().get(0);
        this.projDuration = myTaskManager.getProjectLength().getLength();
        this.remainingEffort = 0;
        this.totalEffort = 0;
        this.goodPace = false;
        this.chart = null;
    }

    public Chart getChart() {
        return chart;
    }

    public boolean isGoodPace() {
        return goodPace;
    }

    public double getRemainingEffort() {
        return remainingEffort;
    }

    public int getProjDuration() {
        return projDuration;
    }

    public GanttCalendar getCalendar() {
        return calendar;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Worksheet getWorksheet() {
        return worksheet;
    }

    public int getTotalEffort() {
        return totalEffort;
    }

    public void createChart(final File output) throws Exception {
        init();
        // Adding sample values to cells
        worksheet.getCells().get("A2").putValue("Remaining Effort");
        worksheet.getCells().get("A3").putValue("Ideal Burndown");

        //get total effort
        for (Task task : myTaskManager.getTasks()){
            totalEffort += task.getDuration().getLength();
        }

        remainingEffort = totalEffort;

        //the amount of work that should be done everyday, equally
        double x = (double)totalEffort/(double)(projDuration);

        //the day of the earlier task
        Date start = myTaskManager.getProjectStart();

        worksheet.getCells().get(0, 1).putValue("Day0");
        worksheet.getCells().get(1, 1).putValue(totalEffort); //removing the same amount everytime
        worksheet.getCells().get(2, 1).putValue(totalEffort); //removing the same amount everytime


        for(int i = 1; i <= projDuration; i++){
            double dayEffort = 0;
            worksheet.getCells().get(0, i + 1).putValue("Day" + i);
            worksheet.getCells().get(2, i + 1).putValue(totalEffort - i*x); //removing the same amount everytime


            //we will be passing by every day of the project
            GanttCalendar day = CalendarFactory.createGanttCalendar(new Date( start.getTime() + DAY_MILI * (i - 1)  ));

            if(day.compareTo(calendar) <= 0){
                //for everyday we will see if each task is going to be done in that day (only will count the ones before the day we are at)
                for (Task task : myTaskManager.getTasks()){
                    //date of start of the task

                    Date taskStart = new Date(task.getStart().getYear() - GEORGIAN_CALENDAR, task.getStart().getMonth(), task.getStart().getDate());
                    for(int j = 0; j < task.getDuration().getLength(); j++){
                        GanttCalendar taskDay = CalendarFactory.createGanttCalendar(new Date( taskStart.getTime() + DAY_MILI * j ));

                        //if the task is in that day
                        if(day.compareTo(taskDay) == 0){
                            int percentage = 100/task.getDuration().getLength();
                            double workDone = this.getWorkDone(percentage, task.getCompletionPercentage(), j);
                            dayEffort += workDone;
                        }
                    }
                }
                remainingEffort -= dayEffort;
                worksheet.getCells().get(1, i + 1).putValue(remainingEffort); //removing the same amount everytime
                //depending on how we are doing at the last day the bar will show in different colors
                if(day.compareTo(calendar) == 0 && remainingEffort <= totalEffort - i*x)
                    goodPace = true;
                else
                    goodPace = false;
            }
        }

        this.editChartLooks();
        this.saveChart(output);


    }

    private void saveChart(File output) throws Exception {
        workbook.save(output.getAbsolutePath(), SaveFormat.XLSX);
    }

    private void editChartLooks(){
        // Adding a chart to the worksheet
        int chartIndex = worksheet.getCharts().add(ChartType.LINE_WITH_DATA_MARKERS, 5, 0, 30, projDuration + 3);

        // Accessing the instance of the newly added chart
        chart = worksheet.getCharts().get(chartIndex);

        //setting the title of the chart
        Title title = chart.getTitle();
        title.setText("Sprint Burndown Chart");

        // Setting the font color of the chart title to blue
        Font font = title.getFont();
        font.setSize(13);

        //Set Properties of nseries
        int s2_idx = chart.getNSeries().add("A2: A2", true);
        int s3_idx = chart.getNSeries().add("A22: A22", true);

        // Set IsColorVaried to true for varied points color
        chart.getNSeries().setColorVaried(true);

        // Set properties of background area and series markers
        chart.getNSeries().get(s2_idx).getArea().setFormatting(FormattingType.CUSTOM);
        if(goodPace) {
            chart.getNSeries().get(s2_idx).getMarker().getArea().setForegroundColor(Color.getBlue());
            chart.getNSeries().get(s2_idx).getBorder().setColor(Color.getBlue());
        }
        else{
            chart.getNSeries().get(s2_idx).getMarker().getArea().setForegroundColor(Color.getOrangeRed());
            chart.getNSeries().get(s2_idx).getBorder().setColor(Color.getOrangeRed());
        }
        chart.getNSeries().get(s2_idx).getMarker().getBorder().setVisible(false);


        // Set properties of background area and series markers
        chart.getNSeries().get(s3_idx).getArea().setFormatting(FormattingType.CUSTOM);
        chart.getNSeries().get(s3_idx).getMarker().getArea().setForegroundColor(Color.getGreen());
        chart.getNSeries().get(s3_idx).getBorder().setColor(Color.getGreen());
        chart.getNSeries().get(s3_idx).getMarker().setMarkerSize(2);
        chart.getNSeries().get(s3_idx).getMarker().getBorder().setVisible(false);



        // Setting the foreground color of the plot area
        ChartFrame plotArea = chart.getPlotArea();
        Area area = plotArea.getArea();
        area.setForegroundColor(Color.getWhite());

        // Setting the foreground color of the chart area
        ChartArea chartArea = chart.getChartArea();
        area = chartArea.getArea();
        area.setForegroundColor(Color.getWhite());

        // Setting chart data source as the range  "A1:C4"

        int row = 2;
        int column = projDuration + 1;
        String name = CellsHelper.cellIndexToName(row, column);

        chart.setChartDataRange("A1:" + name, false);
    }

    private double getWorkDone(int percentage, int workDone, int day){
        if(percentage * day >= workDone) //that day there was no advance in the task
            return 0;
        else if(percentage * (day + 1) <= workDone)
            return 1;
        else
            return (workDone - (percentage * day))/100.0;
    }

}







