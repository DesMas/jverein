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
package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

public interface Report extends DBObject
{

  public String getBezeichnung() throws RemoteException;

  public void setBezeichnung(String bezeichnung) throws RemoteException;

  public String getQuelle() throws RemoteException;

  public void setQuelle(String quelle) throws RemoteException;

  public byte[] getKompilat() throws RemoteException;

  public void setKompilat(byte[] kompilat) throws RemoteException;

}
