/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/

package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.gui.dialogs.ShowVariablesDialog;
import de.jost_net.JVerein.gui.menu.ShowVariablesMenu;
import de.jost_net.JVerein.rmi.Lesefeld;
import de.jost_net.JVerein.util.LesefeldAuswerter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein View zum Bearbeiten von Skripten f�r ein Lesefeld.
 */
public class LesefeldDetailView extends AbstractView implements Listener
{

  private LesefeldAuswerter lesefeldAuswerter;

  private Lesefeld lesefeld;

  private TextAreaInput textAreaInputScriptCode;

  private TextAreaInput textAreaInputScriptResult;

  private TextInput textInputScriptName;

  @Override
  public void bind() throws Exception
  {
    Composite parent = getParent();
    SimpleContainer container = new SimpleContainer(parent, true);

    // Auf diese Input-Felder sollte nur �ber die Funktionen
    // updateLesefeldFromGUI() und updateScriptResult() zugegriffen werden.
    textInputScriptName = new TextInput(
        lesefeld != null ? lesefeld.getBezeichnung() : "");
    container.addLabelPair(JVereinPlugin.getI18n().tr("Skript-Name"), textInputScriptName);

    textAreaInputScriptCode = new TextAreaInput(
        lesefeld != null ? lesefeld.getScript() : "");
    container.addLabelPair(JVereinPlugin.getI18n().tr("Skript"), textAreaInputScriptCode);

    textAreaInputScriptResult = new TextAreaInput("");
    container.addLabelPair(JVereinPlugin.getI18n().tr("Ausgabe"), textAreaInputScriptResult);

    if (lesefeld != null)
      updateScriptResult();

    ButtonArea buttonArea = new ButtonArea();
    Button button = new Button(
        JVereinPlugin.getI18n().tr("Aktualisieren (F5)"), new Action()
        {
          public void handleAction(Object context) throws ApplicationException
          {
            updateScriptResult();
          }
        }, null, false, "view-refresh.png");
    buttonArea.addButton(button);
    button = new Button(JVereinPlugin.getI18n().tr("Variablen anzeigen (F6)"),
        new OpenInsertVariableDialogAction(), null, false, "zuordnung.png");
    buttonArea.addButton(button);
    button = new Button(JVereinPlugin.getI18n().tr("Speichern und zur�ck"),
        new SaveLesefeldAction(), null, false, "document-save.png");
    buttonArea.addButton(button);
    button = new Button(JVereinPlugin.getI18n().tr("Abbrechen und zur�ck"),
        new AbortEditLesefeldAction(), null, false, "process-stop.png");
    buttonArea.addButton(button);
    buttonArea.paint(this.getParent());
  }

  /**
   * Aktualisiert lokales Feld lesefeld mit den vom Nutzer eingegebenen Daten
   * aus der GUI. Dabei wird ggf. lesefeld initialisiert und die Eindeutigkeit
   * des Namens des Skriptes sichergestellt.
   * 
   * @return
   */
  private boolean updateLesefeldFromGUI()
  {
    if (textInputScriptName.getValue().toString().isEmpty())
    {
      GUI.getStatusBar().setErrorText(
          JVereinPlugin.getI18n().tr("Bitte Skript-Namen eingeben."));
      return false;
    }
    try
    {
      for (Lesefeld lesefeld : lesefeldAuswerter.getLesefelder())
      {
        // Bezeichnung von Lesefeld muss eindeutig sein.
        if (lesefeld.getBezeichnung().equals(textInputScriptName.getValue()))
        {
          String currentid = lesefeld.getID();
          if (this.lesefeld == null
              || (this.lesefeld != null && !this.lesefeld.getID()
                  .equalsIgnoreCase((currentid))))
          {
            GUI.getStatusBar().setErrorText(
                JVereinPlugin.getI18n().tr(
                    "Bitte eindeutigen Skript-Namen eingeben!"));
            return false;
          }
        }
      }

      // erstelle neues lesefeld, wenn n�tig.
      if (lesefeld == null)
        lesefeld = (Lesefeld) Einstellungen.getDBService().createObject(
            Lesefeld.class, null);

      lesefeld.setBezeichnung((String) textInputScriptName.getValue());
      lesefeld.setScript((String) textAreaInputScriptCode.getValue());
      lesefeld.setEvaluatedContent((String) textAreaInputScriptResult
          .getValue());
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
      GUI.getStatusBar().setErrorText(e.getMessage());
      return false;
    }

    return true;
  }

