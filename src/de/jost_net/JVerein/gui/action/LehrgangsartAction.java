/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 * heiner@jverein.de
 * www.jverein.de
 * $Log$
 **********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.view.LehrgangsartDetailView;
import de.jost_net.JVerein.rmi.Lehrgangsart;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class LehrgangsartAction implements Action
{
  public void handleAction(Object context) throws ApplicationException
  {
    Lehrgangsart l = null;

    if (context != null && (context instanceof Lehrgangsart))
    {
      l = (Lehrgangsart) context;
    }
    else
    {
      try
      {
        l = (Lehrgangsart) Einstellungen.getDBService().createObject(
            Lehrgangsart.class, null);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(
            "Fehler bei der Erzeugung einer neuen Lehrgangsart", e);
      }
    }
    GUI.startView(LehrgangsartDetailView.class.getName(), l);
  }
}
