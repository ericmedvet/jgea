/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.listener.event;

import java.util.Map;

/**
 *
 * @author eric
 */
public interface InfoEvent extends Event {
  
  public Map<String, Object> getInfo();
  
}
