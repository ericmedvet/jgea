package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import io.github.ericmedvet.jgea.core.util.HashMapTable;
import io.github.ericmedvet.jgea.core.util.Table;

import java.time.LocalDateTime;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public class TabledSource {

  private final Source source;
  private final Table<LocalDateTime, MachineKey, MachineInfo> machines;
  private final Table<LocalDateTime, ProcessKey, ProcessInfo> processes;
  private final Table<LocalDateTime, ProcessKey, LogInfo> logs;
  private final Table<LocalDateTime, ExperimentKey, ExperimentInfo> experiments;
  private final Table<LocalDateTime, RunKey, RunInfo> runs;
  private final Table<LocalDateTime, DataItemKey, DataItemInfo> dataItems;

  public TabledSource(Source source) {
    this.source = source;
    machines = new HashMapTable<>();
    processes = new HashMapTable<>();
    logs = new HashMapTable<>();
    experiments = new HashMapTable<>();
    runs = new HashMapTable<>();
    dataItems = new HashMapTable<>();
  }


}
