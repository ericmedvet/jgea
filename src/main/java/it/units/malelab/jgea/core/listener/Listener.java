/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener;

import it.units.malelab.jgea.core.listener.event.Event;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author eric
 */
public interface Listener extends Serializable {
  
  public void listen(Event event);
  
}
