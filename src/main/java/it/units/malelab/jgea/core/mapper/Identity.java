/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.units.malelab.jgea.core.mapper;

import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.event.MapperEvent;
import java.util.Collections;

/**
 *
 * @author eric
 */
public class Identity<A> extends DeterministicMapper<A, A>{

  @Override
  protected A map(A a, Listener listener) throws MappingException {
    listener.listen(new MapperEvent(a, a, Collections.EMPTY_MAP));
    return a;
  }

}