  @Override
  public void unbind() throws ApplicationException
  {
    GUI.getDisplay().removeFilter(SWT.KeyDown, this);
  };

  public LesefeldDetailView(LesefeldAuswerter lesefeldAuswerter,
      Lesefeld lesefeld)
  {
    this.lesefeldAuswerter = lesefeldAuswerter;
    this.lesefeld = lesefeld;

    // KeyListener f�r HotKeys.
    GUI.getDisplay().addFilter(SWT.KeyDown, this);
  }

  /**
   * Holt akutelles Skript von GUI, evaluiert dieses und schreibt Ergebnis
   * zur�ck in die GUI.
   * 
   * @return true bei Erfolg, sonst false (Fehlermeldung wird in
   *         Skript-Ausgabe-Feld geschrieben).
   */
  private boolean updateScriptResult()
  {
    if (!updateLesefeldFromGUI())
      return false;
    String result = "";
    boolean success = true;
    try
    {
      result = (String) lesefeldAuswerter.eval(lesefeld.getScript());
      if (result == null)
      {
        result = JVereinPlugin.getI18n().tr(
            "Skript-Fehler: Skript muss R�ckgabewert liefern.");
        success = false;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      result = JVereinPlugin.getI18n().tr("Skript-Fehler:\r\n")
          + e.getMessage();
      success = false;
    }
    finally
    {
      textAreaInputScriptResult.setValue(result);
    }

    return success;
  }

  public void handleEvent(Event event)
  {
    // aktualisiere Script-Ausgabe, wenn F5 gedr�ckt wird.
    if (event.keyCode == org.eclipse.swt.SWT.F5)
    {
      updateScriptResult();
    }
    else if (event.keyCode == org.eclipse.swt.SWT.F6)
    {
      new OpenInsertVariableDialogAction().handleAction(null);
    }
  }

  private final class AbortEditLesefeldAction implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      GUI.startPreviousView();
    }
  }

  private final class SaveLesefeldAction implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      if (updateScriptResult())
      {

        lesefeldAuswerter.addLesefelderDefinition(lesefeld);

        try
        {
          lesefeld.store();
          GUI.getStatusBar().setSuccessText(JVereinPlugin.getI18n().tr("Skript gespeichert."));
          GUI.startPreviousView();
        }
        catch (RemoteException e)
        {
          e.printStackTrace();
          GUI.getStatusBar().setErrorText(
              JVereinPlugin.getI18n().tr(
                  "Skript kann nicht gespeichert werden."));
          Logger.error(
              JVereinPlugin.getI18n().tr(
                  "Skript kann nicht gespeichert werden."), e);
        }

      }
      else
        GUI.getStatusBar().setErrorText(
            JVereinPlugin.getI18n().tr(
                "Skript enth�lt Fehler. Kann nicht gespeichert werden."));

    }
  }

  private final class OpenInsertVariableDialogAction implements Action
  {
    public void handleAction(Object context)
    {
      try
      {
        ShowVariablesDialog d = new ShowVariablesDialog(
            lesefeldAuswerter.getMap(), false);
        ShowVariablesMenu menu = new ShowVariablesMenu();
        menu.setPrependCopyText("");
        menu.setAppendCopyText("");
        d.setContextMenu(menu);
        d.setDoubleClickAction(menu.getCopyToClipboardAction());
        d.open();
      }
      catch (Exception e)
      {
        Logger.error(
            JVereinPlugin.getI18n().tr("Fehler beim Anzeigen der Variablen."),
            e);
        GUI.getStatusBar().setErrorText("Fehler beim Anzeigen der Variablen.");
      }

    }

  }

}