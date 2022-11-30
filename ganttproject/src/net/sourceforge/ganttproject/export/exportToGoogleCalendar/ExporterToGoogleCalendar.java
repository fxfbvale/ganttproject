/*
Copyright 2005-2012 GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;



import java.awt.Component;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.image.RenderedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;


import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import biz.ganttproject.core.option.EnumerationOption;
import biz.ganttproject.core.option.GPAbstractOption;
import biz.ganttproject.core.option.GPOption;
import biz.ganttproject.core.option.GPOptionGroup;

import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.action.GPAction;

public class ExporterToGoogleCalendar extends ExporterBase {

    static class FileTypeOption extends GPAbstractOption<String> implements EnumerationOption {
        static final String[] FILE_FORMAT_ID = new String[] { "impex.image.fileformat.png", "impex.image.fileformat.jpeg" };

        static final String[] FILE_EXTENSION = new String[] { "png", "jpg" };

        // TODO GPAbstractOption already has this field, why add it again?!
        private String myValue = FileTypeOption.FILE_FORMAT_ID[0];

        FileTypeOption() {
            super("impex.image.fileformat");
        }

        private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        private static final String TOKENS_DIRECTORY_PATH = "tokens";
        private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar");
        private static final String CREDENTIALS_FILE_PATH = "/credentials.json";






        @Override
        public String[] getAvailableValues() {
            return FileTypeOption.FILE_FORMAT_ID;
        }

        @Override
        public void setValue(String value) {
            myValue = value;
        }

        @Override
        public String getValue() {
            return myValue;
        }

        String proposeFileExtension() {
            for (int i = 0; i < FileTypeOption.FILE_FORMAT_ID.length; i++) {
                if (myValue.equals(FileTypeOption.FILE_FORMAT_ID[i])) {
                    return FileTypeOption.FILE_EXTENSION[i];
                }
            }
            throw new IllegalStateException("Selected format=" + myValue + " has not been found in known formats:"
                    + Arrays.asList(FileTypeOption.FILE_FORMAT_ID));
        }

        @Override
        public String getPersistentValue() {
            return null;
        }

        @Override
        public void loadPersistentValue(String value) {
        }

        @Override
        public boolean isChanged() {
            return false;
        }
    }

    private final FileTypeOption myFileTypeOption = new FileTypeOption();

    private final GPOptionGroup myOptions = new GPOptionGroup("impex.image", new GPOption[] { myFileTypeOption });

    public ExporterToGoogleCalendar() {
        myOptions.setTitled(false);
    }

    @Override
    public String getFileTypeDescription() {
        return MessageFormat.format(GanttLanguage.getInstance().getText("impex.googleCalendar.description"),
                new Object[] { proposeFileExtension() });
    }

    private final GPAction myActions = new GPAction("Login in Google Account") {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("o abreu é gay");
        }
    };
    public GPAction getActions() {
        return myActions;
    }

    @Override
    public GPOptionGroup getOptions() {
        return null;
    }

    @Override
    public List<GPOptionGroup> getSecondaryOptions() {
        return Collections.singletonList(createExportRangeOptionGroup());
    }

    private boolean userLoggedIn(){
        File storedCredentials = new File("tokens/StoredCredential");
        return storedCredentials.exists();
    }


    @Override
    public Component getCustomOptionsUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder("Google Account where to export project"));
        mainPanel.setPreferredSize(new Dimension(400, 100));
        JPanel labelPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        mainPanel.add(labelPanel, BorderLayout.LINE_START);
        mainPanel.add(buttonPanel, BorderLayout.LINE_END);
        JLabel emailInfo = new JLabel();
        try{
            GoogleCalendar gc = new GoogleCalendar();
            //gc.login();
            emailInfo.setText(userLoggedIn() ? gc.getLoggedInUser() : "No account connect yet." );
            JButton loginButton = new JButton(userLoggedIn() ? "Change Google account" : "Login in Google account");
            labelPanel.add( emailInfo, BorderLayout.LINE_START );
            mainPanel.add( loginButton, BorderLayout.LINE_START );
            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        File storedCredentials = new File("tokens/StoredCredential");
                        System.out.println(storedCredentials.delete());
                        gc.login();
                        if(userLoggedIn()) {
                            emailInfo.setText(gc.getLoggedInUser());
                            loginButton.setText("Change Google account");
                        }
                    }catch (Exception E){
                        E.printStackTrace(System.out);
                    }
                }
            });
        }catch (Exception E){
            E.printStackTrace(System.out);
        }
        return mainPanel;
    }

    @Override
    public String getFileNamePattern() {
        return proposeFileExtension();
    }

    @Override
    protected ExporterJob[] createJobs(final File outputFile, List<File> resultFiles) {
        ExporterJob job = createImageExportJob(outputFile);
        resultFiles.add(outputFile);
        return new ExporterJob[] { job };
    }

    private ExporterJob createImageExportJob(final File outputFile) {
        ExporterJob result = new ExporterJob("Export project") {
            @Override
            protected IStatus run() {
                OutputStream outputStream = null;
                try {
                    System.out.println(getProject());
                    /*
                    outputFile.createNewFile();
                    outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                    // TODO Fix this ugly hack!! Ie make the settings available in a proper way
                    GanttCSVExport exporter = new GanttCSVExport(getProject(),
                            ((GanttProject) getProject()).getGanttOptions().getCSVOptions());
                    exporter.save(outputStream);
                    outputStream.flush();
                    */

                } catch (Exception e) {
                    getUIFacade().showErrorDialog(e);
                    return Status.CANCEL_STATUS;
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            System.out.println("Error exporting");
                        }
                    }
                }
                return Status.OK_STATUS;
            }
        };
        return result;
    }

    @Override
    public String proposeFileExtension() {
        return myFileTypeOption.proposeFileExtension();
    }

    @Override
    public String[] getFileExtensions() {
        return FileTypeOption.FILE_EXTENSION;
    }
}