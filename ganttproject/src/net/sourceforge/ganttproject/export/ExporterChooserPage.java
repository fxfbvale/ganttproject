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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import biz.ganttproject.core.option.GPOptionGroup;

import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.export.ExportFileWizardImpl.State;
import net.sourceforge.ganttproject.gui.options.GPOptionChoicePanel;
import net.sourceforge.ganttproject.gui.projectwizard.WizardPage;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.gui.UIFacade.Centering;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.google.common.collect.Maps;

import net.sourceforge.ganttproject.action.CancelAction;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.action.OkAction;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIFacade.Centering;
import net.sourceforge.ganttproject.gui.UIFacade.Dialog;
import net.sourceforge.ganttproject.gui.options.TopPanel;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author bard
 */
class ExporterChooserPage implements WizardPage {
  private final List<Exporter> myExporters;
  private final State myState;
  private final GanttLanguage language = GanttLanguage.getInstance();

  ExporterChooserPage(List<Exporter> exporters, ExportFileWizardImpl.State state) {
    myExporters = exporters;
    myState = state;

  }

  @Override
  public String getTitle() {
    return language.getText("option.exporter.title");
  }

  @Override
  public Component getComponent() {
    int selectedGroupIndex = 0;
    Action[] choiceChangeActions = new Action[myExporters.size()];
    GPOptionGroup[] choiceOptions = new GPOptionGroup[myExporters.size()];
    for (int i = 0; i < myExporters.size(); i++) {
      final Exporter nextExporter = myExporters.get(i);
      if (nextExporter == myState.getExporter()) {
        selectedGroupIndex = i;
      }
      Action nextAction = new AbstractAction(nextExporter.getFileTypeDescription()) {
        @Override
        public void actionPerformed(ActionEvent e) {
          ExporterChooserPage.this.myState.setExporter(nextExporter);
        }
      };

      GPOptionGroup nextOptions = nextExporter.getOptions();
      if (nextOptions != null) {
        nextOptions.lock();
      }

      if( nextExporter.getActions() != null ){
        GPAction action = nextExporter.getActions();
      }


      choiceChangeActions[i] = nextAction;
      //Add ACTION OPTIONS
      choiceOptions[i] = nextOptions;
    }
    GPOptionChoicePanel choicePanel = new GPOptionChoicePanel();
    return choicePanel.getComponent(choiceChangeActions, choiceOptions, selectedGroupIndex);
  }

  @Override
  public void setActive(boolean b) {
    if (false == b) {
      for (Exporter e : myExporters) {
        if (e.getOptions() != null) {
          e.getOptions().commit();
        }
      }
    } else {
      for (Exporter e : myExporters) {
        if (e.getOptions() != null) {
          e.getOptions().lock();
        }
      }

    }
  }

}
