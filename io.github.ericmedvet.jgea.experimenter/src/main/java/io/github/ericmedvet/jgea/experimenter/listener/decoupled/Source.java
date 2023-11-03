package io.github.ericmedvet.jgea.experimenter.listener.decoupled;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author "Eric Medvet" on 2023/11/03 for jgea
 */
public interface Source {

  Collection<DataItemKey> dataItemKeys(MachineKey machineKey, ExperimentKey experimentKey, RunKey runKey);

  Collection<ExperimentKey> experimentKeys(MachineKey machineKey);

  Collection<MachineKey> machineKeys();

  List<MachineInfo> pop(MachineKey machineKey, LocalDateTime from);

  List<ExperimentInfo> pop(MachineKey machineKey, ExperimentKey experimentKey, LocalDateTime from);

  List<RunKey> pop(MachineKey machineKey, ExperimentKey experimentKey, RunKey runKey, LocalDateTime from);

  List<DataItemInfo> pop(
      MachineKey machineKey,
      ExperimentKey experimentKey,
      RunKey runKey,
      DataItemKey dataItemKey,
      LocalDateTime from
  );

  Collection<RunKey> runKeys(MachineKey machineKey, ExperimentKey experimentKey);
}
