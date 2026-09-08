## Package `accumulator`

Aliases: `acc`, `accumulator`

### Builder `accumulator.all()`

`acc.all(eFunction; listFunction)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `eFunction` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, F&gt;</code> |
| `listFunction` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;F&gt;, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Accumulators.all()` by jgea-experimenter:2.8.2

### Builder `accumulator.first()`

`acc.first(function)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `function` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Accumulators.first()` by jgea-experimenter:2.8.2

### Builder `accumulator.last()`

`acc.last(function)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `function` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Accumulators.last()` by jgea-experimenter:2.8.2

## Package `consumer`

### Builder `consumer.composed()`

`consumer.composed(f; consumer)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, O&gt;</code> |
| `consumer` | npm |  | <code><abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;O, K&gt;</code> |

Produces <code><abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;X, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Consumers.composed()` by jgea-experimenter:2.8.2

### Builder `consumer.deaf()`

`consumer.deaf()`

Produces <code><abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;?, ?&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Consumers.deaf()` by jgea-experimenter:2.8.2

### Builder `consumer.saver()`

`consumer.saver(of; overwrite; path; suffix; verbose)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, O&gt;</code> |
| `overwrite` | b | `false` | <code>boolean</code> |
| `path` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `suffix` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;X, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Consumers.saver()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.arena`

Aliases: `ds.arena`, `dynSys.arena`, `dynamicalSystem.arena`

### Builder `dynamicalSystem.arena.fromString()`

`ds.arena.fromString(name; s; l; diagonal; emptyChar; obstacleChar; startChar; targetChar; separatorChar)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `custom` | <code><abbr title="java.lang.String">String</abbr></code> |
| `s` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `l` | d | `0.1` | <code>double</code> |
| `diagonal` | b | `false` | <code>boolean</code> |
| `emptyChar` | s | ` ` | <code><abbr title="java.lang.String">String</abbr></code> |
| `obstacleChar` | s | `w` | <code><abbr title="java.lang.String">String</abbr></code> |
| `startChar` | s | `s` | <code><abbr title="java.lang.String">String</abbr></code> |
| `targetChar` | s | `t` | <code><abbr title="java.lang.String">String</abbr></code> |
| `separatorChar` | s | `|` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationArena">NavigationArena</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Arenas.fromString()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.arena.prepared()`

`ds.arena.prepared(name; initialRobotXRange; initialRobotYRange; targetXRange; targetYRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | e | `EMPTY` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.Arena$Prepared">Arena$Prepared</abbr></code> |
| `initialRobotXRange` | npm | `m.range(max = 0.55; min = 0.45)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `initialRobotYRange` | npm | `m.range(max = 0.85; min = 0.8)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `targetXRange` | npm | `m.range(max = 0.5; min = 0.5)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `targetYRange` | npm | `m.range(max = 0.15; min = 0.15)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationArena">NavigationArena</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Arenas.prepared()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.biAgentTask`

Aliases: `ds.baTask`, `ds.bat`, `ds.biAgentTask`, `dynSys.baTask`, `dynSys.bat`, `dynSys.biAgentTask`, `dynamicalSystem.baTask`, `dynamicalSystem.bat`, `dynamicalSystem.biAgentTask`

### Builder `dynamicalSystem.biAgentTask.fromEnvironment()`

`ds.bat.fromEnvironment(name; environment; stopCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{environment.name}[{tRange.min};{tRange.max}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `environment` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiEnvironment">HomogeneousBiEnvironment</abbr>&lt;O, A, S, C&gt;</code> |
| `stopCondition` | npm |  | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;S&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask">HomogeneousBiAgentTask</abbr>&lt;C, O, A, S&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.HomogeneousBiAgentTasks.fromEnvironment()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.drawer`

Aliases: `ds.d`, `ds.drawer`, `dynSys.d`, `dynSys.drawer`, `dynamicalSystem.d`, `dynamicalSystem.drawer`

### Builder `dynamicalSystem.drawer.navigation()`

`ds.d.navigation(ioType; showSensors)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ioType` | e | `GRAPHIC` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationDrawer$Configuration$IOType">NavigationDrawer$Configuration$IOType</abbr></code> |
| `showSensors` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationDrawer">NavigationDrawer</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Drawers.navigation()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.drawer.pointNavigation()`

`ds.d.pointNavigation(ioType)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ioType` | e | `GRAPHIC` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationDrawer$Configuration$IOType">NavigationDrawer$Configuration$IOType</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationDrawer">PointNavigationDrawer</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Drawers.pointNavigation()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.drawer.pong()`

`ds.d.pong()`

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongDrawer">PongDrawer</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Drawers.pong()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.drawer.sequentialBf()`

`ds.d.sequentialBf(configuration; scoreTypes)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration">Configuration</abbr></code> |
| `scoreTypes` | e[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils$ScoreType">BooleanUtils$ScoreType</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunctionDrawer">SequentialBooleanFunctionDrawer</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Drawers.sequentialBf()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.drawer.vectorField()`

`ds.d.vectorField(arena)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `arena` | e |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.Arena$Prepared">Arena$Prepared</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.drawer.VectorFieldDrawer">VectorFieldDrawer</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Drawers.vectorField()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.drawer.vectorialTrajectory()`

`ds.d.vectorialTrajectory(configuration; reductionType)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration">Configuration</abbr></code> |
| `reductionType` | e | `PCA` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.TrajectoryPlot$Data$ReductionType">TrajectoryPlot$Data$ReductionType</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.drawer.VectorialTrajectoryDrawer">VectorialTrajectoryDrawer</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Drawers.vectorialTrajectory()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.environment`

Aliases: `ds.e`, `ds.env`, `ds.environment`, `dynSys.e`, `dynSys.env`, `dynSys.environment`, `dynamicalSystem.e`, `dynamicalSystem.env`, `dynamicalSystem.environment`

### Builder `dynamicalSystem.environment.navigation()`

`ds.e.navigation(name; initialRobotDirectionRange; robotRadius; robotMaxV; sensorsAngleRange; nOfSensors; sensorRange; targetSensing; arena; rescaleInput; relativeV; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `nav-{arena.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `initialRobotDirectionRange` | npm | `m.range(max = 0; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `robotRadius` | d | `0.05` | <code>double</code> |
| `robotMaxV` | d | `0.1` | <code>double</code> |
| `sensorsAngleRange` | npm | `m.range(max = 1.57; min = -1.57)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `nOfSensors` | i | `5` | <code>int</code> |
| `sensorRange` | d | `0.5` | <code>double</code> |
| `targetSensing` | e | `LIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment$Configuration$TargetSensing">NavigationEnvironment$Configuration$TargetSensing</abbr></code> |
| `arena` | npm | `ds.arena.prepared()` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationArena">NavigationArena</abbr></code> |
| `rescaleInput` | b | `true` | <code>boolean</code> |
| `relativeV` | b | `true` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.Environment">Environment</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment$State">NavigationEnvironment$State</abbr>, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;CS&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Environments.navigation()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pointNavigation()`

`ds.e.pointNavigation(name; robotMaxV; collisionBlock; arena; rescaleInput; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `nav-{arena}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `robotMaxV` | d | `0.01` | <code>double</code> |
| `collisionBlock` | d | `0.005` | <code>double</code> |
| `arena` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationArena">NavigationArena</abbr></code> |
| `rescaleInput` | b | `true` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.Environment">Environment</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationEnvironment$State">PointNavigationEnvironment$State</abbr>, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;CS&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Environments.pointNavigation()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong()`

`ds.e.pong(name; racketsInitialYRange; racketsLength; racketsMaxDeltaPosition; ballInitialVelocity; ballMaxVelocity; ballInitialAngleRange; ballAccelerationRate; maxPercentageAngleAdjustment; arenaXLength; arenaYLength; precision; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `pong` | <code><abbr title="java.lang.String">String</abbr></code> |
| `racketsInitialYRange` | npm | `m.range(max = 28; min = 22)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `racketsLength` | d | `5.0` | <code>double</code> |
| `racketsMaxDeltaPosition` | d | `0.5` | <code>double</code> |
| `ballInitialVelocity` | d | `20.0` | <code>double</code> |
| `ballMaxVelocity` | d | `50.0` | <code>double</code> |
| `ballInitialAngleRange` | npm | `m.range(max = 0.4; min = -0.4)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `ballAccelerationRate` | d | `1.1` | <code>double</code> |
| `maxPercentageAngleAdjustment` | d | `0.1` | <code>double</code> |
| `arenaXLength` | d | `60.0` | <code>double</code> |
| `arenaYLength` | d | `50.0` | <code>double</code> |
| `precision` | d | `1.0E-5` | <code>double</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment">PongEnvironment</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Environments.pong()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.environment.navigation`

Aliases: `ds.e.n`, `ds.e.nav`, `ds.e.navigation`, `ds.env.n`, `ds.env.nav`, `ds.env.navigation`, `ds.environment.n`, `ds.environment.nav`, `ds.environment.navigation`, `dynSys.e.n`, `dynSys.e.nav`, `dynSys.e.navigation`, `dynSys.env.n`, `dynSys.env.nav`, `dynSys.env.navigation`, `dynSys.environment.n`, `dynSys.environment.nav`, `dynSys.environment.navigation`, `dynamicalSystem.e.n`, `dynamicalSystem.e.nav`, `dynamicalSystem.e.navigation`, `dynamicalSystem.env.n`, `dynamicalSystem.env.nav`, `dynamicalSystem.env.navigation`, `dynamicalSystem.environment.n`, `dynamicalSystem.environment.nav`, `dynamicalSystem.environment.navigation`

### Builder `dynamicalSystem.environment.navigation.arenaCoverage()`

`ds.e.n.arenaCoverage(name; of; xBins; yBins; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `arena.coverage[{xBins}x{yBins}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `xBins` | i | `10` | <code>int</code> |
| `yBins` | i | `10` | <code>int</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.arenaCoverage()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.avgD()`

`ds.e.n.avgD(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `avg.dist` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.avgD()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.avgGapToObstacle()`

`ds.e.n.avgGapToObstacle(name; of; direction; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `avg.gap[d={direction:%.2f}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment$State">NavigationEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `direction` | d | `-1.5707963267948966` | <code>double</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.avgGapToObstacle()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.closestRobotP()`

`ds.e.n.closestRobotP(name; of; normalized)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `closest.pos` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `normalized` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.geometry.Point">Point</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.closestRobotP()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.distanceFromTarget()`

`ds.e.n.distanceFromTarget(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `dist` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.distanceFromTarget()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.finalD()`

`ds.e.n.finalD(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `final.dist` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.finalD()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.finalRobotP()`

`ds.e.n.finalRobotP(name; of; normalized)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `final.robot.pos` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `normalized` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.geometry.Point">Point</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.finalRobotP()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.finalTargetP()`

`ds.e.n.finalTargetP(name; of; normalized)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `final.target.pos` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `normalized` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.geometry.Point">Point</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.finalTargetP()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.finalTime()`

`ds.e.n.finalTime(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `final.time` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.finalTime()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.finalTimePlusD()`

`ds.e.n.finalTimePlusD(name; of; epsilon; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `final.td` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `epsilon` | d | `0.01` | <code>double</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.finalTimePlusD()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.minD()`

`ds.e.n.minD(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `min.dist` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.minD()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.symbolicTrajectory()`

`ds.e.n.symbolicTrajectory(name; of; movTRate; turnT; forwardSymbol; backwardSymbol; forwardLeftSymbol; forwardRightSymbol; backwardLeftSymbol; backwardRightSymbol; rotateLeftSymbol; rotateRightSymbol; stopSymbol; collapse; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `symbolic.trajectory[{collapse}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment$State">NavigationEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `movTRate` | d | `0.1` | <code>double</code> |
| `turnT` | d | `0.25` | <code>double</code> |
| `forwardSymbol` | s | `↑` | <code><abbr title="java.lang.String">String</abbr></code> |
| `backwardSymbol` | s | `↓` | <code><abbr title="java.lang.String">String</abbr></code> |
| `forwardLeftSymbol` | s | `↖` | <code><abbr title="java.lang.String">String</abbr></code> |
| `forwardRightSymbol` | s | `↗` | <code><abbr title="java.lang.String">String</abbr></code> |
| `backwardLeftSymbol` | s | `↙` | <code><abbr title="java.lang.String">String</abbr></code> |
| `backwardRightSymbol` | s | `↘` | <code><abbr title="java.lang.String">String</abbr></code> |
| `rotateLeftSymbol` | s | `↶` | <code><abbr title="java.lang.String">String</abbr></code> |
| `rotateRightSymbol` | s | `↷` | <code><abbr title="java.lang.String">String</abbr></code> |
| `stopSymbol` | s | `o` | <code><abbr title="java.lang.String">String</abbr></code> |
| `collapse` | b | `false` | <code>boolean</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.symbolicTrajectory()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.x()`

`ds.e.n.x(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `x` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.geometry.Point">Point</abbr>&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.x()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.navigation.y()`

`ds.e.n.y(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `y` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.geometry.Point">Point</abbr>&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationFunctions.y()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.environment.navigation.reward`

Aliases: `ds.e.n.reward`, `ds.e.nav.reward`, `ds.e.navigation.reward`, `ds.env.n.reward`, `ds.env.nav.reward`, `ds.env.navigation.reward`, `ds.environment.n.reward`, `ds.environment.nav.reward`, `ds.environment.navigation.reward`, `dynSys.e.n.reward`, `dynSys.e.nav.reward`, `dynSys.e.navigation.reward`, `dynSys.env.n.reward`, `dynSys.env.nav.reward`, `dynSys.env.navigation.reward`, `dynSys.environment.n.reward`, `dynSys.environment.nav.reward`, `dynSys.environment.navigation.reward`, `dynamicalSystem.e.n.reward`, `dynamicalSystem.e.nav.reward`, `dynamicalSystem.e.navigation.reward`, `dynamicalSystem.env.n.reward`, `dynamicalSystem.env.nav.reward`, `dynamicalSystem.env.navigation.reward`, `dynamicalSystem.environment.n.reward`, `dynamicalSystem.environment.nav.reward`, `dynamicalSystem.environment.navigation.reward`

### Builder `dynamicalSystem.environment.navigation.reward.reaching()`

`ds.e.n.reward.reaching(name; of; targetProximityRadius; targetProximityReward; collisionPenalty; distanceWeight; distanceDecreaseWeight; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `reaching[{targetProximityRadius:%.2f}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.navigation.State">State</abbr>&gt;</code> |
| `targetProximityRadius` | d | `0.05` | <code>double</code> |
| `targetProximityReward` | d | `1.0` | <code>double</code> |
| `collisionPenalty` | d | `0.01` | <code>double</code> |
| `distanceWeight` | d | `-0.01` | <code>double</code> |
| `distanceDecreaseWeight` | d | `0.1` | <code>double</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NavigationRewards.reaching()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.environment.pong`

Aliases: `ds.e.pong`, `ds.env.pong`, `ds.environment.pong`, `dynSys.e.pong`, `dynSys.env.pong`, `dynSys.environment.pong`, `dynamicalSystem.e.pong`, `dynamicalSystem.env.pong`, `dynamicalSystem.environment.pong`

### Builder `dynamicalSystem.environment.pong.numberOfCollisionsWithBall1()`

`ds.e.pong.numberOfCollisionsWithBall1(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.0f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.numberOfCollisionsWithBall1()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.numberOfCollisionsWithBall2()`

`ds.e.pong.numberOfCollisionsWithBall2(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.0f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.numberOfCollisionsWithBall2()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.score1()`

`ds.e.pong.score1(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.score1()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.score2()`

`ds.e.pong.score2(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.score2()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.scoreDiff1()`

`ds.e.pong.scoreDiff1(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.scoreDiff1()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.scoreDiff2()`

`ds.e.pong.scoreDiff2(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.scoreDiff2()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.shiftedScoreDiff1()`

`ds.e.pong.shiftedScoreDiff1(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.shiftedScoreDiff1()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.shiftedScoreDiff2()`

`ds.e.pong.shiftedScoreDiff2(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.shiftedScoreDiff2()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.yOffsetFromBall1()`

`ds.e.pong.yOffsetFromBall1(of; format; ballXProximityThreshold)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.0f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `ballXProximityThreshold` | d | `0.2` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.yOffsetFromBall1()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.pong.yOffsetFromBall2()`

`ds.e.pong.yOffsetFromBall2(of; format; ballXProximityThreshold)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiAgentTask$Step">HomogeneousBiAgentTask$Step</abbr>&lt;double[], double[], <abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongEnvironment$State">PongEnvironment$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%5.0f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `ballXProximityThreshold` | d | `0.2` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongFunctions.yOffsetFromBall2()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.environment.sequentialBoolean`

Aliases: `ds.e.sBool`, `ds.e.sequentialBoolean`, `ds.env.sBool`, `ds.env.sequentialBoolean`, `ds.environment.sBool`, `ds.environment.sequentialBoolean`, `dynSys.e.sBool`, `dynSys.e.sequentialBoolean`, `dynSys.env.sBool`, `dynSys.env.sequentialBoolean`, `dynSys.environment.sBool`, `dynSys.environment.sequentialBoolean`, `dynamicalSystem.e.sBool`, `dynamicalSystem.e.sequentialBoolean`, `dynamicalSystem.env.sBool`, `dynamicalSystem.env.sequentialBoolean`, `dynamicalSystem.environment.sBool`, `dynamicalSystem.environment.sequentialBoolean`

### Builder `dynamicalSystem.environment.sequentialBoolean.avgScore()`

`ds.e.sBool.avgScore(name; of; format; scoreType; caseIndexes)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `avg[{scoreType}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;double[]&gt;, double[], <abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction$State">SequentialBooleanFunction$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%+5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `scoreType` | e | `UNLIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils$ScoreType">BooleanUtils$ScoreType</abbr></code> |
| `caseIndexes` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.SequentialBooleanFunctions.avgScore()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.sequentialBoolean.avgScoreDelta()`

`ds.e.sBool.avgScoreDelta(name; of; format; scoreType; firstIndexes; secondIndexes)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `avg.delta[{scoreType}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;double[]&gt;, double[], <abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction$State">SequentialBooleanFunction$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%+5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `scoreType` | e | `UNLIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils$ScoreType">BooleanUtils$ScoreType</abbr></code> |
| `firstIndexes` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `secondIndexes` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.SequentialBooleanFunctions.avgScoreDelta()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.environment.sequentialBoolean.sdScore()`

`ds.e.sBool.sdScore(name; of; format; scoreType; indexes)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `avg.delta[{scoreType}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;double[]&gt;, double[], <abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction$State">SequentialBooleanFunction$State</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%+5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `scoreType` | e | `UNLIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils$ScoreType">BooleanUtils$ScoreType</abbr></code> |
| `indexes` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.SequentialBooleanFunctions.sdScore()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.function`

Aliases: `ds.f`, `ds.function`, `dynSys.f`, `dynSys.function`, `dynamicalSystem.f`, `dynamicalSystem.function`

### Builder `dynamicalSystem.function.agentStateTrajectory()`

`ds.f.agentStateTrajectory(name; of; stateF; sat; tRange; dT; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `states.in[{simulation.name}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, C&gt;</code> |
| `stateF` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;CS, double[]&gt;</code> |
| `sat` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask">SingleAgentTask</abbr>&lt;C, ?, ?, CS, ?&gt;</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `dT` | d |  | <code>double</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.SortedMap">SortedMap</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, double[]&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.agentStateTrajectory()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.asNumericalRLAgent()`

`ds.f.asNumericalRLAgent(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `as.numrl.agent` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent">NumericalReinforcementLearningAgent</abbr>&lt;S&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.asNumericalRLAgent()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.asRLAgent()`

`ds.f.asRLAgent(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `as.rl.agent` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.DynamicalSystem">DynamicalSystem</abbr>&lt;I, O, S&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent">ReinforcementLearningAgent</abbr>&lt;I, O, S&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.asRLAgent()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.biLevelHighInnerNds()`

`ds.f.biLevelHighInnerNds(name; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `high` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.BiLevelNumericalDynamicalSystem">BiLevelNumericalDynamicalSystem</abbr>&lt;SH, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;SH&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.biLevelHighInnerNds()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.biLevelLowInnerNds()`

`ds.f.biLevelLowInnerNds(name; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `low` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.BiLevelNumericalDynamicalSystem">BiLevelNumericalDynamicalSystem</abbr>&lt;?, SL&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;SL&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.biLevelLowInnerNds()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.cumulatedReward()`

`ds.f.cumulatedReward(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `cumulated.reward` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;?&gt;, ?, ?&gt;&gt;&gt;</code> |
| `format` | s | `%+6.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.cumulatedReward()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.doubleOp()`

`ds.f.doubleOp(name; of; activationF; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{activationF}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `activationF` | e | `IDENTITY` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron$ActivationFunction">MultiLayerPerceptron$ActivationFunction</abbr></code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.doubleOp()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.nonLearning()`

`ds.f.nonLearning(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `non.learning` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.rl.FrozenableRLAgent">FrozenableRLAgent</abbr>&lt;I, O, ?&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.DynamicalSystem">DynamicalSystem</abbr>&lt;I, O, ?&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.nonLearning()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.opponentBiSimulator()`

`ds.f.opponentBiSimulator(name; of; simulation; opponent; home; tRange; dT; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `opponent.sim` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, S&gt;</code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiSimulation">HomogeneousBiSimulation</abbr>&lt;S, SS, B&gt;</code> |
| `opponent` | npm |  | <code>S</code> |
| `home` | b | `true` | <code>boolean</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `dT` | d |  | <code>double</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;SS&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.opponentBiSimulator()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.params()`

`ds.f.params(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `params` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Parametrized">Parametrized</abbr>&lt;?, P&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, P&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.params()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.rewards()`

`ds.f.rewards(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `reward` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;?&gt;, ?, ?&gt;&gt;&gt;</code> |
| `format` | s | `s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.rewards()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.selfBiSimulator()`

`ds.f.selfBiSimulator(name; of; simulation; tRange; dT; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `self.sim` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, S&gt;</code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiSimulation">HomogeneousBiSimulation</abbr>&lt;S, SS, B&gt;</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `dT` | d |  | <code>double</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;SS&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.selfBiSimulator()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.simOutcome()`

`ds.f.simOutcome(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `sim.outcome` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;S&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.SortedMap">SortedMap</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, S&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.simOutcome()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.simulate()`

`ds.f.simulate(name; of; simulation; tRange; dT; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `sim[{simulation.name}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `simulation` | npm |  | <code>S</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `dT` | d |  | <code>double</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, O&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.simulate()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.stateless()`

`ds.f.stateless(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `stateless` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.FrozenableDynamicalSystem">FrozenableDynamicalSystem</abbr>&lt;I, O, ?&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.StatelessSystem">StatelessSystem</abbr>&lt;I, O&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.stateless()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.unwrappedRl()`

`ds.f.unwrappedRl(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `unwrapped.rl` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;O&gt;, A, S&gt;&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;O, A, S&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.unwrappedRl()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.function.weights()`

`ds.f.weights(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `weights` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron">MultiLayerPerceptron</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Functions.weights()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.misc`

Aliases: `ds.misc`, `dynSys.misc`, `dynamicalSystem.misc`

### Builder `dynamicalSystem.misc.simExample()`

`ds.misc.simExample(simulation)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;T, ?, ?&gt;</code> |

Produces <code>T</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Miscs.simExample()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.num`

Aliases: `ds.num`, `dynSys.num`, `dynamicalSystem.num`

### Builder `dynamicalSystem.num.composition()`

`ds.num.composition(first; second; nOfInternalIOs)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `first` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S1&gt;&gt;</code> |
| `second` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S2&gt;&gt;</code> |
| `nOfInternalIOs` | i |  | <code>int</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;S1, S2&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.composition()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.drn()`

`ds.num.drn(timeRange; innerNeuronsRatio; innerNeurons; activationFunction; threshold; timeResolution)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `timeRange` | npm | `m.range(max = 1; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `innerNeuronsRatio` | d | `1.0` | <code>double</code> |
| `innerNeurons` | i |  | <code>int</code> |
| `activationFunction` | e | `TANH` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron$ActivationFunction">MultiLayerPerceptron$ActivationFunction</abbr></code> |
| `threshold` | d | `0.1` | <code>double</code> |
| `timeResolution` | d | `0.16666` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.DelayedRecurrentNetwork">DelayedRecurrentNetwork</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.drn()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.enhanced()`

`ds.num.enhanced(windowT; inner; types)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `windowT` | d |  | <code>double</code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S&gt;&gt;</code> |
| `types` | e[] | `[CURRENT, TREND, AVG]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.EnhancedInput$Type">EnhancedInput$Type</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.EnhancedInput">EnhancedInput</abbr>&lt;S&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.enhanced()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.fromFunctions()`

`ds.num.fromFunctions(name; functions)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{functions}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `functions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction">MultivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.fromFunctions()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.hebbianMlp()`

`ds.num.hebbianMlp(innerLayers; learningRate; weightsUpdateInterval; activationFunction; initialWeightRange; maxWeightMagnitude; randomGenerator; parametrizationType; weightInitializationType)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `innerLayers` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `learningRate` | d | `0.01` | <code>double</code> |
| `weightsUpdateInterval` | i | `1` | <code>int</code> |
| `activationFunction` | e | `TANH` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron$ActivationFunction">MultiLayerPerceptron$ActivationFunction</abbr></code> |
| `initialWeightRange` | npm | `m.range(max = 0.1; min = -0.1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `maxWeightMagnitude` | d | `10.0` | <code>double</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |
| `parametrizationType` | e | `SYNAPSE` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.HebbianMultiLayerPerceptron$ParametrizationType">HebbianMultiLayerPerceptron$ParametrizationType</abbr></code> |
| `weightInitializationType` | e | `PARAMS` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.HebbianMultiLayerPerceptron$WeightInitializationType">HebbianMultiLayerPerceptron$WeightInitializationType</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.HebbianMultiLayerPerceptron">HebbianMultiLayerPerceptron</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.hebbianMlp()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.inStepped()`

`ds.num.inStepped(stepT; inner)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `stepT` | d | `1.0` | <code>double</code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.composed.Stepped$State">Stepped$State</abbr>&lt;S&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.inStepped()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.linear()`

`ds.num.linear(zeroQ)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `zeroQ` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.LinearCombination">LinearCombination</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.linear()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.mlp()`

`ds.num.mlp(innerLayerRatio; nOfInnerLayers; innerLayers; activationFunction)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `innerLayerRatio` | d | `0.65` | <code>double</code> |
| `nOfInnerLayers` | i | `0` | <code>int</code> |
| `innerLayers` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `activationFunction` | e | `TANH` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron$ActivationFunction">MultiLayerPerceptron$ActivationFunction</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron">MultiLayerPerceptron</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.mlp()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.noised()`

`ds.num.noised(inputSigma; outputSigma; randomGenerator; inner)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `inputSigma` | d | `0.01` | <code>double</code> |
| `outputSigma` | d | `0.01` | <code>double</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.Noised">Noised</abbr>&lt;S&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.noised()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.outStepped()`

`ds.num.outStepped(stepT; inner)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `stepT` | d | `1.0` | <code>double</code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.composed.Stepped$State">Stepped$State</abbr>&lt;S&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.outStepped()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.sin()`

`ds.num.sin(p; f; a; b)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `p` | npm | `m.range(max = 1.57; min = -1.57)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `f` | npm | `m.range(max = 1; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `a` | npm | `m.range(max = 1; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `b` | npm | `m.range(max = 0.5; min = -0.5)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.Sinusoidal">Sinusoidal</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.sin()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.num.stepped()`

`ds.num.stepped(stepT; inner)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `stepT` | d | `0.1` | <code>double</code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;S&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.composed.Stepped$State">Stepped$State</abbr>&lt;S&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalDynamicalSystems.stepped()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.opponent.pong`

Aliases: `ds.opponent.pong`, `dynSys.opponent.pong`, `dynamicalSystem.opponent.pong`

### Builder `dynamicalSystem.opponent.pong.simple()`

`ds.opponent.pong.simple(deltaPosition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `deltaPosition` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.pong.PongAgent">PongAgent</abbr></code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.PongOpponents.simple()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.rl.num`

Aliases: `ds.rl.num`, `dynSys.rl.num`, `dynamicalSystem.rl.num`

### Builder `dynamicalSystem.rl.num.freeFormMlp()`

`ds.rl.num.freeFormMlp(innerLayerRatio; nOfInnerLayers; innerLayers; activationFunction; historyLength; weightsUpdateInterval; initialWeightRange; randomGenerator; maxWeightMagnitude; weightInitializationType)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `innerLayerRatio` | d | `0.65` | <code>double</code> |
| `nOfInnerLayers` | i | `1` | <code>int</code> |
| `innerLayers` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `activationFunction` | e | `TANH` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron$ActivationFunction">MultiLayerPerceptron$ActivationFunction</abbr></code> |
| `historyLength` | i | `10` | <code>int</code> |
| `weightsUpdateInterval` | i | `1` | <code>int</code> |
| `initialWeightRange` | npm | `m.range(max = 0.01; min = -0.01)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |
| `maxWeightMagnitude` | d | `10.0` | <code>double</code> |
| `weightInitializationType` | e | `RANDOM` | <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.ann.HebbianMultiLayerPerceptron$WeightInitializationType">HebbianMultiLayerPerceptron$WeightInitializationType</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent">NumericalReinforcementLearningAgent</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.rl.FreeFormPlasticMLPRLAgent">FreeFormPlasticMLPRLAgent</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalRLAgents.freeFormMlp()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.rl.num.linearActorCritic()`

`ds.rl.num.linearActorCritic(name; actorLearningRate; criticLearningRate; actorWeightDecay; criticWeightDecay; discountFactor; explorationNoise; maxGradLogProb; initialWeightRange; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `lAC[alr={actorLearningRate};clr={criticLearningRate};en={explorationNoise}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `actorLearningRate` | d | `1.0E-4` | <code>double</code> |
| `criticLearningRate` | d | `0.001` | <code>double</code> |
| `actorWeightDecay` | d | `1.0E-5` | <code>double</code> |
| `criticWeightDecay` | d | `1.0E-4` | <code>double</code> |
| `discountFactor` | d | `0.99` | <code>double</code> |
| `explorationNoise` | d | `1.0` | <code>double</code> |
| `maxGradLogProb` | d | `10.0` | <code>double</code> |
| `initialWeightRange` | npm | `m.range(max = 0.2; min = -0.2)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent">NumericalReinforcementLearningAgent</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.rl.LinearActorCritic">LinearActorCritic</abbr>&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.NumericalRLAgents.linearActorCritic()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.simulation`

Aliases: `ds.s`, `ds.sim`, `ds.simulation`, `dynSys.s`, `dynSys.sim`, `dynSys.simulation`, `dynamicalSystem.s`, `dynamicalSystem.sim`, `dynamicalSystem.simulation`

### Builder `dynamicalSystem.simulation.sequential()`

`ds.s.sequential(name; relativeV)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `seq.sims` | <code><abbr title="java.lang.String">String</abbr></code> |
| `relativeV` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;? extends <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;T, S, O&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;T, S, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;S&gt;&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Simulations.sequential()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.simulation.sequentialBf()`

`ds.s.sequentialBf(name; target; cases; rewardType; resetAgent; shuffle; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `sequentialBf` | <code><abbr title="java.lang.String">String</abbr></code> |
| `target` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction">BooleanFunction</abbr></code> |
| `cases` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `rewardType` | e | `UNLIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils$ScoreType">BooleanUtils$ScoreType</abbr></code> |
| `resetAgent` | b | `false` | <code>boolean</code> |
| `shuffle` | b | `false` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction">SequentialBooleanFunction</abbr>&lt;?&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Simulations.sequentialBf()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.simulation.sequentialXor()`

`ds.s.sequentialXor(name; cases; rewardType; resetAgent; shuffle; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `sequentialXor` | <code><abbr title="java.lang.String">String</abbr></code> |
| `cases` | s[] | `[00, 01, 10, 11]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `rewardType` | e | `UNLIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils$ScoreType">BooleanUtils$ScoreType</abbr></code> |
| `resetAgent` | b | `false` | <code>boolean</code> |
| `shuffle` | b | `false` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.synthetic.SequentialXor">SequentialXor</abbr>&lt;?&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Simulations.sequentialXor()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.simulation.variableSensorPositionsNavigation()`

`ds.s.variableSensorPositionsNavigation(name; initialRobotDirectionRange; robotRadius; robotMaxV; nOfSensors; sensorRange; targetSensing; arena; rescaleInput; relativeV; sortAngles; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `vs[{nOfSensors}]-nav-{arena}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `initialRobotDirectionRange` | npm | `m.range(max = 0; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `robotRadius` | d | `0.05` | <code>double</code> |
| `robotMaxV` | d | `0.01` | <code>double</code> |
| `nOfSensors` | i | `5` | <code>int</code> |
| `sensorRange` | d | `0.5` | <code>double</code> |
| `targetSensing` | e | `LIMITED` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment$Configuration$TargetSensing">NavigationEnvironment$Configuration$TargetSensing</abbr></code> |
| `arena` | npm | `ds.arena.prepared()` | <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.NavigationArena">NavigationArena</abbr></code> |
| `rescaleInput` | b | `true` | <code>boolean</code> |
| `relativeV` | b | `false` | <code>boolean</code> |
| `sortAngles` | b | `true` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.navigation.VariableSensorPositionsNavigation">VariableSensorPositionsNavigation</abbr>&lt;?&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.Simulations.variableSensorPositionsNavigation()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.singleAgentTask`

Aliases: `ds.saTask`, `ds.sat`, `ds.singleAgentTask`, `dynSys.saTask`, `dynSys.sat`, `dynSys.singleAgentTask`, `dynamicalSystem.saTask`, `dynamicalSystem.sat`, `dynamicalSystem.singleAgentTask`

### Builder `dynamicalSystem.singleAgentTask.fromEnvironment()`

`ds.sat.fromEnvironment(name; environment; stopCondition; resetAgent)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{environment.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `environment` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Environment">Environment</abbr>&lt;O, A, ES, C&gt;</code> |
| `stopCondition` | npm | `predicate.not(condition = predicate.always())` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;ES&gt;</code> |
| `resetAgent` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask">SingleAgentTask</abbr>&lt;C, O, A, CS, ES&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.SingleAgentTasks.fromEnvironment()` by jgea-experimenter:2.8.2

### Builder `dynamicalSystem.singleAgentTask.sequential()`

`ds.sat.sequential(name; tasks; resetFirst; resetAll)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `seq.tasks` | <code><abbr title="java.lang.String">String</abbr></code> |
| `tasks` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;? extends <abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask">SingleAgentTask</abbr>&lt;C, O, A, CS, TS&gt;&gt;</code> |
| `resetFirst` | b | `false` | <code>boolean</code> |
| `resetAll` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask">SingleAgentTask</abbr>&lt;C, O, A, CS, TS&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.SingleAgentTasks.sequential()` by jgea-experimenter:2.8.2

## Package `dynamicalSystem.singleRLAgentTask`

Aliases: `ds.saRLTask`, `ds.singleRLAgentTask`, `ds.srlat`, `dynSys.saRLTask`, `dynSys.singleRLAgentTask`, `dynSys.srlat`, `dynamicalSystem.saRLTask`, `dynamicalSystem.singleRLAgentTask`, `dynamicalSystem.srlat`

### Builder `dynamicalSystem.singleRLAgentTask.fromNumericalEnvironment()`

`ds.srlat.fromNumericalEnvironment(name; environment; stopCondition; resetAgent; initialReward; reward)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{environment.name}[{reward.name}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `environment` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Environment">Environment</abbr>&lt;double[], double[], ES, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;? extends CS&gt;&gt;</code> |
| `stopCondition` | npm | `predicate.not(condition = predicate.always())` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;ES&gt;</code> |
| `resetAgent` | b | `false` | <code>boolean</code> |
| `initialReward` | d | `0.0` | <code>double</code> |
| `reward` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;ES, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.control.SingleRLAgentTask">SingleRLAgentTask</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent">NumericalReinforcementLearningAgent</abbr>&lt;? extends CS&gt;, double[], double[], CS, ES&gt;</code>; built from `io.github.ericmedvet.jsdynsym.buildable.builders.SingleRLAgentTasks.fromNumericalEnvironment()` by jgea-experimenter:2.8.2

## Package `ea`

### Builder `ea.experiment()`

`ea.experiment(name; startTime; runs; listeners)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `startTime` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `runs` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, ?, ?, ?&gt;&gt;</code> |
| `listeners` | npm[] | `[ea.l.console()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.ExecutorService">ExecutorService</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, ?, ?, ?&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, ?, ?, ?&gt;&gt;&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.experimenter.Experiment">Experiment</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.Experiment()` by jgea-experimenter:2.8.2

### Builder `ea.run()`

`ea.run(name; solver; problem; randomGenerator; nOfThreads)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `solver` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, ? extends <abbr title="io.github.ericmedvet.jgea.core.solver.AbstractPopulationBasedIterativeSolver">AbstractPopulationBasedIterativeSolver</abbr>&lt;? extends <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, P&gt;, P, ?, G, S, Q&gt;&gt;</code> |
| `problem` | npm |  | <code>P</code> |
| `randomGenerator` | npm |  | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |
| `nOfThreads` | i | `-1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.Run()` by jgea-experimenter:2.8.2

### Builder `ea.runOutcome()`

`ea.runOutcome(index; run; serializedGenotypes)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `index` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `run` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, ?, ?, ?&gt;</code> |
| `serializedGenotypes` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.experimenter.RunOutcome">RunOutcome</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.RunOutcome()` by jgea-experimenter:2.8.2

## Package `ea.accumulator`

Aliases: `ea.a`, `ea.acc`, `ea.accumulator`

### Builder `ea.accumulator.bests()`

`ea.a.bests(eFunction; listFunction)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `eFunction` | npm | `ea.f.best()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, F&gt;</code> |
| `listFunction` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;F&gt;, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Accumulators.all()` by jgea-experimenter:2.8.2

### Builder `ea.accumulator.lastBest()`

`ea.a.lastBest(function)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `function` | npm | `ea.f.best()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Accumulators.last()` by jgea-experimenter:2.8.2

### Builder `ea.accumulator.lastPopulationMap()`

`ea.a.lastPopulationMap(serializerF)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `serializerF` | npm | `f.toBase64()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Object">Object</abbr>, <abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, ?, ?, ?&gt;, <abbr title="io.github.ericmedvet.jnb.core.NamedParamMap">NamedParamMap</abbr>, <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, ?, ?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Accumulators.lastPopulationMap()` by jgea-experimenter:2.8.2

## Package `ea.biproblem.synthetic`

Aliases: `ea.biproblem.s`, `ea.biproblem.synthetic`, `ea.bp.s`, `ea.bp.synthetic`

### Builder `ea.biproblem.synthetic.boundedSumBiProblem()`

`ea.bp.s.boundedSumBiProblem(name; d; b; k)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `boundedSumBiProblem-{d}-{b}-{k}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `d` | i | `5` | <code>int</code> |
| `b` | i | `100` | <code>int</code> |
| `k` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.BoundedSumBiProblem">BoundedSumBiProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticBiProblems.boundedSumBiProblem()` by jgea-experimenter:2.8.2

### Builder `ea.biproblem.synthetic.pointAimingBiProblem()`

`ea.bp.s.pointAimingBiProblem(name; p; target)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `pointAimingBiProblem-{p}-{target}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |
| `target` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.PointAimingBiProblem">PointAimingBiProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticBiProblems.pointAimingBiProblem()` by jgea-experimenter:2.8.2

## Package `ea.comparator`

### Builder `ea.comparator.ascending()`

`ea.comparator.ascending(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super X, ? extends T&gt;</code> |

Produces <code><abbr title="java.util.Comparator">Comparator</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Comparators.ascending()` by jgea-experimenter:2.8.2

### Builder `ea.comparator.descending()`

`ea.comparator.descending(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super X, ? extends T&gt;</code> |

Produces <code><abbr title="java.util.Comparator">Comparator</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Comparators.descending()` by jgea-experimenter:2.8.2

### Builder `ea.comparator.pAscending()`

`ea.comparator.pAscending(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super X, ? extends T&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Comparators.pAscending()` by jgea-experimenter:2.8.2

### Builder `ea.comparator.pDescending()`

`ea.comparator.pDescending(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super X, ? extends T&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Comparators.pDescending()` by jgea-experimenter:2.8.2

## Package `ea.consumer`

Aliases: `ea.c`, `ea.consumer`

### Builder `ea.consumer.telegram()`

`ea.c.telegram(of; title; chatId; botIdFilePath)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, O&gt;</code> |
| `title` | s | `Experiment:
	{name}
Run {index}:
	Solver: {solver.name}
	Problem: {problem.name}
	Seed: {randomGenerator.seed}
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `chatId` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `botIdFilePath` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, ?, ?, ?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Consumers.telegram()` by jgea-experimenter:2.8.2

## Package `ea.drawer`

Aliases: `ea.d`, `ea.drawer`

### Builder `ea.drawer.formula()`

`ea.d.formula(scale; simplify)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `scale` | d | `1.0` | <code>double</code> |
| `simplify` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Drawers.formula()` by jgea-experimenter:2.8.2

### Builder `ea.drawer.polyomino()`

`ea.d.polyomino(maxW; maxH; colors; borderColor)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `maxW` | i | `0` | <code>int</code> |
| `maxH` | i | `0` | <code>int</code> |
| `colors` | npm |  | <code><abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.Character">Character</abbr>, <abbr title="java.awt.Color">Color</abbr>&gt;</code> |
| `borderColor` | npm | `ea.misc.colorByName(name = white)` | <code><abbr title="java.awt.Color">Color</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.experimenter.drawer.PolyominoDrawer">PolyominoDrawer</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Drawers.polyomino()` by jgea-experimenter:2.8.2

### Builder `ea.drawer.tree()`

`ea.d.tree(scale)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `scale` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.experimenter.drawer.TreeDrawer">TreeDrawer</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Drawers.tree()` by jgea-experimenter:2.8.2

## Package `ea.function`

Aliases: `ea.f`, `ea.function`

### Builder `ea.function.all()`

`ea.f.all(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;I&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.all()` by jgea-experimenter:2.8.2

### Builder `ea.function.archiveContents()`

`ea.f.archiveContents(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.Archive">Archive</abbr>&lt;?, ?, T&gt;&gt;</code> |
| `format` | s | `%4.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.archiveContents()` by jgea-experimenter:2.8.2

### Builder `ea.function.archiveCoverage()`

`ea.f.archiveCoverage(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.Archive">Archive</abbr>&lt;?, ?, ?&gt;&gt;</code> |
| `format` | s | `%4.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.archiveCoverage()` by jgea-experimenter:2.8.2

### Builder `ea.function.archiveToPolygons()`

`ea.f.archiveToPolygons(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.TwoDKeyArchive">TwoDKeyArchive</abbr>&lt;?, T&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.geometry.Polygon">Polygon</abbr>, T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.archiveToPolygons()` by jgea-experimenter:2.8.2

### Builder `ea.function.asDouble()`

`ea.f.asDouble(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.asDouble()` by jgea-experimenter:2.8.2

### Builder `ea.function.behavior()`

`ea.f.behavior(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.problem.BehaviorBasedProblem$Outcome">BehaviorBasedProblem$Outcome</abbr>&lt;B, ?&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, B&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.behavior()` by jgea-experimenter:2.8.2

### Builder `ea.function.behaviorQuality()`

`ea.f.behaviorQuality(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.problem.BehaviorBasedProblem$Outcome">BehaviorBasedProblem$Outcome</abbr>&lt;?, BQ&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, BQ&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.behaviorQuality()` by jgea-experimenter:2.8.2

### Builder `ea.function.best()`

`ea.f.best(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, I&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.best()` by jgea-experimenter:2.8.2

### Builder `ea.function.coMeArchive1()`

`ea.f.coMeArchive1(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMEPopulationState">CoMEPopulationState</abbr>&lt;G, ?, S, ?, ?, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive">NumericalKeyArchive</abbr>&lt;? extends <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, ? extends <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.coMeArchive1()` by jgea-experimenter:2.8.2

### Builder `ea.function.coMeArchive2()`

`ea.f.coMeArchive2(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMEPopulationState">CoMEPopulationState</abbr>&lt;?, G, ?, S, ?, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive">NumericalKeyArchive</abbr>&lt;? extends <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, ? extends <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.coMeArchive2()` by jgea-experimenter:2.8.2

### Builder `ea.function.coMeStrategy1Field()`

`ea.f.coMeStrategy1Field(of; relative)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMEPopulationState">CoMEPopulationState</abbr>&lt;?, ?, ?, ?, ?, ?, ?&gt;&gt;</code> |
| `relative` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.coMeStrategy1Field()` by jgea-experimenter:2.8.2

### Builder `ea.function.coMeStrategy2Field()`

`ea.f.coMeStrategy2Field(of; relative)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMEPopulationState">CoMEPopulationState</abbr>&lt;?, ?, ?, ?, ?, ?, ?&gt;&gt;</code> |
| `relative` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.coMeStrategy2Field()` by jgea-experimenter:2.8.2

### Builder `ea.function.computedArchive()`

`ea.f.computedArchive(of; descriptors; archive; qComparator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;I&gt;&gt;</code> |
| `descriptors` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `archive` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `qComparator` | npm | `ea.f.qualityComparator(of = ea.f.problem())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super Q&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive">NumericalKeyArchive</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.computedArchive()` by jgea-experimenter:2.8.2

### Builder `ea.function.crossEpistasis()`

`ea.f.crossEpistasis(of; startOffset; endOffset; splitOffset; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `startOffset` | i | `0` | <code>int</code> |
| `endOffset` | i | `0` | <code>int</code> |
| `splitOffset` | i | `0` | <code>int</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.crossEpistasis()` by jgea-experimenter:2.8.2

### Builder `ea.function.cumulativeFidelity()`

`ea.f.cumulativeFidelity(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, ?, ?, ?&gt;&gt;</code> |
| `format` | s | `%8.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.cumulativeFidelity()` by jgea-experimenter:2.8.2

### Builder `ea.function.elapsedSecs()`

`ea.f.elapsedSecs(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.State">State</abbr>&lt;?, ?&gt;&gt;</code> |
| `format` | s | `%6.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.elapsedSecs()` by jgea-experimenter:2.8.2

### Builder `ea.function.firsts()`

`ea.f.firsts(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;I&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.firsts()` by jgea-experimenter:2.8.2

### Builder `ea.function.fromMapper()`

`ea.f.fromMapper(of; mapper; example; name; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, G&gt;</code> |
| `mapper` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `example` | npm |  | <code>S</code> |
| `name` | s | interpolate `{mapper.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, S&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.fromMapper()` by jgea-experimenter:2.8.2

### Builder `ea.function.fromProblem()`

`ea.f.fromProblem(of; problem; name; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, S&gt;</code> |
| `problem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;</code> |
| `name` | s | interpolate `{problem.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.fromProblem()` by jgea-experimenter:2.8.2

### Builder `ea.function.genotype()`

`ea.f.genotype(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, ?, ?&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, G&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.genotype()` by jgea-experimenter:2.8.2

### Builder `ea.function.globalProgress()`

`ea.f.globalProgress(name; l)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `global.progress` | <code><abbr title="java.lang.String">String</abbr></code> |
| `l` | i | `8` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;<abbr title="java.lang.Object">Object</abbr>, <abbr title="io.github.ericmedvet.jgea.core.util.TextPlotter$Miniplot">TextPlotter$Miniplot</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.globalProgress()` by jgea-experimenter:2.8.2

### Builder `ea.function.globalProgressRate()`

`ea.f.globalProgressRate(name; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `global.progress.rate` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%4.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;<abbr title="java.lang.Object">Object</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.globalProgressRate()` by jgea-experimenter:2.8.2

### Builder `ea.function.hist()`

`ea.f.hist(nOfBins; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `nOfBins` | i | `8` | <code>int</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.util.TextPlotter$Miniplot">TextPlotter$Miniplot</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.hist()` by jgea-experimenter:2.8.2

### Builder `ea.function.hypervolume2D()`

`ea.f.hypervolume2D(minReference; maxReference; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `minReference` | d[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `maxReference` | d[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `format` | s | `%.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.hypervolume2D()` by jgea-experimenter:2.8.2

### Builder `ea.function.id()`

`ea.f.id(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;?, ?, ?&gt;&gt;</code> |
| `format` | s | `%6d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Long">Long</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.id()` by jgea-experimenter:2.8.2

### Builder `ea.function.isCrossRedundancy()`

`ea.f.isCrossRedundancy(of; startOffset; endOffset; splitOffset; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `startOffset` | i | `0` | <code>int</code> |
| `endOffset` | i | `0` | <code>int</code> |
| `splitOffset` | i | `0` | <code>int</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isCrossRedundancy()` by jgea-experimenter:2.8.2

### Builder `ea.function.isLb()`

`ea.f.isLb(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `is.lower.bound` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isLb()` by jgea-experimenter:2.8.2

### Builder `ea.function.isParameter()`

`ea.f.isParameter(name; of; parameter; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `is.{parameter}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `parameter` | s | `size` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isParameter()` by jgea-experimenter:2.8.2

### Builder `ea.function.isRedundancy()`

`ea.f.isRedundancy(of; startOffset; endOffset; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `startOffset` | i | `0` | <code>int</code> |
| `endOffset` | i | `0` | <code>int</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isRedundancy()` by jgea-experimenter:2.8.2

### Builder `ea.function.isToBoundedSumPhenotype()`

`ea.f.isToBoundedSumPhenotype(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `bounded.sum.phenotype` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isToBoundedSumPhenotype()` by jgea-experimenter:2.8.2

### Builder `ea.function.isToList()`

`ea.f.isToList(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `is.list` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isToList()` by jgea-experimenter:2.8.2

### Builder `ea.function.isUb()`

`ea.f.isUb(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `is.upper.bound` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.isUb()` by jgea-experimenter:2.8.2

### Builder `ea.function.lasts()`

`ea.f.lasts(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;I&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.lasts()` by jgea-experimenter:2.8.2

### Builder `ea.function.maMeArchive()`

`ea.f.maMeArchive(of; n)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MAMEPopulationState">MAMEPopulationState</abbr>&lt;G, S, Q, ?&gt;&gt;</code> |
| `n` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive">NumericalKeyArchive</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.maMeArchive()` by jgea-experimenter:2.8.2

### Builder `ea.function.meArchive()`

`ea.f.meArchive(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState">MEPopulationState</abbr>&lt;G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive">NumericalKeyArchive</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.meArchive()` by jgea-experimenter:2.8.2

### Builder `ea.function.mfMeFidelityArchive()`

`ea.f.mfMeFidelityArchive(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState">MultiFidelityMEPopulationState</abbr>&lt;G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive">NumericalKeyArchive</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState$LocalState">MultiFidelityMEPopulationState$LocalState</abbr>&lt;G, S, Q&gt;, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState$LocalState">MultiFidelityMEPopulationState$LocalState</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.mfMeFidelityArchive()` by jgea-experimenter:2.8.2

### Builder `ea.function.mfMeLsCumulativeFidelity()`

`ea.f.mfMeLsCumulativeFidelity(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState$LocalState">MultiFidelityMEPopulationState$LocalState</abbr>&lt;?, ?, ?&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.mfMeLsCumulativeFidelity()` by jgea-experimenter:2.8.2

### Builder `ea.function.mfMeLsFidelity()`

`ea.f.mfMeLsFidelity(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState$LocalState">MultiFidelityMEPopulationState$LocalState</abbr>&lt;?, ?, ?&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.mfMeLsFidelity()` by jgea-experimenter:2.8.2

### Builder `ea.function.mfMeLsNOfEvals()`

`ea.f.mfMeLsNOfEvals(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState$LocalState">MultiFidelityMEPopulationState$LocalState</abbr>&lt;?, ?, ?&gt;&gt;</code> |
| `format` | s | `%4d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Long">Long</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.mfMeLsNOfEvals()` by jgea-experimenter:2.8.2

### Builder `ea.function.mids()`

`ea.f.mids(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;I&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.mids()` by jgea-experimenter:2.8.2

### Builder `ea.function.nOfBirths()`

`ea.f.nOfBirths(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, ?, ?, ?&gt;&gt;</code> |
| `format` | s | `%5d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Long">Long</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.nOfBirths()` by jgea-experimenter:2.8.2

### Builder `ea.function.nOfEvals()`

`ea.f.nOfEvals(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, ?, ?, ?&gt;&gt;</code> |
| `format` | s | `%5d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Long">Long</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.nOfEvals()` by jgea-experimenter:2.8.2

### Builder `ea.function.nOfIterations()`

`ea.f.nOfIterations(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.State">State</abbr>&lt;?, ?&gt;&gt;</code> |
| `format` | s | `%4d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Long">Long</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.nOfIterations()` by jgea-experimenter:2.8.2

### Builder `ea.function.numExpr()`

`ea.f.numExpr(name; of; args; expr; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `[{expr}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, Z&gt;</code> |
| `args` | npm[] | `[f.identity()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;Z, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `expr` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.numExpr()` by jgea-experimenter:2.8.2

### Builder `ea.function.overallTargetDistance()`

`ea.f.overallTargetDistance(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, S, ?, P&gt;&gt;</code> |
| `format` | s | `%.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.overallTargetDistance()` by jgea-experimenter:2.8.2

### Builder `ea.function.parentIds()`

`ea.f.parentIds(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;?, ?, ?&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;<abbr title="java.lang.Long">Long</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.parentIds()` by jgea-experimenter:2.8.2

### Builder `ea.function.popTargetDistances()`

`ea.f.popTargetDistances(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, S, ?, P&gt;&gt;</code> |
| `format` | s | `%.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.popTargetDistances()` by jgea-experimenter:2.8.2

### Builder `ea.function.problem()`

`ea.f.problem(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.State">State</abbr>&lt;P, S&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, P&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.problem()` by jgea-experimenter:2.8.2

### Builder `ea.function.progress()`

`ea.f.progress(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.State">State</abbr>&lt;?, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.util.Progress">Progress</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.progress()` by jgea-experimenter:2.8.2

### Builder `ea.function.quality()`

`ea.f.quality(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;?, ?, Q&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.quality()` by jgea-experimenter:2.8.2

### Builder `ea.function.qualityComparator()`

`ea.f.qualityComparator(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;?, Q&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.qualityComparator()` by jgea-experimenter:2.8.2

### Builder `ea.function.qualityFunction()`

`ea.f.qualityFunction(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, P&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.function.Function">Function</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.qualityFunction()` by jgea-experimenter:2.8.2

### Builder `ea.function.rate()`

`ea.f.rate(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.util.Progress">Progress</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.rate()` by jgea-experimenter:2.8.2

### Builder `ea.function.runs()`

`ea.f.runs(of; name; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.experimenter.Experiment">Experiment</abbr>&gt;</code> |
| `name` | s | `runs` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, ?, ?, ?&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.runs()` by jgea-experimenter:2.8.2

### Builder `ea.function.simplifiedBTree()`

`ea.f.simplifiedBTree(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `simplified` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element">Element</abbr>&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element">Element</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.simplifiedBTree()` by jgea-experimenter:2.8.2

### Builder `ea.function.simplifiedSrTree()`

`ea.f.simplifiedSrTree(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `simplified` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.simplifiedSrTree()` by jgea-experimenter:2.8.2

### Builder `ea.function.size()`

`ea.f.size(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code> |
| `format` | s | `%d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.size()` by jgea-experimenter:2.8.2

### Builder `ea.function.solution()`

`ea.f.solution(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;?, S, ?&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, S&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.solution()` by jgea-experimenter:2.8.2

### Builder `ea.function.srTreeShortVarName()`

`ea.f.srTreeShortVarName(name; of; format; map; findRegex; replaceExpr)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `var.name.change` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code> |
| `format` | s | `s` | <code><abbr title="java.lang.String">String</abbr></code> |
| `map` | npm | `m.sMap()` | <code><abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `findRegex` | s | `(([a-zA-Z])[a-z]++_?)` | <code><abbr title="java.lang.String">String</abbr></code> |
| `replaceExpr` | s | `$2` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.srTreeVarNameChange()` by jgea-experimenter:2.8.2

### Builder `ea.function.srTreeVarNameChange()`

`ea.f.srTreeVarNameChange(name; of; format; map; findRegex; replaceExpr)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `var.name.change` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code> |
| `format` | s | `s` | <code><abbr title="java.lang.String">String</abbr></code> |
| `map` | npm | `m.sMap()` | <code><abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `findRegex` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `replaceExpr` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.srTreeVarNameChange()` by jgea-experimenter:2.8.2

### Builder `ea.function.stateGrid()`

`ea.f.stateGrid(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState">GridPopulationState</abbr>&lt;G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.stateGrid()` by jgea-experimenter:2.8.2

### Builder `ea.function.supplied()`

`ea.f.supplied(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.function.Supplier">Supplier</abbr>&lt;Z&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, Z&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.supplied()` by jgea-experimenter:2.8.2

### Builder `ea.function.targetDistances()`

`ea.f.targetDistances(problem; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `problem` | npm |  | <code>P</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;?, S, ?&gt;&gt;</code> |
| `format` | s | `%.2f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.targetDistances()` by jgea-experimenter:2.8.2

### Builder `ea.function.toDoubleString()`

`ea.f.toDoubleString(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, Z&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.toDoubleString()` by jgea-experimenter:2.8.2

### Builder `ea.function.treeHeight()`

`ea.f.treeHeight(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;C&gt;&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.treeHeight()` by jgea-experimenter:2.8.2

### Builder `ea.function.treeLabels()`

`ea.f.treeLabels(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;C&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;C&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.treeLabels()` by jgea-experimenter:2.8.2

### Builder `ea.function.treeLeafLabels()`

`ea.f.treeLeafLabels(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;C&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;C&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.treeLeafLabels()` by jgea-experimenter:2.8.2

### Builder `ea.function.treeSize()`

`ea.f.treeSize(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;C&gt;&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.treeSize()` by jgea-experimenter:2.8.2

### Builder `ea.function.ttpnDeadGatesRate()`

`ea.f.ttpnDeadGatesRate(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network">Network</abbr>&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.ttpnDeadGatesRate()` by jgea-experimenter:2.8.2

### Builder `ea.function.ttpnDeadOrIUnwiredOutputGatesRate()`

`ea.f.ttpnDeadOrIUnwiredOutputGatesRate(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network">Network</abbr>&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.ttpnDeadOrIUnwiredOutputGatesRate()` by jgea-experimenter:2.8.2

### Builder `ea.function.ttpnNOfTypes()`

`ea.f.ttpnNOfTypes(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network">Network</abbr>&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.ttpnNOfTypes()` by jgea-experimenter:2.8.2

### Builder `ea.function.uniformSample()`

`ea.f.uniformSample(of; name; ranges; nOfPoints)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction">MultivariateRealFunction</abbr>&gt;</code> |
| `name` | s | interpolate `uniform.sample` | <code><abbr title="java.lang.String">String</abbr></code> |
| `ranges` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr>&gt;</code> |
| `nOfPoints` | i | `10` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.uniformSample()` by jgea-experimenter:2.8.2

### Builder `ea.function.validationQuality()`

`ea.f.validationQuality(of; individual; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, S, Q, P&gt;&gt;</code> |
| `individual` | npm | `ea.f.best()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, ?, S, Q, P&gt;, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;?, S, Q&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.validationQuality()` by jgea-experimenter:2.8.2

### Builder `ea.function.validationQualityFunction()`

`ea.f.validationQualityFunction(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, P&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.function.Function">Function</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Functions.validationQualityFunction()` by jgea-experimenter:2.8.2

## Package `ea.grammar.grid`

### Builder `ea.grammar.grid.bundled()`

`ea.grammar.grid.bundled(name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar">GridGrammar</abbr>&lt;<abbr title="java.lang.Character">Character</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GridGrammars.bundled()` by jgea-experimenter:2.8.2

### Builder `ea.grammar.grid.fromFile()`

`ea.grammar.grid.fromFile(path)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `path` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar">GridGrammar</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GridGrammars.fromFile()` by jgea-experimenter:2.8.2

## Package `ea.grammar.string`

### Builder `ea.grammar.string.booleanRegression()`

`ea.grammar.string.booleanRegression(operators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `operators` | e[] | `[and, or, not]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element$Operator">Element$Operator</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StringGrammars.booleanRegression()` by jgea-experimenter:2.8.2

### Builder `ea.grammar.string.bundled()`

`ea.grammar.string.bundled(name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StringGrammars.bundled()` by jgea-experimenter:2.8.2

### Builder `ea.grammar.string.fromFile()`

`ea.grammar.string.fromFile(path)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `path` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StringGrammars.fromFile()` by jgea-experimenter:2.8.2

### Builder `ea.grammar.string.fromProblem()`

`ea.grammar.string.fromProblem(problem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `problem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammarBasedProblem">StringGrammarBasedProblem</abbr>&lt;N, ?&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;N&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StringGrammars.fromProblem()` by jgea-experimenter:2.8.2

### Builder `ea.grammar.string.regression()`

`ea.grammar.string.regression(constants; operators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `constants` | d[] | `[0.1, 1.0, 10.0]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `operators` | e[] | `[+, -, *, p÷, plog]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element$Operator">Element$Operator</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StringGrammars.regression()` by jgea-experimenter:2.8.2

### Builder `ea.grammar.string.simpleFixedArity()`

`ea.grammar.string.simpleFixedArity(arity)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `arity` | i |  | <code>int</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StringGrammars.simpleFixedArity()` by jgea-experimenter:2.8.2

## Package `ea.listener`

Aliases: `ea.l`, `ea.listener`

### Builder `ea.listener.allCsv()`

`ea.l.allCsv(splitter; inner; deferred; onlyLast; eCondition; kCondition; splitterF; defaultOEFunctions; defaultIEFunctions; oeFunctions; ieFunctions; kFunctions; path; errorString; intFormat; doubleFormat)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `splitter` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;OE, <abbr title="java.util.Collection">Collection</abbr>&lt;IE&gt;&gt;</code> |
| `inner` | npm | `listener.csv(defaultEFunctions = [f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfIterations()); f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfEvals()); f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfBirths()); f.composition(of = f.pairFirst(name = identity); then = ea.f.elapsedSecs()); f.composition(of = f.pairSecond(name = identity); then = ea.f.id())]; defaultKFunctions = [f.mappableKey(key = "problem.name"); f.mappableKey(key = "solver.name"); f.mappableKey(key = "randomGenerator.seed")]; doubleFormat = "%.5e"; eFunctions = []; errorString = NA; intFormat = "%d"; kFunctions = []; path = null)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;OE, IE&gt;, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;OE&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `splitterF` | npm | `ea.f.all()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `defaultOEFunctions` | npm[] | `[
  ea.f.nOfIterations();
  ea.f.nOfEvals();
  ea.f.nOfBirths();
  ea.f.elapsedSecs()
]
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `defaultIEFunctions` | npm[] | `[
  ea.f.id()
]
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `oeFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `ieFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `errorString` | s | `NA` | <code><abbr title="java.lang.String">String</abbr></code> |
| `intFormat` | s | `%d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `doubleFormat` | s | `%.5e` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;OE, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.splitPair()` by jgea-experimenter:2.8.2

### Builder `ea.listener.console()`

`ea.l.console(defaultEFunctions; eFunctions; defaultKFunctions; kFunctions; deferred; onlyLast; eCondition; kCondition; logExceptions)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `defaultEFunctions` | npm[] | `[ea.f.nOfIterations(), ea.f.nOfEvals(), ea.f.nOfBirths(), ea.f.elapsedSecs(), f.size(of = ea.f.all()), f.size(of = ea.f.firsts()), f.size(of = ea.f.lasts()), f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `eFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `defaultKFunctions` | npm[] | `[ea.f.globalProgress(), f.mappableKey(key = "problem.name"), f.mappableKey(key = "solver.name"), f.mappableKey(key = "randomGenerator.seed")]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `logExceptions` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.console()` by jgea-experimenter:2.8.2

### Builder `ea.listener.csv()`

`ea.l.csv(defaultEFunctions; eFunctions; defaultKFunctions; kFunctions; deferred; onlyLast; eCondition; kCondition; path; errorString; intFormat; doubleFormat)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `defaultEFunctions` | npm[] | `[ea.f.nOfIterations(), ea.f.nOfEvals(), ea.f.nOfBirths(), ea.f.elapsedSecs(), f.size(of = ea.f.all()), f.size(of = ea.f.firsts()), f.size(of = ea.f.lasts()), f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all())), ea.f.id(of = ea.f.best())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `eFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `defaultKFunctions` | npm[] | `[f.mappableKey(key = "problem.name"), f.mappableKey(key = "solver.name"), f.mappableKey(key = "randomGenerator.seed")]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `errorString` | s | `NA` | <code><abbr title="java.lang.String">String</abbr></code> |
| `intFormat` | s | `%d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `doubleFormat` | s | `%.5e` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.csv()` by jgea-experimenter:2.8.2

### Builder `ea.listener.firstsCsv()`

`ea.l.firstsCsv(splitter; inner; deferred; onlyLast; eCondition; kCondition; splitterF; defaultOEFunctions; defaultIEFunctions; oeFunctions; ieFunctions; kFunctions; path; errorString; intFormat; doubleFormat)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `splitter` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;OE, <abbr title="java.util.Collection">Collection</abbr>&lt;IE&gt;&gt;</code> |
| `inner` | npm | `listener.csv(defaultEFunctions = [f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfIterations()); f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfEvals()); f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfBirths()); f.composition(of = f.pairFirst(name = identity); then = ea.f.elapsedSecs()); f.composition(of = f.pairSecond(name = identity); then = ea.f.id())]; defaultKFunctions = [f.mappableKey(key = "problem.name"); f.mappableKey(key = "solver.name"); f.mappableKey(key = "randomGenerator.seed")]; doubleFormat = "%.5e"; eFunctions = []; errorString = NA; intFormat = "%d"; kFunctions = []; path = null)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;OE, IE&gt;, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;OE&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `splitterF` | npm | `ea.f.firsts()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `defaultOEFunctions` | npm[] | `[
  ea.f.nOfIterations();
  ea.f.nOfEvals();
  ea.f.nOfBirths();
  ea.f.elapsedSecs()
]
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `defaultIEFunctions` | npm[] | `[
  ea.f.id()
]
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `oeFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `ieFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `errorString` | s | `NA` | <code><abbr title="java.lang.String">String</abbr></code> |
| `intFormat` | s | `%d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `doubleFormat` | s | `%.5e` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;OE, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.splitPair()` by jgea-experimenter:2.8.2

### Builder `ea.listener.individualCsv()`

`ea.l.individualCsv(splitter; inner; deferred; onlyLast; eCondition; kCondition; splitterF; defaultOEFunctions; defaultIEFunctions; oeFunctions; ieFunctions; kFunctions; path; errorString; intFormat; doubleFormat)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `splitter` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;OE, <abbr title="java.util.Collection">Collection</abbr>&lt;IE&gt;&gt;</code> |
| `inner` | npm | `listener.csv(defaultEFunctions = [f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfIterations()); f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfEvals()); f.composition(of = f.pairFirst(name = identity); then = ea.f.nOfBirths()); f.composition(of = f.pairFirst(name = identity); then = ea.f.elapsedSecs()); f.composition(of = f.pairSecond(name = identity); then = ea.f.id())]; defaultKFunctions = [f.mappableKey(key = "problem.name"); f.mappableKey(key = "solver.name"); f.mappableKey(key = "randomGenerator.seed")]; doubleFormat = "%.5e"; eFunctions = []; errorString = NA; intFormat = "%d"; kFunctions = []; path = null)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;OE, IE&gt;, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;OE&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `splitterF` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `defaultOEFunctions` | npm[] | `[
  ea.f.nOfIterations();
  ea.f.nOfEvals();
  ea.f.nOfBirths();
  ea.f.elapsedSecs()
]
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `defaultIEFunctions` | npm[] | `[
  ea.f.id()
]
` | <code><abbr title="java.lang.String">String</abbr></code> |
| `oeFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `ieFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `errorString` | s | `NA` | <code><abbr title="java.lang.String">String</abbr></code> |
| `intFormat` | s | `%d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `doubleFormat` | s | `%.5e` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;OE, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.splitPair()` by jgea-experimenter:2.8.2

### Builder `ea.listener.net()`

`ea.l.net(defaultFunctions; functions; defaultRunFunctions; runFunctions; serverAddress; serverPort; serverKeyFilePath; pollInterval; runCondition; stateCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `defaultFunctions` | npm[] | `[ea.f.nOfIterations(), ea.f.nOfEvals(), ea.f.nOfBirths(), ea.f.elapsedSecs(), f.size(of = ea.f.all()), f.size(of = ea.f.firsts()), f.size(of = ea.f.lasts()), f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;, ?&gt;&gt;</code> |
| `functions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;, ?&gt;&gt;</code> |
| `defaultRunFunctions` | npm[] | `[f.mappableKey(key = "problem.name"), f.mappableKey(key = "solver.name"), f.mappableKey(key = "randomGenerator.seed")]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;, ?&gt;&gt;</code> |
| `runFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;, ?&gt;&gt;</code> |
| `serverAddress` | s | `127.0.0.1` | <code><abbr title="java.lang.String">String</abbr></code> |
| `serverPort` | i | `10979` | <code>int</code> |
| `serverKeyFilePath` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `pollInterval` | d | `1.0` | <code>double</code> |
| `runCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;&gt;</code> |
| `stateCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Listeners.net()` by jgea-experimenter:2.8.2

### Builder `ea.listener.saveForExp()`

`ea.l.saveForExp(of; preprocessor; consumers; deferred; eCondition; kCondition; path; processor; overwrite; verbose)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(overwrite = false; path = null; verbose = false)]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `processor` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.saveForRun()`

`ea.l.saveForRun(of; preprocessor; consumers; deferred; eCondition; kCondition; path; overwrite; processor; verbose)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(overwrite = false; path = null; verbose = false)]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `processor` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onKDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.saveLastPopulationForRun()`

`ea.l.saveLastPopulationForRun(of; preprocessor; consumers; deferred; eCondition; kCondition; path; overwrite; processor; verbose)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.acc.lastPopulationMap()` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(overwrite = false; path = null; verbose = false)]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s | `run-{index:%04d}-last-pop` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `processor` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onKDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.savePlotAndCsvForExp()`

`ea.l.savePlotAndCsvForExp(of; preprocessor; consumers; deferred; eCondition; kCondition; secondary; overwrite; path; plot; type; configuration)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `null` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(of = viz.f.imagePlotter(configuration = viz.plot.configuration.image(); secondary = false; type = png); overwrite = false; path = null), consumer.saver(of = viz.f.csvPlotter(); overwrite = false; path = null; suffix = ".tsv")]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `secondary` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `plot` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.savePlotAndCsvForRun()`

`ea.l.savePlotAndCsvForRun(of; preprocessor; consumers; deferred; eCondition; kCondition; secondary; overwrite; path; plot; type; configuration; verbose)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `null` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(of = viz.f.imagePlotter(configuration = viz.plot.configuration.image(); secondary = false; type = png); overwrite = false; path = null; verbose = false), consumer.saver(of = viz.f.csvPlotter(); overwrite = false; path = null; suffix = ".tsv"; verbose = false)]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `secondary` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `plot` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onKDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.savePlotForExp()`

`ea.l.savePlotForExp(of; preprocessor; consumers; deferred; eCondition; kCondition; path; processor; overwrite; verbose; plot; secondary; type; configuration)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `null` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(overwrite = false; path = null; verbose = false)]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `processor` | npm | `viz.f.imagePlotter(configuration = viz.plot.configuration.image(); secondary = false; type = png)` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `plot` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `secondary` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.savePlotForRun()`

`ea.l.savePlotForRun(of; preprocessor; consumers; deferred; eCondition; kCondition; path; overwrite; processor; verbose; secondary; plot; type; configuration)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `null` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `null` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[consumer.saver(overwrite = false; path = null; verbose = false)]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `overwrite` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `processor` | npm | `viz.f.imagePlotter(configuration = viz.plot.configuration.image(); secondary = false; type = png)` | <code><abbr title="java.lang.String">String</abbr></code> |
| `verbose` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `secondary` | b | `false` | <code><abbr title="java.lang.String">String</abbr></code> |
| `plot` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onKDone()` by jgea-experimenter:2.8.2

### Builder `ea.listener.tui()`

`ea.l.tui(defaultFunctions; functions; defaultRunFunctions; runFunctions; runCondition; stateCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `defaultFunctions` | npm[] | `[ea.f.nOfIterations(), ea.f.nOfEvals(), ea.f.nOfBirths(), ea.f.elapsedSecs(), f.size(of = ea.f.all()), f.size(of = ea.f.firsts()), f.size(of = ea.f.lasts()), f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;, ?&gt;&gt;</code> |
| `functions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;, ?&gt;&gt;</code> |
| `defaultRunFunctions` | npm[] | `[f.mappableKey(key = "problem.name"), f.mappableKey(key = "solver.name"), f.mappableKey(key = "randomGenerator.seed")]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;, ?&gt;&gt;</code> |
| `runFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;, ?&gt;&gt;</code> |
| `runCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;&gt;</code> |
| `stateCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;?, G, S, Q, ?&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, G, S, Q&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Listeners.tui()` by jgea-experimenter:2.8.2

## Package `ea.mapper`

Aliases: `ea.m`, `ea.mapper`

### Builder `ea.mapper.aggregatedInputNds()`

`ea.m.aggregatedInputNds(of; types; windowT)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |
| `types` | e[] | `[CURRENT, TREND]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.AggregatedInput$Type">AggregatedInput$Type</abbr>&gt;</code> |
| `windowT` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.aggregatedInputNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.bsToGeCfgTree()`

`ea.m.bsToGeCfgTree(name; of; grammar; nOfCodons; codonLengthRate; maxWraps)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `cfg.tree[ge;{nOfCodons}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;</code> |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;L&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;L&gt;&gt;</code> |
| `nOfCodons` | i | `256` | <code>int</code> |
| `codonLengthRate` | d | `1.0` | <code>double</code> |
| `maxWraps` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;L&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.bsToGeCfgTree()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.bsToGrammarGrid()`

`ea.m.bsToGrammarGrid(of; grammar; l; overwrite; criteria)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;</code> |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar">GridGrammar</abbr>&lt;T&gt;&gt;</code> |
| `l` | i | `256` | <code>int</code> |
| `overwrite` | b | `false` | <code>boolean</code> |
| `criteria` | e[] | `[LEAST_RECENT, LOWEST_Y, LOWEST_X]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.StandardGridDeveloper$SortingCriterion">StandardGridDeveloper$SortingCriterion</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.bsToGrammarGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.bsToHgeCfgTree()`

`ea.m.bsToHgeCfgTree(name; of; grammar; recursive; length)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `cfg.tree[hge;{length}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;</code> |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;L&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;L&gt;&gt;</code> |
| `recursive` | b | `true` | <code>boolean</code> |
| `length` | i | `1024` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;L&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.bsToHgeCfgTree()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.bsToWhgeCfgTree()`

`ea.m.bsToWhgeCfgTree(name; of; grammar; recursive; expressivenessDepth; weightOptions; weightChildren; length)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `cfg.tree[whge;{length};exp={expressivenessDepth}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;</code> |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;L&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;L&gt;&gt;</code> |
| `recursive` | b | `true` | <code>boolean</code> |
| `expressivenessDepth` | i | `3` | <code>int</code> |
| `weightOptions` | b | `false` | <code>boolean</code> |
| `weightChildren` | b | `true` | <code>boolean</code> |
| `length` | i | `1024` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;L&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.bsToWhgeCfgTree()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.cfgTreeToMultiBTree()`

`ea.m.cfgTreeToMultiBTree(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element">Element</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.cfgTreeToMultiBTree()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.cfgTreeToPb()`

`ea.m.cfgTreeToPb(of; problem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;N&gt;&gt;</code> |
| `problem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammarBasedProblem">StringGrammarBasedProblem</abbr>&lt;N, S&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, S&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.cfgTreeToPb()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.cfgTreeToSrTree()`

`ea.m.cfgTreeToSrTree(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.cfgTreeToSrTree()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.composedNds()`

`ea.m.composedNds(of; then; nOfInternalIOs)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |
| `then` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |
| `nOfInternalIOs` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.composedNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToBitString()`

`ea.m.dsToBitString(of; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |
| `t` | d | `0.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.dsToBitString()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToDa()`

`ea.m.dsToDa(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, double[]&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.dsToDa()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToFillingRateGrid()`

`ea.m.dsToFillingRateGrid(of; rate; negItem; posItem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |
| `rate` | d | `0.25` | <code>double</code> |
| `negItem` | npm |  | <code>T</code> |
| `posItem` | npm |  | <code>T</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.dsToFillingRateGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToGrammarGrid()`

`ea.m.dsToGrammarGrid(of; grammar; l; overwrite; criteria)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar">GridGrammar</abbr>&lt;T&gt;&gt;</code> |
| `l` | i | `256` | <code>int</code> |
| `overwrite` | b | `false` | <code>boolean</code> |
| `criteria` | e[] | `[LEAST_RECENT, LOWEST_Y, LOWEST_X]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.StandardGridDeveloper$SortingCriterion">StandardGridDeveloper$SortingCriterion</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.dsToGrammarGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToIs()`

`ea.m.dsToIs(of; range)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |
| `range` | npm | `ds.range(max = 1; min = -1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.dsToIs()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToParametrized()`

`ea.m.dsToParametrized(of; parametrized; innerOf)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.mapper.dsToDa(of = ea.m.identity())` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, P&gt;</code> |
| `parametrized` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, Y&gt;</code> |
| `innerOf` | npm | `ea.m.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, E&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.parametrized()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.dsToThresholdedGrid()`

`ea.m.dsToThresholdedGrid(of; t; negItem; posItem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |
| `t` | d | `0.0` | <code>double</code> |
| `negItem` | npm |  | <code>T</code> |
| `posItem` | npm |  | <code>T</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.dsToThresholdedGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.enhancedNds()`

`ea.m.enhancedNds(of; windowT; types)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |
| `windowT` | d |  | <code>double</code> |
| `types` | e[] | `[CURRENT, TREND, AVG]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.EnhancedInput$Type">EnhancedInput$Type</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.enhancedNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.fGraphToNmrf()`

`ea.m.fGraphToNmrf(of; postOperator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.Graph">Graph</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Node">Node</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code> |
| `postOperator` | npm | `ds.f.doubleOp(activationF = identity)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.fGraphToNmrf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.gridToGrid()`

`ea.m.gridToGrid(name; of; mapper; predicate; defaultElement; onlyLargestConnected)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `cell.map[m={mapper}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;T, K&gt;</code> |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;K, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `defaultElement` | npm | `m.nullValue()` | <code>K</code> |
| `onlyLargestConnected` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;K&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.gridToGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.identity()`

`ea.m.identity()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.identity()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.isIndexed()`

`ea.m.isIndexed(of; relativeLength)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;T&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code> |
| `relativeLength` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.isIndexed()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.isToGrammarGrid()`

`ea.m.isToGrammarGrid(of; grammar; upperBound; l; overwrite; criteria)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.GridGrammar">GridGrammar</abbr>&lt;T&gt;&gt;</code> |
| `upperBound` | i | `16` | <code>int</code> |
| `l` | i | `256` | <code>int</code> |
| `overwrite` | b | `false` | <code>boolean</code> |
| `criteria` | e[] | `[LEAST_RECENT, LOWEST_Y, LOWEST_X]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.grammar.grid.StandardGridDeveloper$SortingCriterion">StandardGridDeveloper$SortingCriterion</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.isToGrammarGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.isToSGrid()`

`ea.m.isToSGrid(of; maxW; maxH; nullItem; items)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `maxW` | i | `-1` | <code>int</code> |
| `maxH` | i | `-1` | <code>int</code> |
| `nullItem` | b | `true` | <code>boolean</code> |
| `items` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.isToSGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.isToString()`

`ea.m.isToString(of; alphabet)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;</code> |
| `alphabet` | s | `abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.isToString()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.multiBTreeToBf()`

`ea.m.multiBTreeToBf(name; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `bf` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element">Element</abbr>&gt;&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction">BooleanFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.multiBTreeToBf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.multiSrTreeToNmrf()`

`ea.m.multiSrTreeToNmrf(of; simplify; postOperator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;&gt;</code> |
| `simplify` | b | `false` | <code>boolean</code> |
| `postOperator` | npm | `ds.f.doubleOp(activationF = identity)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.multiSrTreeToNmrf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.ndsPairToBiLevelNds()`

`ea.m.ndsPairToBiLevelNds(name; of; nOfHighOutputs; highPeriod; highIndexes; lowIndexes; averageEnabled)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `bi.level` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;&gt;</code> |
| `nOfHighOutputs` | i |  | <code>int</code> |
| `highPeriod` | i |  | <code>int</code> |
| `highIndexes` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `lowIndexes` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `averageEnabled` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.ndsPairToBiLevelNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.ndsToNrla()`

`ea.m.ndsToNrla(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent">NumericalReinforcementLearningAgent</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.ndsToNrla()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.nmrfToGrid()`

`ea.m.nmrfToGrid(of; items)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code> |
| `items` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;T&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.nmrfToGrid()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.nmrfToMrca()`

`ea.m.nmrfToMrca(of; nOfAdditionalChannels; kernels; initializer; range; additiveCoefficient; alivenessThreshold; toroidal)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code> |
| `nOfAdditionalChannels` | i | `1` | <code>int</code> |
| `kernels` | e[] | `[IDENTITY, LAPLACIAN, SOBEL_EDGES]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton$Kernel">MultivariateRealGridCellularAutomaton$Kernel</abbr>&gt;</code> |
| `initializer` | e | `CENTER_ALL` | <code><abbr title="io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton$Initializer">MultivariateRealGridCellularAutomaton$Initializer</abbr></code> |
| `range` | npm | `m.range(max = 1; min = -1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `additiveCoefficient` | d | `1.0` | <code>double</code> |
| `alivenessThreshold` | d | `0.0` | <code>double</code> |
| `toroidal` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton">MultivariateRealGridCellularAutomaton</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.nmrfToMrca()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.nmrfToNds()`

`ea.m.nmrfToNds(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.nmrfToNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.nmrfToNurf()`

`ea.m.nmrfToNurf(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction">NamedUnivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.nmrfToNurf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.noisedNds()`

`ea.m.noisedNds(of; inputSigma; outputSigma; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |
| `inputSigma` | d | `0.0` | <code>double</code> |
| `outputSigma` | d | `0.0` | <code>double</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.noisedNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.noisedNmrf()`

`ea.m.noisedNmrf(of; sigma; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code> |
| `sigma` | d | `0.0` | <code>double</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.noisedNmrf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.ntissToNmrf()`

`ea.m.ntissToNmrf(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalTimeInvariantStatelessSystem">NumericalTimeInvariantStatelessSystem</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.ntissToNmrf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.nurfToNds()`

`ea.m.nurfToNds(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction">NamedUnivariateRealFunction</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.nurfToNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.oGraphToNmrf()`

`ea.m.oGraphToNmrf(of; postOperator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.Graph">Graph</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Node">Node</abbr>, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph$NonValuedArc">OperatorGraph$NonValuedArc</abbr>&gt;&gt;</code> |
| `postOperator` | npm | `ds.f.doubleOp(activationF = identity)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.oGraphToNmrf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.pair()`

`ea.m.pair(of; first; second)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;F1, S1&gt;&gt;</code> |
| `first` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;F1, F2&gt;</code> |
| `second` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;S1, S2&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;F2, S2&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.pair()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.parametrized()`

`ea.m.parametrized(of; parametrized)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, P&gt;</code> |
| `parametrized` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, Y&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, E&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.parametrized()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.splitter()`

`ea.m.splitter(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;T&gt;, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.splitter()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.srTreeToNurf()`

`ea.m.srTreeToNurf(of; simplify; postOperator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;</code> |
| `simplify` | b | `false` | <code>boolean</code> |
| `postOperator` | npm | `ds.f.doubleOp(activationF = identity)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction">NamedUnivariateRealFunction</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.srTreeToNurf()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.steppedNds()`

`ea.m.steppedNds(of; stepT)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code> |
| `stepT` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem">NumericalDynamicalSystem</abbr>&lt;?&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.steppedNds()` by jgea-experimenter:2.8.2

### Builder `ea.mapper.ttpnToProgram()`

`ea.m.ttpnToProgram(of; maxNOfSteps; maxNOfTokens; maxTokensSize; maxSingleTokenSize; skipBlocked)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network">Network</abbr>&gt;</code> |
| `maxNOfSteps` | i | `128` | <code>int</code> |
| `maxNOfTokens` | i | `256` | <code>int</code> |
| `maxTokensSize` | i | `1024` | <code>int</code> |
| `maxSingleTokenSize` | i | `128` | <code>int</code> |
| `skipBlocked` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.Program">Program</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Mappers.ttpnToProgram()` by jgea-experimenter:2.8.2

## Package `ea.misc`

### Builder `ea.misc.bestSelector()`

`ea.misc.bestSelector(name; nOfOpponents)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `best[{nOfOpponents}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `nOfOpponents` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.bestSelector()` by jgea-experimenter:2.8.2

### Builder `ea.misc.bf()`

`ea.misc.bf(exprs)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `exprs` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction">BooleanFunction</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.bf()` by jgea-experimenter:2.8.2

### Builder `ea.misc.caVideo()`

`ea.misc.caVideo(gray; caStateRange; nOfSteps; sizeRate; marginRate; frameRate; fontSize)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `gray` | b | `true` | <code>boolean</code> |
| `caStateRange` | npm | `m.range(max = 1; min = -1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `nOfSteps` | i | `100` | <code>int</code> |
| `sizeRate` | i | `10` | <code>int</code> |
| `marginRate` | d | `0.0` | <code>double</code> |
| `frameRate` | d | `10.0` | <code>double</code> |
| `fontSize` | d | `10.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.drawer.VideoBuilder">VideoBuilder</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.ca.MultivariateRealGridCellularAutomaton">MultivariateRealGridCellularAutomaton</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.caVideo()` by jgea-experimenter:2.8.2

### Builder `ea.misc.ch()`

`ea.misc.ch(s)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `s` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.lang.Character">Character</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.ch()` by jgea-experimenter:2.8.2

### Builder `ea.misc.colorByName()`

`ea.misc.colorByName(name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.awt.Color">Color</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.colorByName()` by jgea-experimenter:2.8.2

### Builder `ea.misc.colorByRgb()`

`ea.misc.colorByRgb(r; g; b)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `r` | i |  | <code>int</code> |
| `g` | i |  | <code>int</code> |
| `b` | i |  | <code>int</code> |

Produces <code><abbr title="java.awt.Color">Color</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.colorByRgb()` by jgea-experimenter:2.8.2

### Builder `ea.misc.evenParityBf()`

`ea.misc.evenParityBf(n)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction">BooleanFunction</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.evenParityBf()` by jgea-experimenter:2.8.2

### Builder `ea.misc.farthestMESelector()`

`ea.misc.farthestMESelector(name; nOfOpponents)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `farthest[{nOfOpponents}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `nOfOpponents` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.farthestMESelector()` by jgea-experimenter:2.8.2

### Builder `ea.misc.fromTextFile()`

`ea.misc.fromTextFile(path; f)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `path` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.String">String</abbr>, X&gt;</code> |

Produces <code>X</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.fromTextFile()` by jgea-experimenter:2.8.2

### Builder `ea.misc.imgByName()`

`ea.misc.imgByName(name; gateBGColor; w; h; marginRate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `gateBGColor` | npm | `ea.misc.colorByName(name = black)` | <code><abbr title="java.awt.Color">Color</abbr></code> |
| `w` | i | `15` | <code>int</code> |
| `h` | i | `15` | <code>int</code> |
| `marginRate` | d | `0.1` | <code>double</code> |

Produces <code><abbr title="java.awt.image.BufferedImage">BufferedImage</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.imgByName()` by jgea-experimenter:2.8.2

### Builder `ea.misc.imgFromString()`

`ea.misc.imgFromString(s; borderColor; gateBGColor; w; h; marginRate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `s` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `borderColor` | npm | `ea.misc.colorByName(name = white)` | <code><abbr title="java.awt.Color">Color</abbr></code> |
| `gateBGColor` | npm | `ea.misc.colorByName(name = black)` | <code><abbr title="java.awt.Color">Color</abbr></code> |
| `w` | i | `159` | <code>int</code> |
| `h` | i | `15` | <code>int</code> |
| `marginRate` | d | `0.1` | <code>double</code> |

Produces <code><abbr title="java.awt.image.BufferedImage">BufferedImage</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.imgFromString()` by jgea-experimenter:2.8.2

### Builder `ea.misc.lossyAverage()`

`ea.misc.lossyAverage(memoryFactor)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `memoryFactor` | d | `0.5` | <code>double</code> |

Produces <code><abbr title="java.util.function.BinaryOperator">BinaryOperator</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.lossyAverage()` by jgea-experimenter:2.8.2

### Builder `ea.misc.minValue()`

`ea.misc.minValue()`

Produces <code><abbr title="java.util.function.BinaryOperator">BinaryOperator</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.minValue()` by jgea-experimenter:2.8.2

### Builder `ea.misc.mopmBf()`

`ea.misc.mopmBf(n)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction">BooleanFunction</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.mopmBf()` by jgea-experimenter:2.8.2

### Builder `ea.misc.nearestMESelector()`

`ea.misc.nearestMESelector(name; nOfOpponents)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `nearest[{nOfOpponents}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `nOfOpponents` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.nearestMESelector()` by jgea-experimenter:2.8.2

### Builder `ea.misc.nmrf()`

`ea.misc.nmrf(exprs; yVarNames)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `exprs` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `yVarNames` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedMultivariateRealFunction">NamedMultivariateRealFunction</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.nmrf()` by jgea-experimenter:2.8.2

### Builder `ea.misc.nurf()`

`ea.misc.nurf(expr; yVarName; additionalVars; postOperator; simplify)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `expr` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `yVarName` | s | `y` | <code><abbr title="java.lang.String">String</abbr></code> |
| `additionalVars` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `postOperator` | npm | `ds.f.doubleOp(activationF = identity)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `simplify` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jsdynsym.core.numerical.named.NamedUnivariateRealFunction">NamedUnivariateRealFunction</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.nurf()` by jgea-experimenter:2.8.2

### Builder `ea.misc.oldestSelector()`

`ea.misc.oldestSelector(name; nOfOpponents)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `oldest[{nOfOpponents}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `nOfOpponents` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.oldestSelector()` by jgea-experimenter:2.8.2

### Builder `ea.misc.randomSelector()`

`ea.misc.randomSelector(name; nOfOpponents)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `random[{nOfOpponents}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `nOfOpponents` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.randomSelector()` by jgea-experimenter:2.8.2

### Builder `ea.misc.sEntry()`

`ea.misc.sEntry(key; value)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `key` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `value` | npm |  | <code>V</code> |

Produces <code><abbr title="java.util.Map$Entry">Map$Entry</abbr>&lt;<abbr title="java.lang.String">String</abbr>, V&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.sEntry()` by jgea-experimenter:2.8.2

### Builder `ea.misc.toVideo()`

`ea.misc.toVideo(drawer)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `drawer` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.SimulationOutcomeDrawer">SimulationOutcomeDrawer</abbr>&lt;S&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.drawer.VideoBuilder">VideoBuilder</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;S&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Miscs.toVideo()` by jgea-experimenter:2.8.2

## Package `ea.plot.multi`

Aliases: `ea.plot.m`, `ea.plot.multi`

### Builder `ea.plot.multi.quality()`

`ea.plot.m.quality(xSubplot; ySubplot; line; x; y; valueAggregator; minAggregator; maxAggregator; xRange; yRange; limitOneYForK; useKForX; xQuantization; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `line` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm | `f.quantized(of = ea.f.nOfEvals(); q = 1)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;?, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm | `f.composition(of = ea.f.quality(of = ea.f.best()); then = f.identity())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `valueAggregator` | npm | `f.median()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `minAggregator` | npm | `f.percentile(p = 25)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `maxAggregator` | npm | `f.percentile(p = 75)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |
| `useKForX` | b | `false` | <code>boolean</code> |
| `xQuantization` | i | `1` | <code><abbr title="java.lang.String">String</abbr></code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.AggregatedXYDataSeriesMKPAF">AggregatedXYDataSeriesMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.xy()` by jgea-experimenter:2.8.2

### Builder `ea.plot.multi.qualityBoxplot()`

`ea.plot.m.qualityBoxplot(xSubplot; ySubplot; box; y; predicateValue; condition; yRange; limitOneYForK; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `box` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `y` | npm | `f.composition(of = ea.f.quality(of = ea.f.best()); then = f.identity())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm | `ea.f.rate(of = ea.f.progress())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.gtEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.DistributionMKPAF">DistributionMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>, X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.yBoxplot()` by jgea-experimenter:2.8.2

### Builder `ea.plot.multi.scatterExp()`

`ea.plot.m.scatterExp(xSubplot; ySubplot; group; x; y; predicateValue; condition; xRange; yRange; limitOneYForK)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `group` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm | `ea.f.rate(of = ea.f.progress())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.gtEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.ScatterMKPAF">ScatterMKPAF</abbr>&lt;E, K, <abbr title="java.lang.String">String</abbr>, X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.scatter()` by jgea-experimenter:2.8.2

### Builder `ea.plot.multi.uniqueness()`

`ea.plot.m.uniqueness(xSubplot; ySubplot; line; x; y; valueAggregator; minAggregator; maxAggregator; xRange; yRange; limitOneYForK; useKForX; xQuantization)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `line` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm | `f.quantized(of = ea.f.nOfEvals(); q = 1)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;?, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm | `f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()))` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `valueAggregator` | npm | `f.median()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `minAggregator` | npm | `f.percentile(p = 25)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `maxAggregator` | npm | `f.percentile(p = 75)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |
| `useKForX` | b | `false` | <code>boolean</code> |
| `xQuantization` | i | `1` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.AggregatedXYDataSeriesMKPAF">AggregatedXYDataSeriesMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.xy()` by jgea-experimenter:2.8.2

### Builder `ea.plot.multi.uniquenessBoxplot()`

`ea.plot.m.uniquenessBoxplot(xSubplot; ySubplot; box; y; predicateValue; condition; yRange; limitOneYForK)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `box` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `y` | npm | `f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all()))` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm | `ea.f.rate(of = ea.f.progress())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.gtEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.DistributionMKPAF">DistributionMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>, X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.yBoxplot()` by jgea-experimenter:2.8.2

### Builder `ea.plot.multi.xyExp()`

`ea.plot.m.xyExp(xSubplot; ySubplot; line; x; y; valueAggregator; minAggregator; maxAggregator; xRange; yRange; limitOneYForK; useKForX; xQuantization)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `line` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm | `f.quantized(of = ea.f.nOfEvals(); q = 1)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;?, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `valueAggregator` | npm | `f.median()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `minAggregator` | npm | `f.percentile(p = 25)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `maxAggregator` | npm | `f.percentile(p = 75)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |
| `useKForX` | b | `false` | <code>boolean</code> |
| `xQuantization` | i | `1` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.AggregatedXYDataSeriesMKPAF">AggregatedXYDataSeriesMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.xy()` by jgea-experimenter:2.8.2

### Builder `ea.plot.multi.yBoxplotExp()`

`ea.plot.m.yBoxplotExp(xSubplot; ySubplot; box; y; predicateValue; condition; yRange; limitOneYForK)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm | `f.interpolated(name = problem; s = "{problem.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm | `f.interpolated(name = none; s = "_")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `box` | npm | `f.interpolated(name = solver; s = "{solver.name}")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm | `ea.f.rate(of = ea.f.progress())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.gtEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.DistributionMKPAF">DistributionMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>, X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.yBoxplot()` by jgea-experimenter:2.8.2

## Package `ea.plot.single`

Aliases: `ea.plot.s`, `ea.plot.single`

### Builder `ea.plot.single.biObjectivePopulation()`

`ea.plot.s.biObjectivePopulation(title; points; x; y; predicateValue; unique; condition; xRange; yRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Fronts with {solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `points` | npm[] | `[ea.f.firsts(), ea.f.mids(), ea.f.lasts()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Collection">Collection</abbr>&lt;P&gt;&gt;&gt;</code> |
| `x` | npm | `f.nThMapValue(n = 0; of = ea.f.quality())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super P, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm | `f.nThMapValue(n = 1; of = ea.f.quality())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super P, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `unique` | b | `true` | <code>boolean</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSEPAF">XYDataSeriesSEPAF</abbr>&lt;E, K, X, P&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyes()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.coMe()`

`ea.plot.s.coMe(title; values; maps; predicateValue; condition; valueRange; unique; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Archives of {solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[f.composition(of = ea.f.quality(); then = f.identity())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super V, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `maps` | npm[] | `[ea.f.archiveToPolygons(of = ea.f.coMeArchive1()), ea.f.archiveToPolygons(of = ea.f.coMeArchive2())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.geometry.Polygon">Polygon</abbr>, V&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.HeatPolyMapSEPAF">HeatPolyMapSEPAF</abbr>&lt;E, K, X, V&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.heat()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.coMeStrategies()`

`ea.plot.s.coMeStrategies(title; fields; pointPairs; predicateValue; condition; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Strategies (2D fields) of {solver.name} on {problem.name} (seed={runrandomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `fields` | npm[] | `[ea.f.coMeStrategy1Field(), ea.f.coMeStrategy2Field()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, F&gt;&gt;</code> |
| `pointPairs` | npm[] | `[f.identity()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super F, ? extends <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.VectorialFieldSEPAF">VectorialFieldSEPAF</abbr>&lt;E, K, X, F&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.field()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.fieldRun()`

`ea.plot.s.fieldRun(title; fields; pointPairs; predicateValue; condition; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `fields` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, F&gt;&gt;</code> |
| `pointPairs` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super F, ? extends <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.VectorialFieldSEPAF">VectorialFieldSEPAF</abbr>&lt;E, K, X, F&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.field()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.gridRun()`

`ea.plot.s.gridRun(title; values; grids; predicateValue; condition; valueRange; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super G, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `grids` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;G&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.UnivariateGridSEPAF">UnivariateGridSEPAF</abbr>&lt;E, K, X, G&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.grid()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.gridState()`

`ea.plot.s.gridState(title; values; grids; predicateValue; condition; valueRange; unique; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Grid population of {solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[f.composition(of = ea.f.quality(); then = f.identity())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super G, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `grids` | npm[] | `[ea.f.stateGrid()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;G&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.UnivariateGridSEPAF">UnivariateGridSEPAF</abbr>&lt;E, K, X, G&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.grid()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.heatRun()`

`ea.plot.s.heatRun(title; values; maps; predicateValue; condition; valueRange; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super V, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `maps` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.geometry.Polygon">Polygon</abbr>, V&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.HeatPolyMapSEPAF">HeatPolyMapSEPAF</abbr>&lt;E, K, X, V&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.heat()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.landscape()`

`ea.plot.s.landscape(title; predicateValue; condition; mapper; q; xRange; yRange; xF; yF; valueRange; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q, P&gt;, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S&gt;</code> |
| `q` | npm | `ea.m.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;Q, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `xF` | npm | `f.nTh(n = 0; of = ea.f.genotype())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `yF` | npm | `f.nTh(n = 1; of = ea.f.genotype())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.LandscapeSEPAF">LandscapeSEPAF</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q, P&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Run">Run</abbr>&lt;?, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, X, <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SinglePlots.landscape()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.maMe2()`

`ea.plot.s.maMe2(title; values; maps; predicateValue; condition; valueRange; unique; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Archives of {solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[f.composition(of = ea.f.quality(); then = f.identity())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super V, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `maps` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.geometry.Polygon">Polygon</abbr>, V&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.HeatPolyMapSEPAF">HeatPolyMapSEPAF</abbr>&lt;E, K, X, V&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.heat()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.me()`

`ea.plot.s.me(title; values; maps; predicateValue; condition; valueRange; unique; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Archive of {solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[f.composition(of = ea.f.quality(); then = f.identity())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super V, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `maps` | npm[] | `[ea.f.archiveToPolygons(of = ea.f.meArchive())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.geometry.Polygon">Polygon</abbr>, V&gt;&gt;&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.HeatPolyMapSEPAF">HeatPolyMapSEPAF</abbr>&lt;E, K, X, V&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.heat()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.populationValidation()`

`ea.plot.s.populationValidation(title; points; x; y; predicateValue; unique; condition; xRange; yRange; q; v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "Population validation of {solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `points` | npm[] | `[ea.f.all()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Collection">Collection</abbr>&lt;P&gt;&gt;&gt;</code> |
| `x` | npm | `f.composition(of = ea.f.quality(); then = f.identity())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super P, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm | `f.composition(of = f.composition(of = ea.f.solution(); then = null); then = f.identity())` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super P, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm | `f.quantized(format = "%.2f"; of = ea.f.rate(of = ea.f.progress()); q = 0.05)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `unique` | b | `true` | <code>boolean</code> |
| `condition` | npm | `predicate.inD(values = [0.0; 0.1; 0.25; 0.5; 1.0])` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `v` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSEPAF">XYDataSeriesSEPAF</abbr>&lt;E, K, X, P&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyes()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.quality()`

`ea.plot.s.quality(title; x; ys; xRange; yRange; q)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm | `ea.f.nOfEvals()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `ys` | npm[] | `[f.composition(of = ea.f.quality(of = ea.f.best()); then = f.identity())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `q` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSRPAF">XYDataSeriesSRPAF</abbr>&lt;E, K&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyrs()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.uniqueness()`

`ea.plot.s.uniqueness(title; x; ys; xRange; yRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm | `ea.f.nOfEvals()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `ys` | npm[] | `[f.uniqueness(of = f.each(mapF = ea.f.genotype(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.solution(); of = ea.f.all())), f.uniqueness(of = f.each(mapF = ea.f.quality(); of = ea.f.all()))]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSRPAF">XYDataSeriesSRPAF</abbr>&lt;E, K&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyrs()` by jgea-experimenter:2.8.2

### Builder `ea.plot.single.xyrsRun()`

`ea.plot.s.xyrsRun(title; x; ys; xRange; yRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm | `f.interpolated(name = title; s = "{solver.name} on {problem.name} (seed={randomGenerator.seed})")` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm | `ea.f.nOfEvals()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `ys` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSRPAF">XYDataSeriesSRPAF</abbr>&lt;E, K&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyrs()` by jgea-experimenter:2.8.2

## Package `ea.predicate`

### Builder `ea.predicate.isSrConstant()`

`ea.predicate.isSrConstant(f)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Predicates.isSrConstant()` by jgea-experimenter:2.8.2

### Builder `ea.predicate.isSrVariable()`

`ea.predicate.isSrVariable(f)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Predicates.isSrVariable()` by jgea-experimenter:2.8.2

## Package `ea.problem`

Aliases: `ea.p`, `ea.problem`

### Builder `ea.problem.biSimToBiTo()`

`ea.p.biSimToBiTo(name; simulation; cFunction; type; qFunction1; qFunction2; dT; tRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiSimulation">HomogeneousBiSimulation</abbr>&lt;S, BS, B&gt;</code> |
| `cFunction` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;Q, C&gt;</code> |
| `type` | e | `MINIMIZE` | <code><abbr title="io.github.ericmedvet.jgea.experimenter.builders.Problems$OptimizationType">Problems$OptimizationType</abbr></code> |
| `qFunction1` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;B, Q&gt;</code> |
| `qFunction2` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;B, Q&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedBiProblem">TotalOrderQualityBasedBiProblem</abbr>&lt;S, B, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.biSimToBiTo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.biSimToTo()`

`ea.p.biSimToTo(name; simulation; cFunction; type; qFunction; trainingOpponent; dT; tRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.HomogeneousBiSimulation">HomogeneousBiSimulation</abbr>&lt;S, BS, B&gt;</code> |
| `cFunction` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;Q, C&gt;</code> |
| `type` | e | `MINIMIZE` | <code><abbr title="io.github.ericmedvet.jgea.experimenter.builders.Problems$OptimizationType">Problems$OptimizationType</abbr></code> |
| `qFunction` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;B, Q&gt;</code> |
| `trainingOpponent` | npm |  | <code><abbr title="java.util.function.Supplier">Supplier</abbr>&lt;S&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.biSimToTo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.functionToTo()`

`ea.p.functionToTo(name; qFunction; cFunction; type; example)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{qFunction.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `qFunction` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, Q&gt;</code> |
| `cFunction` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;Q, C&gt;</code> |
| `type` | e | `MINIMIZE` | <code><abbr title="io.github.ericmedvet.jgea.experimenter.builders.Problems$OptimizationType">Problems$OptimizationType</abbr></code> |
| `example` | npm | `misc.nullValue()` | <code>S</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.functionToTo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.functionsToScbmo()`

`ea.p.functionsToScbmo(name; cases; validationCases; toMinObjectives; toMaxObjectives; example)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `cases` | <code><abbr title="java.lang.String">String</abbr></code> |
| `cases` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;S, CQ&gt;&gt;</code> |
| `validationCases` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;S, CQ&gt;&gt;</code> |
| `toMinObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;CQ&gt;, O&gt;&gt;</code> |
| `toMaxObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;CQ&gt;, O&gt;&gt;</code> |
| `example` | npm | `misc.nullValue()` | <code>S</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleCBMOProblem">SimpleCBMOProblem</abbr>&lt;S, <abbr title="java.util.function.Function">Function</abbr>&lt;S, CQ&gt;, CQ, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.functionsToScbmo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.manyToCbto()`

`ea.p.manyToCbto(name; problems; validationProblems; aggregator; comparator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{problems}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `problems` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, PQ&gt;&gt;</code> |
| `validationProblems` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, PQ&gt;&gt;</code> |
| `aggregator` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;PQ&gt;, AQ&gt;</code> |
| `comparator` | npm |  | <code><abbr title="java.util.Comparator">Comparator</abbr>&lt;AQ&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.CBTOProblem">CBTOProblem</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, PQ&gt;, PQ, AQ&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.manyToCbto()` by jgea-experimenter:2.8.2

### Builder `ea.problem.moToSo()`

`ea.p.moToSo(name; objective; moProblem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{moProblem.name}[{objective}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `objective` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `moProblem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.problem.MultiObjectiveProblem">MultiObjectiveProblem</abbr>&lt;S, Q, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.moToSo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.mtToMo()`

`ea.p.mtToMo(name; mtProblem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `mt2mo[{mtProblem.name}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mtProblem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.problem.MultiTargetProblem">MultiTargetProblem</abbr>&lt;S&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleMOProblem">SimpleMOProblem</abbr>&lt;S, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.mtToMo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.preMapped()`

`ea.p.preMapped(name; mapper; problem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{problem.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;T, S&gt;</code> |
| `problem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.problem.Problem">Problem</abbr>&lt;S&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.Problem">Problem</abbr>&lt;T&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.preMapped()` by jgea-experimenter:2.8.2

### Builder `ea.problem.simToDurationSmfbbmo()`

`ea.p.simToDurationSmfbbmo(name; simulation; dT; initT; finalTRange; toMinObjectives; toMaxObjectives)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}[finalT={finalTRange.min}--{finalTRange.min}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;S, BS, B&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `initT` | d |  | <code>double</code> |
| `finalTRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `toMinObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |
| `toMaxObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleMFBBMOProblem">SimpleMFBBMOProblem</abbr>&lt;S, B, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.simToDurationSmfbbmo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.simToResolutionSmfbbmo()`

`ea.p.simToResolutionSmfbbmo(name; simulation; dTRange; tRange; toMinObjectives; toMaxObjectives)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}[dT={dTRange.min}--{dTRange.max}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;S, BS, B&gt;</code> |
| `dTRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `toMinObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |
| `toMaxObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleMFBBMOProblem">SimpleMFBBMOProblem</abbr>&lt;S, B, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.simToResolutionSmfbbmo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.simToSbbmo()`

`ea.p.simToSbbmo(name; simulation; dT; tRange; toMinObjectives; toMaxObjectives)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;S, BS, B&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `toMinObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |
| `toMaxObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleBBMOProblem">SimpleBBMOProblem</abbr>&lt;S, B, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.simToSbbmo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.simToSmo()`

`ea.p.simToSmo(name; simulation; dT; tRange; toMinObjectives; toMaxObjectives)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;S, BS, B&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `toMinObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |
| `toMaxObjectives` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;B, O&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleMOProblem">SimpleMOProblem</abbr>&lt;S, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.simToSmo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.simToTo()`

`ea.p.simToTo(name; simulation; dT; tRange; comparator; qFunction)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{simulation.name}->{qFunction}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulation` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;S, BS, B&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `comparator` | npm |  | <code><abbr title="java.util.Comparator">Comparator</abbr>&lt;Q&gt;</code> |
| `qFunction` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;B, Q&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.simToTo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.simsToTo()`

`ea.p.simsToTo(name; simulations; dT; tRange; comparator; aggregator; qFunction)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `sims->{qFunction}->{aggregator}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `simulations` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.Simulation">Simulation</abbr>&lt;S, BS, B&gt;&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `comparator` | npm |  | <code><abbr title="java.util.Comparator">Comparator</abbr>&lt;Q&gt;</code> |
| `aggregator` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;Q&gt;, Q&gt;</code> |
| `qFunction` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;B, Q&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.simsToTo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.smoToSubsettedSmo()`

`ea.p.smoToSubsettedSmo(name; objectives; smoProblem)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{smoProblem.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `objectives` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `smoProblem` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleMOProblem">SimpleMOProblem</abbr>&lt;S, O&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.SimpleMOProblem">SimpleMOProblem</abbr>&lt;S, O&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.smoToSubsettedSmo()` by jgea-experimenter:2.8.2

### Builder `ea.problem.srlatToBbto()`

`ea.p.srlatToBbto(name; task; dT; tRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{task.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `task` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.SingleRLAgentTask">SingleRLAgentTask</abbr>&lt;C, O, A, CS, TS&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.BBTOProblem">BBTOProblem</abbr>&lt;C, <abbr title="io.github.ericmedvet.jsdynsym.control.Simulation$Outcome">Simulation$Outcome</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.control.SingleAgentTask$Step">SingleAgentTask$Step</abbr>&lt;<abbr title="io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent$RewardedInput">ReinforcementLearningAgent$RewardedInput</abbr>&lt;O&gt;, A, TS&gt;&gt;, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.srlatToBbto()` by jgea-experimenter:2.8.2

### Builder `ea.problem.srlatToTo()`

`ea.p.srlatToTo(name; task; dT; tRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{task.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `task` | npm |  | <code><abbr title="io.github.ericmedvet.jsdynsym.control.SingleRLAgentTask">SingleRLAgentTask</abbr>&lt;C, O, A, CS, ?&gt;</code> |
| `dT` | d |  | <code>double</code> |
| `tRange` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;C, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Problems.srlatToTo()` by jgea-experimenter:2.8.2

## Package `ea.problem.booleanRegression`

Aliases: `ea.p.booleanRegression`, `ea.p.br`, `ea.problem.booleanRegression`, `ea.problem.br`

### Builder `ea.problem.booleanRegression.evenParity()`

`ea.p.br.evenParity(name; n; metrics; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `parity[{n}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `n` | i |  | <code>int</code> |
| `metrics` | e[] | `[avg_dissimilarity]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.bool.BooleanRegressionProblem$Metric">BooleanRegressionProblem$Metric</abbr>&gt;</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.bool.synthetic.EvenParity">EvenParity</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.BooleanRegressionProblems.evenParity()` by jgea-experimenter:2.8.2

### Builder `ea.problem.booleanRegression.mopm()`

`ea.p.br.mopm(name; n; metrics; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `momp[{n}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `n` | i |  | <code>int</code> |
| `metrics` | e[] | `[avg_dissimilarity]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.bool.BooleanRegressionProblem$Metric">BooleanRegressionProblem$Metric</abbr>&gt;</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.bool.synthetic.MultipleOutputParallelMultiplier">MultipleOutputParallelMultiplier</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.BooleanRegressionProblems.mopm()` by jgea-experimenter:2.8.2

### Builder `ea.problem.booleanRegression.targetBased()`

`ea.p.br.targetBased(name; n; exprs; metrics; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `target` | <code><abbr title="java.lang.String">String</abbr></code> |
| `n` | i |  | <code>int</code> |
| `exprs` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `metrics` | e[] | `[avg_dissimilarity]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.bool.BooleanRegressionProblem$Metric">BooleanRegressionProblem$Metric</abbr>&gt;</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.bool.synthetic.PrecomputedSyntheticBRProblem">PrecomputedSyntheticBRProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.BooleanRegressionProblems.targetBased()` by jgea-experimenter:2.8.2

## Package `ea.problem.multivariateRegression`

Aliases: `ea.p.mr`, `ea.p.multivariateRegression`, `ea.problem.mr`, `ea.problem.multivariateRegression`

### Builder `ea.problem.multivariateRegression.fromData()`

`ea.p.mr.fromData(name; provider; metrics; nFolds; testFold)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{provider.name}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `provider` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.util.IndexedProvider">IndexedProvider</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.problem.ExampleBasedProblem$Example">ExampleBasedProblem$Example</abbr>&lt;<abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `metrics` | e[] | `[mse]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem$Metric">UnivariateRegressionProblem$Metric</abbr>&gt;</code> |
| `nFolds` | i | `10` | <code>int</code> |
| `testFold` | i | `0` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.multivariate.MultivariateRegressionProblem">MultivariateRegressionProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.MultivariateRegressionProblems.fromData()` by jgea-experimenter:2.8.2

## Package `ea.problem.programSynthesis`

Aliases: `ea.p.programSynthesis`, `ea.p.ps`, `ea.problem.programSynthesis`, `ea.problem.ps`

### Builder `ea.problem.programSynthesis.synthetic()`

`ea.p.ps.synthetic(name; metrics; maxDissimilarity; randomGenerator; nOfCases; nOfValidationCases; maxExceptionRate; ints; reals; strings; intRange; realRange; stringLengthRange; sequenceSizeRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `metrics` | e[] | `[fail_rate]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.programsynthesis.ProgramSynthesisProblem$Metric">ProgramSynthesisProblem$Metric</abbr>&gt;</code> |
| `maxDissimilarity` | d | `1000.0` | <code>double</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |
| `nOfCases` | i | `20` | <code>int</code> |
| `nOfValidationCases` | i | `100` | <code>int</code> |
| `maxExceptionRate` | d | `0.1` | <code>double</code> |
| `ints` | i[] | `[1, 2, 3, 5, 10]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `reals` | d[] | `[1.0, 2.0, 3.0, 1.5, 2.5, 3.14]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `strings` | s[] | `[cat, dog, Hello World!, mummy]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `intRange` | npm | `m.range(max = 100; min = -10)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `realRange` | npm | `m.range(max = 10; min = -10)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `stringLengthRange` | npm | `m.range(max = 20; min = 2)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `sequenceSizeRange` | npm | `m.range(max = 8; min = 1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.programsynthesis.ProgramSynthesisProblem">ProgramSynthesisProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.ProgramSynthesisProblems.synthetic()` by jgea-experimenter:2.8.2

## Package `ea.problem.synthetic`

Aliases: `ea.p.s`, `ea.p.synthetic`, `ea.problem.s`, `ea.problem.synthetic`

### Builder `ea.problem.synthetic.ackley()`

`ea.p.s.ackley(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `ackley-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.Ackley">Ackley</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.ackley()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.bentCigar()`

`ea.p.s.bentCigar(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `bentCigar-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.BentCigar">BentCigar</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.bentCigar()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.charShapeApproximation()`

`ea.p.s.charShapeApproximation(name; target; translation; smoothed; weighted)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `shape-{target}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `target` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `translation` | b | `true` | <code>boolean</code> |
| `smoothed` | b | `true` | <code>boolean</code> |
| `weighted` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.grid.CharShapeApproximation">CharShapeApproximation</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.charShapeApproximation()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.circularPointsAiming()`

`ea.p.s.circularPointsAiming(name; p; n; radius; center; seed)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `circularPointsAiming-{p}-{n}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |
| `n` | i | `5` | <code>int</code> |
| `radius` | d | `0.5` | <code>double</code> |
| `center` | d | `1.0` | <code>double</code> |
| `seed` | i | `1` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.CircularPointsAiming">CircularPointsAiming</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.circularPointsAiming()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.discus()`

`ea.p.s.discus(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `discus-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.Discus">Discus</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.discus()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.gaussianMixture2D()`

`ea.p.s.gaussianMixture2D(name; targets; c)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `gm2D` | <code><abbr title="java.lang.String">String</abbr></code> |
| `targets` | d[] | `[-3.0, -2.0, 2.0, 2.0, 2.0, 1.0]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `c` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.GaussianMixture2D">GaussianMixture2D</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.gaussianMixture2D()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.grammarText()`

`ea.p.s.grammarText(name; target)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `s({target})` | <code><abbr title="java.lang.String">String</abbr></code> |
| `target` | s | `The white dog!` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.Text">Text</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.grammarText()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.highConditionedElliptic()`

`ea.p.s.highConditionedElliptic(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `highConditionedElliptic-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.HighConditionedElliptic">HighConditionedElliptic</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.highConditionedElliptic()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.intOneMax()`

`ea.p.s.intOneMax(name; p; upperBound)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `iOneMax-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |
| `upperBound` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.IntOneMax">IntOneMax</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.intOneMax()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.lettersMax()`

`ea.p.s.lettersMax(name; letters; l)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `lettersMax-{l}-{letters}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `letters` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `l` | i | `32` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.LettersMax">LettersMax</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.lettersMax()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.linearPoints()`

`ea.p.s.linearPoints(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `lPoints-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.LinearPoints">LinearPoints</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.linearPoints()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.mrCaMorphogenesis()`

`ea.p.s.mrCaMorphogenesis(name; target; gray; fromStep; toStep; caStateRange; targetRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `ca-target-[{minConvergenceStep}-{maxConvergenceStep}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `target` | npm |  | <code><abbr title="java.awt.image.BufferedImage">BufferedImage</abbr></code> |
| `gray` | b | `true` | <code>boolean</code> |
| `fromStep` | i | `40` | <code>int</code> |
| `toStep` | i | `60` | <code>int</code> |
| `caStateRange` | npm | `m.range(max = 1; min = -1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `targetRange` | npm | `m.range(max = 1; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.ca.MRCAMorphogenesis">MRCAMorphogenesis</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.mrCaMorphogenesis()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.mrCaNamedImageMorphogenesis()`

`ea.p.s.mrCaNamedImageMorphogenesis(name; target; gray; fromStep; toStep; caStateRange; targetRange; iName; w; h)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `ca-nImg` | <code><abbr title="java.lang.String">String</abbr></code> |
| `target` | npm | `ea.misc.imgByName(h = 15; name = null; w = 15)` | <code><abbr title="java.awt.image.BufferedImage">BufferedImage</abbr></code> |
| `gray` | b | `false` | <code>boolean</code> |
| `fromStep` | i | `40` | <code>int</code> |
| `toStep` | i | `60` | <code>int</code> |
| `caStateRange` | npm | `m.range(max = 1; min = -1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `targetRange` | npm | `m.range(max = 1; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `iName` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `w` | i | `15` | <code><abbr title="java.lang.String">String</abbr></code> |
| `h` | i | `15` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.ca.MRCAMorphogenesis">MRCAMorphogenesis</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.mrCaMorphogenesis()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.mrCaStringMorphogenesis()`

`ea.p.s.mrCaStringMorphogenesis(name; target; gray; fromStep; toStep; caStateRange; targetRange; s; w; h)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `ca-string` | <code><abbr title="java.lang.String">String</abbr></code> |
| `target` | npm | `ea.misc.imgFromString(h = 15; s = x; w = 15)` | <code><abbr title="java.awt.image.BufferedImage">BufferedImage</abbr></code> |
| `gray` | b | `true` | <code>boolean</code> |
| `fromStep` | i | `40` | <code>int</code> |
| `toStep` | i | `60` | <code>int</code> |
| `caStateRange` | npm | `m.range(max = 1; min = -1)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `targetRange` | npm | `m.range(max = 1; min = 0)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `s` | s | `x` | <code><abbr title="java.lang.String">String</abbr></code> |
| `w` | i | `15` | <code><abbr title="java.lang.String">String</abbr></code> |
| `h` | i | `15` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.ca.MRCAMorphogenesis">MRCAMorphogenesis</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.mrCaMorphogenesis()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.multiModalIntOneMax()`

`ea.p.s.multiModalIntOneMax(name; p; upperBound; nOfTargets)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `mmIOneMax-{p}-{nOfTargets}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |
| `upperBound` | i | `10` | <code>int</code> |
| `nOfTargets` | i | `3` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.MultiModalIntOneMax">MultiModalIntOneMax</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.multiModalIntOneMax()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.multiObjectiveIntOneMax()`

`ea.p.s.multiObjectiveIntOneMax(name; p; upperBound)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `moIOneMax-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |
| `upperBound` | i | `3` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.MultiObjectiveIntOneMax">MultiObjectiveIntOneMax</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.multiObjectiveIntOneMax()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.oneMax()`

`ea.p.s.oneMax(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `oneMax-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.OneMax">OneMax</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.oneMax()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.pointAiming()`

`ea.p.s.pointAiming(name; p; target)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `pointAiming-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |
| `target` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.PointsAiming">PointsAiming</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.pointAiming()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.pointsAiming2D()`

`ea.p.s.pointsAiming2D(name; targetY; targetXs)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `pointsAiming` | <code><abbr title="java.lang.String">String</abbr></code> |
| `targetY` | d | `2.0` | <code>double</code> |
| `targetXs` | d[] | `[-2.0, 2.0]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.PointsAiming">PointsAiming</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.pointsAiming2D()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.rastrigin()`

`ea.p.s.rastrigin(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `rastrigin-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.Rastrigin">Rastrigin</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.rastrigin()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.rosenbrock()`

`ea.p.s.rosenbrock(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `rosenbrock-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.Rosenbrock">Rosenbrock</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.rosenbrock()` by jgea-experimenter:2.8.2

### Builder `ea.problem.synthetic.sphere()`

`ea.p.s.sphere(name; p)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `sphere-{p}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `p` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.synthetic.numerical.Sphere">Sphere</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.SyntheticProblems.sphere()` by jgea-experimenter:2.8.2

## Package `ea.problem.univariateRegression`

Aliases: `ea.p.univariateRegression`, `ea.p.ur`, `ea.problem.univariateRegression`, `ea.problem.ur`

### Builder `ea.problem.univariateRegression.bundled()`

`ea.p.ur.bundled(provider; metrics; nFolds; testFold; shuffle; randomGenerator; name; xScaling; yScaling)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `provider` | npm | `ea.provider.num.fromBundled(name = null; xScaling = none; yScaling = none)` | <code><abbr title="io.github.ericmedvet.jgea.core.util.IndexedProvider">IndexedProvider</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.problem.ExampleBasedProblem$Example">ExampleBasedProblem$Example</abbr>&lt;<abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `metrics` | e[] | `[mse]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem$Metric">UnivariateRegressionProblem$Metric</abbr>&gt;</code> |
| `nFolds` | i | `10` | <code>int</code> |
| `testFold` | i | `0` | <code>int</code> |
| `shuffle` | b | `true` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |
| `name` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `xScaling` | s | `none` | <code><abbr title="java.lang.String">String</abbr></code> |
| `yScaling` | s | `none` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem">UnivariateRegressionProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.UnivariateRegressionProblems.fromData()` by jgea-experimenter:2.8.2

### Builder `ea.problem.univariateRegression.fromData()`

`ea.p.ur.fromData(provider; metrics; nFolds; testFold; shuffle; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `provider` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.util.IndexedProvider">IndexedProvider</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.problem.ExampleBasedProblem$Example">ExampleBasedProblem$Example</abbr>&lt;<abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `metrics` | e[] | `[mse]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem$Metric">UnivariateRegressionProblem$Metric</abbr>&gt;</code> |
| `nFolds` | i | `10` | <code>int</code> |
| `testFold` | i | `0` | <code>int</code> |
| `shuffle` | b | `true` | <code>boolean</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem">UnivariateRegressionProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.UnivariateRegressionProblems.fromData()` by jgea-experimenter:2.8.2

### Builder `ea.problem.univariateRegression.synthetic()`

`ea.p.ur.synthetic(name; metrics; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `metrics` | e[] | `[mse]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.UnivariateRegressionProblem$Metric">UnivariateRegressionProblem$Metric</abbr>&gt;</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.univariate.synthetic.SyntheticURProblem">SyntheticURProblem</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.UnivariateRegressionProblems.synthetic()` by jgea-experimenter:2.8.2

## Package `ea.provider.numerical`

Aliases: `ea.provider.num`, `ea.provider.numerical`

### Builder `ea.provider.numerical.empty()`

`ea.provider.num.empty(xVars; yVars)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xVars` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `yVars` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset">NumericalDataset</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NumericalIndexedProviders.empty()` by jgea-experimenter:2.8.2

### Builder `ea.provider.numerical.fromBundled()`

`ea.provider.num.fromBundled(name; xScaling; yScaling; limit)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `xScaling` | e | `NONE` | <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset$Scaling">NumericalDataset$Scaling</abbr></code> |
| `yScaling` | e | `NONE` | <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset$Scaling">NumericalDataset$Scaling</abbr></code> |
| `limit` | i | `2147483647` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset">NumericalDataset</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NumericalIndexedProviders.fromBundled()` by jgea-experimenter:2.8.2

### Builder `ea.provider.numerical.fromFile()`

`ea.provider.num.fromFile(name; filePath; xVarNamePattern; yVarNamePattern; limit)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{filePath}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `filePath` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `xVarNamePattern` | s | `x.*` | <code><abbr title="java.lang.String">String</abbr></code> |
| `yVarNamePattern` | s | `y.*` | <code><abbr title="java.lang.String">String</abbr></code> |
| `limit` | i | `2147483647` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset">NumericalDataset</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NumericalIndexedProviders.fromFile()` by jgea-experimenter:2.8.2

### Builder `ea.provider.numerical.scaled()`

`ea.provider.num.scaled(of; xScaling; yScaling)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset">NumericalDataset</abbr></code> |
| `xScaling` | e | `NONE` | <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset$Scaling">NumericalDataset$Scaling</abbr></code> |
| `yScaling` | e | `NONE` | <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset$Scaling">NumericalDataset$Scaling</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.problem.regression.NumericalDataset">NumericalDataset</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NumericalIndexedProviders.scaled()` by jgea-experimenter:2.8.2

## Package `ea.representation`

Aliases: `ea.r`, `ea.representation`

### Builder `ea.representation.bTree()`

`ea.r.bTree(constants; operators; minTreeH; maxTreeH)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `constants` | b[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `operators` | e[] | `[and, or, not]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element$Operator">Element$Operator</abbr>&gt;</code> |
| `minTreeH` | i | `4` | <code>int</code> |
| `maxTreeH` | i | `10` | <code>int</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element">Element</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.bool.Element">Element</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.bTree()` by jgea-experimenter:2.8.2

### Builder `ea.representation.bitString()`

`ea.r.bitString(factory; mutations; xovers)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `factory` | npm | `ea.r.f.bsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;</code> |
| `mutations` | npm[] | `[ea.r.go.bsFlipMutation()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;&gt;</code> |
| `xovers` | npm[] | `[ea.r.go.composedXover(mutation = ea.r.go.bsFlipMutation(); xover = ea.r.go.bsUniformXover())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.bitString()` by jgea-experimenter:2.8.2

### Builder `ea.representation.cfgTree()`

`ea.r.cfgTree(grammar; minTreeH; maxTreeH)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `grammar` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;N&gt;, <abbr title="io.github.ericmedvet.jgea.core.representation.grammar.string.StringGrammar">StringGrammar</abbr>&lt;N&gt;&gt;</code> |
| `minTreeH` | i | `4` | <code>int</code> |
| `maxTreeH` | i | `16` | <code>int</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;N&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;N&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.cfgTree()` by jgea-experimenter:2.8.2

### Builder `ea.representation.doubleString()`

`ea.r.doubleString(factory; mutations; xovers)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `factory` | npm | `ea.r.f.dsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `mutations` | npm[] | `[ea.r.go.dsGaussianMutation()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;&gt;</code> |
| `xovers` | npm[] | `[ea.r.go.composedXover(mutation = ea.r.go.dsGaussianMutation(); xover = ea.r.go.dsSegmentGeometricXover())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.doubleString()` by jgea-experimenter:2.8.2

### Builder `ea.representation.intString()`

`ea.r.intString(factory; mutations; xovers)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `factory` | npm | `ea.r.f.isUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code> |
| `mutations` | npm[] | `[ea.r.go.isFlipMutation()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;&gt;</code> |
| `xovers` | npm[] | `[ea.r.go.composedXover(mutation = ea.r.go.isFlipMutation(); xover = ea.r.go.isUniformXover())]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.intString()` by jgea-experimenter:2.8.2

### Builder `ea.representation.list()`

`ea.r.list(of; altMutations; altXovers; uniformCrossover)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `altMutations` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;&gt;</code> |
| `altXovers` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;&gt;</code> |
| `uniformCrossover` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.list()` by jgea-experimenter:2.8.2

### Builder `ea.representation.multiBTree()`

`ea.r.multiBTree(of; altMutations; altXovers; uniformCrossover)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.r.bTree()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `altMutations` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;&gt;</code> |
| `altXovers` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;&gt;</code> |
| `uniformCrossover` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.list()` by jgea-experimenter:2.8.2

### Builder `ea.representation.multiSRTree()`

`ea.r.multiSRTree(of; altMutations; altXovers; uniformCrossover)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `ea.r.srTree()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `altMutations` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;&gt;</code> |
| `altXovers` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;&gt;</code> |
| `uniformCrossover` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;G&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.list()` by jgea-experimenter:2.8.2

### Builder `ea.representation.pair()`

`ea.r.pair(first; second)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `first` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G1, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G1&gt;&gt;</code> |
| `second` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G2, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G2&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;G1, G2&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;G1, G2&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.pair()` by jgea-experimenter:2.8.2

### Builder `ea.representation.seeded()`

`ea.r.seeded(of; seeds; seededRate; mutatedRate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `seeds` | npm |  | <code><abbr title="java.util.Collection">Collection</abbr>&lt;G&gt;</code> |
| `seededRate` | d | `0.01` | <code>double</code> |
| `mutatedRate` | d | `0.49` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.seeded()` by jgea-experimenter:2.8.2

### Builder `ea.representation.srTree()`

`ea.r.srTree(constants; operators; minTreeH; maxTreeH; ephemeral; ephemeralMinV; ephemeralMinV; ephemeralSigmaMut; includeVarNameRegex; excludeVarNameRegex)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `constants` | d[] | `[0.1, 1.0, 10.0]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `operators` | e[] | `[+, -, *, p÷, plog]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element$Operator">Element$Operator</abbr>&gt;</code> |
| `minTreeH` | i | `4` | <code>int</code> |
| `maxTreeH` | i | `10` | <code>int</code> |
| `ephemeral` | b | `false` | <code>boolean</code> |
| `ephemeralMinV` | d | `-5.0` | <code>double</code> |
| `ephemeralMinV` | d | `5.0` | <code>double</code> |
| `ephemeralSigmaMut` | d | `0.25` | <code>double</code> |
| `includeVarNameRegex` | s | `.*` | <code><abbr title="java.lang.String">String</abbr></code> |
| `excludeVarNameRegex` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Tree">Tree</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element">Element</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.srTree()` by jgea-experimenter:2.8.2

### Builder `ea.representation.ttpn()`

`ea.r.ttpn(maxNOfGates; maxNOfAttempts; subnetSizeRate; gates; forbiddenGates; avoidDeadGates)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `maxNOfGates` | i | `32` | <code>int</code> |
| `maxNOfAttempts` | i | `10` | <code>int</code> |
| `subnetSizeRate` | d | `0.33` | <code>double</code> |
| `gates` | npm[] | `[ea.ttpn.gate.bAnd(), ea.ttpn.gate.bNot(), ea.ttpn.gate.bOr(), ea.ttpn.gate.bXor(), ea.ttpn.gate.concat(), ea.ttpn.gate.equal(), ea.ttpn.gate.iTh(), ea.ttpn.gate.length(), ea.ttpn.gate.noop(), ea.ttpn.gate.pairer(), ea.ttpn.gate.queuer(), ea.ttpn.gate.select(), ea.ttpn.gate.sequencer(), ea.ttpn.gate.sink(), ea.ttpn.gate.splitter(), ea.ttpn.gate.unpairer(), ea.ttpn.gate.iBefore(), ea.ttpn.gate.iPMathOperator(operator = addition), ea.ttpn.gate.iPMathOperator(operator = subtraction), ea.ttpn.gate.iPMathOperator(operator = multiplication), ea.ttpn.gate.iPMathOperator(operator = division), ea.ttpn.gate.iRange(), ea.ttpn.gate.iSMult(), ea.ttpn.gate.iSPMult(), ea.ttpn.gate.iSPSum(), ea.ttpn.gate.iSSum(), ea.ttpn.gate.iToR(), ea.ttpn.gate.rBefore(), ea.ttpn.gate.repeater(), ea.ttpn.gate.rPMathOperator(operator = addition), ea.ttpn.gate.rPMathOperator(operator = subtraction), ea.ttpn.gate.rPMathOperator(operator = multiplication), ea.ttpn.gate.rPMathOperator(operator = division), ea.ttpn.gate.rSMult(), ea.ttpn.gate.rSPMult(), ea.ttpn.gate.rSPSum(), ea.ttpn.gate.rSSum(), ea.ttpn.gate.rToI(), ea.ttpn.gate.sBefore(), ea.ttpn.gate.sConcat(), ea.ttpn.gate.sSplitter(), ea.ttpn.gate.bConst(value = true), ea.ttpn.gate.iConst(value = 0), ea.ttpn.gate.iConst(value = 1), ea.ttpn.gate.iConst(value = 2), ea.ttpn.gate.iConst(value = 5), ea.ttpn.gate.rConst(value = 0), ea.ttpn.gate.rConst(value = 0.1), ea.ttpn.gate.rConst(value = 0.2), ea.ttpn.gate.rConst(value = 0.5), ea.ttpn.gate.sPSequencer()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr>&gt;</code> |
| `forbiddenGates` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr>&gt;</code> |
| `avoidDeadGates` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network">Network</abbr>, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Network">Network</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Representations.ttpn()` by jgea-experimenter:2.8.2

## Package `ea.representation.factory`

Aliases: `ea.r.f`, `ea.r.factory`, `ea.representation.f`, `ea.representation.factory`

### Builder `ea.representation.factory.bsUniform()`

`ea.r.f.bsUniform()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Factories.bsUniform()` by jgea-experimenter:2.8.2

### Builder `ea.representation.factory.dsUniform()`

`ea.r.f.dsUniform(initialMinV; initialMaxV)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `initialMinV` | d | `-1.0` | <code>double</code> |
| `initialMaxV` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Factories.dsUniform()` by jgea-experimenter:2.8.2

### Builder `ea.representation.factory.isSequential()`

`ea.r.f.isSequential()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Factories.isSequential()` by jgea-experimenter:2.8.2

### Builder `ea.representation.factory.isUniform()`

`ea.r.f.isUniform()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Factories.isUniform()` by jgea-experimenter:2.8.2

### Builder `ea.representation.factory.isUniformUnique()`

`ea.r.f.isUniformUnique()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Factories.isUniformUnique()` by jgea-experimenter:2.8.2

## Package `ea.representation.geneticOperator`

Aliases: `ea.r.geneticOperator`, `ea.r.go`, `ea.representation.geneticOperator`, `ea.representation.go`

### Builder `ea.representation.geneticOperator.bsFlipMutation()`

`ea.r.go.bsFlipMutation(pMutRate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `pMutRate` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.bsFlipMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.bsUniformXover()`

`ea.r.go.bsUniformXover()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.bit.BitString">BitString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.bsUniformXover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.composedMutation()`

`ea.r.go.composedMutation(mutation1; mutation2)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `mutation1` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;</code> |
| `mutation2` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.composedMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.composedXover()`

`ea.r.go.composedXover(xover; mutation)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xover` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;X&gt;&gt;</code> |
| `mutation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;X&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.composedXover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.deterministicXover()`

`ea.r.go.deterministicXover(first)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `first` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;X&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.deterministicXover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.dsGaussianMutation()`

`ea.r.go.dsGaussianMutation(sigmaMut)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `sigmaMut` | d | `0.35` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.dsGaussianMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.dsHypercubeGeometricXover()`

`ea.r.go.dsHypercubeGeometricXover(ext)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ext` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.dsHypercubeGeometricXover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.dsSegmentGeometricXover()`

`ea.r.go.dsSegmentGeometricXover(ext)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ext` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.dsSegmentGeometricXover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.isFlipMutation()`

`ea.r.go.isFlipMutation(pMutRate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `pMutRate` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.isFlipMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.isSymbolCopyMutation()`

`ea.r.go.isSymbolCopyMutation()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.isSymbolCopyMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.isUniformXover()`

`ea.r.go.isUniformXover()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.sequence.integer.IntString">IntString</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.isUniformXover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.listCrossover()`

`ea.r.go.listCrossover(crossover)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `crossover` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;X&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;X&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;X&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.listCrossover()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.listMutation()`

`ea.r.go.listMutation(mutation)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `mutation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;X&gt;, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;X&gt;&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.listMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.oneMutation()`

`ea.r.go.oneMutation(mutations)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `mutations` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Mutation">Mutation</abbr>&lt;X&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.oneMutation()` by jgea-experimenter:2.8.2

### Builder `ea.representation.geneticOperator.oneXover()`

`ea.r.go.oneXover(xovers)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xovers` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;X&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jgea.core.operator.Crossover">Crossover</abbr>&lt;X&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.GeneticOperators.oneXover()` by jgea-experimenter:2.8.2

## Package `ea.schedule`

### Builder `ea.schedule.evenPiecewise()`

`ea.schedule.evenPiecewise(name; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `evenPiecewise[{values}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `values` | d[] | `[0.0, 1.0]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.DoubleUnaryOperator">DoubleUnaryOperator</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Schedules.evenPiecewise()` by jgea-experimenter:2.8.2

### Builder `ea.schedule.flat()`

`ea.schedule.flat(name; value)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `flat[{value}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `value` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.DoubleUnaryOperator">DoubleUnaryOperator</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Schedules.flat()` by jgea-experimenter:2.8.2

### Builder `ea.schedule.linear()`

`ea.schedule.linear(name; min; max)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `linear[{min}:{max}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `min` | d | `0.0` | <code>double</code> |
| `max` | d | `1.0` | <code>double</code> |

Produces <code><abbr title="java.util.function.DoubleUnaryOperator">DoubleUnaryOperator</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Schedules.linear()` by jgea-experimenter:2.8.2

## Package `ea.solver`

Aliases: `ea.s`, `ea.solver`

### Builder `ea.solver.asyncScheduledMfMapElites()`

`ea.s.asyncScheduledMfMapElites(name; representation; mapper; nOfBirthsForIteration; stop; iComparators; descriptors; archive; recomputationRatio; schedule)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `asyncScheduledMfMapElites[{schedule.name};rr={recomputationRatio:%0.2f}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `nOfBirthsForIteration` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition">ProgressBasedStopCondition</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiFidelityMEPopulationState">MultiFidelityMEPopulationState</abbr>&lt;G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.MultifidelityQualityBasedProblem">MultifidelityQualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |
| `descriptors` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `archive` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `recomputationRatio` | d | `0.5` | <code>double</code> |
| `schedule` | npm | `ea.schedule.flat()` | <code><abbr title="java.util.function.DoubleUnaryOperator">DoubleUnaryOperator</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.AsynchronousScheduledMFMapElites">AsynchronousScheduledMFMapElites</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.asyncScheduledMfMapElites()` by jgea-experimenter:2.8.2

### Builder `ea.solver.biGa()`

`ea.s.biGa(name; representation; mapper; crossoverP; tournamentRate; minNTournament; nPop; stop; maxUniquenessAttempts; fitnessReducer; additionalIndividualComparators; opponentsSelector; fitnessAggregator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `biGa` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `tournamentRate` | d | `0.05` | <code>double</code> |
| `minNTournament` | i | `3` | <code>int</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedBiProblem">QualityBasedBiProblem</abbr>&lt;S, O, Q&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `fitnessReducer` | npm |  | <code><abbr title="java.util.function.BinaryOperator">BinaryOperator</abbr>&lt;Q&gt;</code> |
| `additionalIndividualComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |
| `opponentsSelector` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code> |
| `fitnessAggregator` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;Q&gt;, Q&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.bi.StandardBiEvolver">StandardBiEvolver</abbr>&lt;G, S, Q, O&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.biGa()` by jgea-experimenter:2.8.2

### Builder `ea.solver.biMapElites()`

`ea.s.biMapElites(name; representation; mapper; nPop; stop; descriptors; archive; fitnessReducer; emptyArchive; additionalIndividualComparators; opponentsSelector; fitnessAggregator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `biMe` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState">MEPopulationState</abbr>&lt;G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedBiProblem">QualityBasedBiProblem</abbr>&lt;S, O, Q&gt;&gt;&gt;</code> |
| `descriptors` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `archive` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `fitnessReducer` | npm |  | <code><abbr title="java.util.function.BinaryOperator">BinaryOperator</abbr>&lt;Q&gt;</code> |
| `emptyArchive` | b | `false` | <code>boolean</code> |
| `additionalIndividualComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |
| `opponentsSelector` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.bi.AbstractBiEvolver$OpponentsSelector">AbstractBiEvolver$OpponentsSelector</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;, S, Q, O&gt;</code> |
| `fitnessAggregator` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;Q&gt;, Q&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.bi.mapelites.MapElitesBiEvolver">MapElitesBiEvolver</abbr>&lt;G, S, Q, O&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.biMapElites()` by jgea-experimenter:2.8.2

### Builder `ea.solver.cabea()`

`ea.s.cabea(name; representation; mapper; keepProbability; crossoverP; nTour; stop; toroidal; mooreRadius; gridSize; substrate; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `cabea` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `keepProbability` | d | `0.0` | <code>double</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `nTour` | i | `1` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.cabea.GridPopulationState">GridPopulationState</abbr>&lt;G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `toroidal` | b | `true` | <code>boolean</code> |
| `mooreRadius` | i | `1` | <code>int</code> |
| `gridSize` | i | `11` | <code>int</code> |
| `substrate` | e | `EMPTY` | <code><abbr title="io.github.ericmedvet.jgea.core.solver.cabea.SubstrateFiller$Predefined">SubstrateFiller$Predefined</abbr></code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.cabea.CellularAutomataBasedSolver">CellularAutomataBasedSolver</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.cabea()` by jgea-experimenter:2.8.2

### Builder `ea.solver.cmaEs()`

`ea.s.cmaEs(name; mapper; factory; stop)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `cmaEs` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S&gt;</code> |
| `factory` | npm | `ea.r.f.dsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.es.CMAESState">CMAESState</abbr>&lt;S, Q&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.es.CMAEvolutionaryStrategy">CMAEvolutionaryStrategy</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.cmaEs()` by jgea-experimenter:2.8.2

### Builder `ea.solver.coMapElites()`

`ea.s.coMapElites(name; representation1; representation2; mapper1; mapper2; merger; descriptors1; descriptors1; archive1; archive2; stop; populationSize; nOfOffspring; strategy; normalizedNeighborRadius; maxNOfNeighbors; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `coMe-{strategy}-{neighborRadius}-{maxNOfNeighbors}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation1` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G1, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G1&gt;&gt;</code> |
| `representation2` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G2, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G2&gt;&gt;</code> |
| `mapper1` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G1, S1&gt;</code> |
| `mapper2` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G2, S2&gt;</code> |
| `merger` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;S1, S2&gt;, S&gt;</code> |
| `descriptors1` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G1, S1, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `descriptors1` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G2, S2, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `archive1` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `archive2` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMEPopulationState">CoMEPopulationState</abbr>&lt;G1, G2, S1, S2, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `populationSize` | i | `100` | <code>int</code> |
| `nOfOffspring` | i | `50` | <code>int</code> |
| `strategy` | e | `IDENTITY` | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.strategy.CoMEStrategy$Prepared">CoMEStrategy$Prepared</abbr></code> |
| `normalizedNeighborRadius` | d | `0.1` | <code>double</code> |
| `maxNOfNeighbors` | i | `2` | <code>int</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMEIndividual">CoMEIndividual</abbr>&lt;G1, G2, S1, S2, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.CoMapElites">CoMapElites</abbr>&lt;G1, G2, S1, S2, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.coMapElites()` by jgea-experimenter:2.8.2

### Builder `ea.solver.differentialEvolution()`

`ea.s.differentialEvolution(name; mapper; factory; populationSize; stop; differentialWeight; crossoverP; remap)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `de` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S&gt;</code> |
| `factory` | npm | `ea.r.f.dsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `populationSize` | i | `15` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.ListPopulationState">ListPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `differentialWeight` | d | `0.5` | <code>double</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `remap` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.DifferentialEvolution">DifferentialEvolution</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.differentialEvolution()` by jgea-experimenter:2.8.2

### Builder `ea.solver.elitistGa()`

`ea.s.elitistGa(name; representation; mapper; crossoverP; tournamentRate; eliteRate; minNTournament; nPop; stop; maxUniquenessAttempts; remap; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `elitistGa` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `tournamentRate` | d | `0.05` | <code>double</code> |
| `eliteRate` | d | `0.1` | <code>double</code> |
| `minNTournament` | i | `3` | <code>int</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `remap` | b | `false` | <code>boolean</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.StandardEvolver">StandardEvolver</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.elitistGa()` by jgea-experimenter:2.8.2

### Builder `ea.solver.ga()`

`ea.s.ga(name; representation; mapper; crossoverP; tournamentRate; minNTournament; nPop; stop; maxUniquenessAttempts; remap; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `ga` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `tournamentRate` | d | `0.05` | <code>double</code> |
| `minNTournament` | i | `3` | <code>int</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `remap` | b | `false` | <code>boolean</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.StandardEvolver">StandardEvolver</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.ga()` by jgea-experimenter:2.8.2

### Builder `ea.solver.maMapElites2()`

`ea.s.maMapElites2(name; representation; mapper; nPop; stop; descriptors; descriptors; archive; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `maMe2` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MAMEPopulationState">MAMEPopulationState</abbr>&lt;G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `descriptors` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `descriptors` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `archive` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MultiArchiveMapElites">MultiArchiveMapElites</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.maMapElites2()` by jgea-experimenter:2.8.2

### Builder `ea.solver.mapElites()`

`ea.s.mapElites(name; representation; mapper; nPop; stop; descriptors; archive; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `me` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEPopulationState">MEPopulationState</abbr>&lt;G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `descriptors` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `archive` | npm |  | <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MEIndividual">MEIndividual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.MapElites">MapElites</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.mapElites()` by jgea-experimenter:2.8.2

### Builder `ea.solver.nsga2()`

`ea.s.nsga2(name; representation; mapper; crossoverP; nPop; stop; maxUniquenessAttempts; remap; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `nsga2` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.MultiObjectiveProblem">MultiObjectiveProblem</abbr>&lt;S, Q, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `remap` | b | `false` | <code>boolean</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.NsgaII">NsgaII</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.nsga2()` by jgea-experimenter:2.8.2

### Builder `ea.solver.oGraphea()`

`ea.s.oGraphea(name; mapper; minConst; maxConst; nConst; operators; nPop; stop; arcAdditionRate; arcRemovalRate; nodeAdditionRate; nPop; rankBase; remap; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `oGraphea` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Graph">Graph</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Node">Node</abbr>, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph$NonValuedArc">OperatorGraph$NonValuedArc</abbr>&gt;, S&gt;</code> |
| `minConst` | d | `0.0` | <code>double</code> |
| `maxConst` | d | `5.0` | <code>double</code> |
| `nConst` | i | `10` | <code>int</code> |
| `operators` | e[] | `[+, -, *, p/, plog]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.BaseOperator">BaseOperator</abbr>&gt;</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedPOCPopulationState">SpeciatedPOCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Graph">Graph</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Node">Node</abbr>, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph$NonValuedArc">OperatorGraph$NonValuedArc</abbr>&gt;, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `arcAdditionRate` | d | `3.0` | <code>double</code> |
| `arcRemovalRate` | d | `0.1` | <code>double</code> |
| `nodeAdditionRate` | d | `1.0` | <code>double</code> |
| `nPop` | i | `5` | <code>int</code> |
| `rankBase` | d | `0.75` | <code>double</code> |
| `remap` | b | `false` | <code>boolean</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Graph">Graph</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Node">Node</abbr>, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph$NonValuedArc">OperatorGraph$NonValuedArc</abbr>&gt;, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.speciation.SpeciatedEvolver">SpeciatedEvolver</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Graph">Graph</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.representation.graph.Node">Node</abbr>, <abbr title="io.github.ericmedvet.jgea.core.representation.graph.numeric.operatorgraph.OperatorGraph$NonValuedArc">OperatorGraph$NonValuedArc</abbr>&gt;, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.oGraphea()` by jgea-experimenter:2.8.2

### Builder `ea.solver.openAiEs()`

`ea.s.openAiEs(name; mapper; factory; sigma; batchSize; stepSize; beta1; beta2; epsilon; stop)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `openAiEs` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S&gt;</code> |
| `factory` | npm | `ea.r.f.dsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `sigma` | d | `0.02` | <code>double</code> |
| `batchSize` | i | `30` | <code>int</code> |
| `stepSize` | d | `0.02` | <code>double</code> |
| `beta1` | d | `0.9` | <code>double</code> |
| `beta2` | d | `0.999` | <code>double</code> |
| `epsilon` | d | `1.0E-8` | <code>double</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.es.OpenAIESState">OpenAIESState</abbr>&lt;S, Q&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.es.OpenAIEvolutionaryStrategy">OpenAIEvolutionaryStrategy</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.openAiEs()` by jgea-experimenter:2.8.2

### Builder `ea.solver.pso()`

`ea.s.pso(name; mapper; factory; stop; nPop; w; phiParticle; phiGlobal)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `pso` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S&gt;</code> |
| `factory` | npm | `ea.r.f.dsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.pso.PSOState">PSOState</abbr>&lt;S, Q&gt;&gt;</code> |
| `nPop` | i | `100` | <code>int</code> |
| `w` | d | `0.8` | <code>double</code> |
| `phiParticle` | d | `1.5` | <code>double</code> |
| `phiGlobal` | d | `1.5` | <code>double</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.pso.ParticleSwarmOptimization">ParticleSwarmOptimization</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.pso()` by jgea-experimenter:2.8.2

### Builder `ea.solver.randomSearch()`

`ea.s.randomSearch(name; representation; mapper; stop; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `rs` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.RandomSearch">RandomSearch</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.randomSearch()` by jgea-experimenter:2.8.2

### Builder `ea.solver.randomWalk()`

`ea.s.randomWalk(name; representation; mapper; stop; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `rw` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.RandomWalk">RandomWalk</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.randomWalk()` by jgea-experimenter:2.8.2

### Builder `ea.solver.scheduledMfGa()`

`ea.s.scheduledMfGa(name; representation; mapper; crossoverP; tournamentRate; minNTournament; nPop; stop; maxUniquenessAttempts; iComparators; schedule)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `scheduledMfGa[{schedule.name}]` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `tournamentRate` | d | `0.05` | <code>double</code> |
| `minNTournament` | i | `3` | <code>int</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition">ProgressBasedStopCondition</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.MultiFidelityPOCPopulationState">MultiFidelityPOCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.MultifidelityQualityBasedProblem">MultifidelityQualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |
| `schedule` | npm | `ea.schedule.flat()` | <code><abbr title="java.util.function.DoubleUnaryOperator">DoubleUnaryOperator</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.multifidelity.ScheduledFidelityStandardEvolver">ScheduledFidelityStandardEvolver</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.scheduledMfGa()` by jgea-experimenter:2.8.2

### Builder `ea.solver.simpleEs()`

`ea.s.simpleEs(name; mapper; factory; sigma; parentsRate; nOfElites; nPop; stop; remap)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `es` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapper` | npm | `ea.m.identity()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S&gt;</code> |
| `factory` | npm | `ea.r.f.dsUniform()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="io.github.ericmedvet.jgea.core.Factory">Factory</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;</code> |
| `sigma` | d | `0.35` | <code>double</code> |
| `parentsRate` | d | `0.33` | <code>double</code> |
| `nOfElites` | i | `1` | <code>int</code> |
| `nPop` | i | `30` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.ListPopulationState">ListPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.TotalOrderQualityBasedProblem">TotalOrderQualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `remap` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.es.SimpleEvolutionaryStrategy">SimpleEvolutionaryStrategy</abbr>&lt;S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.simpleEs()` by jgea-experimenter:2.8.2

### Builder `ea.solver.srGp()`

`ea.s.srGp(name; representation; mapper; crossoverP; tournamentRate; minNTournament; nPop; stop; maxUniquenessAttempts; remap; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `srGp` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm | `ea.r.srTree()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.srTreeToNurf()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.8` | <code>double</code> |
| `tournamentRate` | d | `0.05` | <code>double</code> |
| `minNTournament` | i | `3` | <code>int</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `remap` | b | `false` | <code>boolean</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.StandardEvolver">StandardEvolver</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.ga()` by jgea-experimenter:2.8.2

### Builder `ea.solver.ttpnGp()`

`ea.s.ttpnGp(name; representation; mapper; crossoverP; tournamentRate; minNTournament; nPop; stop; maxUniquenessAttempts; remap; iComparators)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `ttpnGp` | <code><abbr title="java.lang.String">String</abbr></code> |
| `representation` | npm | `ea.r.ttpn()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;G, <abbr title="io.github.ericmedvet.jgea.experimenter.Representation">Representation</abbr>&lt;G&gt;&gt;</code> |
| `mapper` | npm | `ea.m.ttpnToProgram()` | <code><abbr title="io.github.ericmedvet.jgea.core.InvertibleMapper">InvertibleMapper</abbr>&lt;G, S&gt;</code> |
| `crossoverP` | d | `0.5` | <code>double</code> |
| `tournamentRate` | d | `0.05` | <code>double</code> |
| `minNTournament` | i | `3` | <code>int</code> |
| `nPop` | i | `100` | <code>int</code> |
| `stop` | npm | `ea.sc.nOfQualityEvaluations()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;, G, S, Q, <abbr title="io.github.ericmedvet.jgea.core.problem.QualityBasedProblem">QualityBasedProblem</abbr>&lt;S, Q&gt;&gt;&gt;</code> |
| `maxUniquenessAttempts` | i | `100` | <code>int</code> |
| `remap` | b | `false` | <code>boolean</code> |
| `iComparators` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.order.PartialComparator">PartialComparator</abbr>&lt;? super <abbr title="io.github.ericmedvet.jgea.core.solver.Individual">Individual</abbr>&lt;G, S, Q&gt;&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;S, <abbr title="io.github.ericmedvet.jgea.core.solver.StandardEvolver">StandardEvolver</abbr>&lt;G, S, Q&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.Solvers.ga()` by jgea-experimenter:2.8.2

## Package `ea.solver.mapelites.archive`

Aliases: `ea.s.mapelites.a`, `ea.s.mapelites.archive`, `ea.s.me.a`, `ea.s.me.archive`, `ea.solver.mapelites.a`, `ea.solver.mapelites.archive`, `ea.solver.me.a`, `ea.solver.me.archive`

### Builder `ea.solver.mapelites.archive.grid()`

`ea.s.me.a.grid(ranges; nsOfBins)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ranges` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr>&gt;</code> |
| `nsOfBins` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.ArchiveProviders.grid()` by jgea-experimenter:2.8.2

### Builder `ea.solver.mapelites.archive.grid2d()`

`ea.s.me.a.grid2d(ranges1; ranges2; nOfBins1; nOfBins2)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ranges1` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `ranges2` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `nOfBins1` | i |  | <code>int</code> |
| `nOfBins2` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.ArchiveProviders.grid2d()` by jgea-experimenter:2.8.2

### Builder `ea.solver.mapelites.archive.voronoi()`

`ea.s.me.a.voronoi(ranges1; ranges2; nOfPoints; randomGenerator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `ranges1` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `ranges2` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `nOfPoints` | i | `100` | <code>int</code> |
| `randomGenerator` | npm | `m.defaultRG()` | <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.mapelites.archive.NumericalKeyArchive$Provider">NumericalKeyArchive$Provider</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.ArchiveProviders.voronoi()` by jgea-experimenter:2.8.2

## Package `ea.stoppingCriterion`

Aliases: `ea.sc`, `ea.stoppingCriterion`

### Builder `ea.stoppingCriterion.cumulativeFidelity()`

`ea.sc.cumulativeFidelity(v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `v` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition">ProgressBasedStopCondition</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, P&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StoppingCriteria.cumulativeFidelity()` by jgea-experimenter:2.8.2

### Builder `ea.stoppingCriterion.elapsed()`

`ea.sc.elapsed(v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `v` | d | `10.0` | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition">ProgressBasedStopCondition</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.State">State</abbr>&lt;P, S&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StoppingCriteria.elapsed()` by jgea-experimenter:2.8.2

### Builder `ea.stoppingCriterion.nOfIterations()`

`ea.sc.nOfIterations(n)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i | `100` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition">ProgressBasedStopCondition</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.State">State</abbr>&lt;P, S&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StoppingCriteria.nOfIterations()` by jgea-experimenter:2.8.2

### Builder `ea.stoppingCriterion.nOfQualityEvaluations()`

`ea.sc.nOfQualityEvaluations(n)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i | `1000` | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.solver.ProgressBasedStopCondition">ProgressBasedStopCondition</abbr>&lt;<abbr title="io.github.ericmedvet.jgea.core.solver.POCPopulationState">POCPopulationState</abbr>&lt;I, G, S, Q, P&gt;&gt;</code>; built from `io.github.ericmedvet.jgea.experimenter.builders.StoppingCriteria.nOfQualityEvaluations()` by jgea-experimenter:2.8.2

## Package `ea.ttpn.gate`

### Builder `ea.ttpn.gate.bAnd()`

`ea.ttpn.gate.bAnd()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.bAnd()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.bConst()`

`ea.ttpn.gate.bConst(value)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `value` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.bConst()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.bNot()`

`ea.ttpn.gate.bNot()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.bNot()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.bOr()`

`ea.ttpn.gate.bOr()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.bOr()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.bXor()`

`ea.ttpn.gate.bXor()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.bXor()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.concat()`

`ea.ttpn.gate.concat()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.concat()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.equal()`

`ea.ttpn.gate.equal()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.equal()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iBefore()`

`ea.ttpn.gate.iBefore()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iBefore()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iConst()`

`ea.ttpn.gate.iConst(value)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `value` | i |  | <code>int</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iConst()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iPMathOperator()`

`ea.ttpn.gate.iPMathOperator(operator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `operator` | e |  | <code><abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element$Operator">Element$Operator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iPMathOperator()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iRange()`

`ea.ttpn.gate.iRange()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iRange()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iSMult()`

`ea.ttpn.gate.iSMult()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iSMult()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iSPMult()`

`ea.ttpn.gate.iSPMult()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iSPMult()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iSPSum()`

`ea.ttpn.gate.iSPSum()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iSPSum()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iSSum()`

`ea.ttpn.gate.iSSum()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iSSum()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iTh()`

`ea.ttpn.gate.iTh()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iTh()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.iToR()`

`ea.ttpn.gate.iToR()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.iToR()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.length()`

`ea.ttpn.gate.length()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.length()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.noop()`

`ea.ttpn.gate.noop()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.noop()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.pairer()`

`ea.ttpn.gate.pairer()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.pairer()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.queuer()`

`ea.ttpn.gate.queuer()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.queuer()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rBefore()`

`ea.ttpn.gate.rBefore()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rBefore()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rConst()`

`ea.ttpn.gate.rConst(value)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `value` | d |  | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rConst()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rPMathOperator()`

`ea.ttpn.gate.rPMathOperator(operator)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `operator` | e |  | <code><abbr title="io.github.ericmedvet.jgea.core.representation.tree.numeric.Element$Operator">Element$Operator</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rPMathOperator()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rSMult()`

`ea.ttpn.gate.rSMult()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rSMult()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rSPMult()`

`ea.ttpn.gate.rSPMult()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rSPMult()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rSPSum()`

`ea.ttpn.gate.rSPSum()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rSPSum()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rSSum()`

`ea.ttpn.gate.rSSum()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rSSum()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.rToI()`

`ea.ttpn.gate.rToI()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.rToI()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.repeater()`

`ea.ttpn.gate.repeater()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.repeater()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sBefore()`

`ea.ttpn.gate.sBefore()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sBefore()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sConcat()`

`ea.ttpn.gate.sConcat()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sConcat()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sConst()`

`ea.ttpn.gate.sConst(value)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `value` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sConst()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sPSequencer()`

`ea.ttpn.gate.sPSequencer()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sPSequencer()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sSplitter()`

`ea.ttpn.gate.sSplitter()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sSplitter()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.select()`

`ea.ttpn.gate.select()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.select()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sequencer()`

`ea.ttpn.gate.sequencer()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sequencer()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.sink()`

`ea.ttpn.gate.sink()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.sink()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.splitter()`

`ea.ttpn.gate.splitter()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.splitter()` by jgea-experimenter:2.8.2

### Builder `ea.ttpn.gate.unpairer()`

`ea.ttpn.gate.unpairer()`

Produces <code><abbr title="io.github.ericmedvet.jgea.core.representation.programsynthesis.ttpn.Gate">Gate</abbr></code>; built from `io.github.ericmedvet.jgea.experimenter.builders.NetworkGates.unpairer()` by jgea-experimenter:2.8.2

## Package `function`

Aliases: `f`, `function`

### Builder `function.all()`

`f.all(of; fs; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `fs` | npm[] | `[f.identity()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;T, K&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.all()` by jgea-experimenter:2.8.2

### Builder `function.allNamed()`

`f.allNamed(of; fs; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `fs` | npm |  | <code><abbr title="java.util.SequencedMap">SequencedMap</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.util.function.Function">Function</abbr>&lt;T, K&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.SequencedMap">SequencedMap</abbr>&lt;<abbr title="java.lang.String">String</abbr>, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.allNamed()` by jgea-experimenter:2.8.2

### Builder `function.any()`

`f.any(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.any()` by jgea-experimenter:2.8.2

### Builder `function.as()`

`f.as(of; name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, Y&gt;</code> |
| `name` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, Y&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.as()` by jgea-experimenter:2.8.2

### Builder `function.avg()`

`f.avg(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.avg()` by jgea-experimenter:2.8.2

### Builder `function.cached()`

`f.cached(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, R&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;T, R&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.cached()` by jgea-experimenter:2.8.2

### Builder `function.classSimpleName()`

`f.classSimpleName(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.classSimpleName()` by jgea-experimenter:2.8.2

### Builder `function.clip()`

`f.clip(of; range; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `range` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.clip()` by jgea-experimenter:2.8.2

### Builder `function.composition()`

`f.composition(of; then)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, Z&gt;</code> |
| `then` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;Z, Y&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, Y&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.composition()` by jgea-experimenter:2.8.2

### Builder `function.copy()`

`f.copy(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `copy` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.copy()` by jgea-experimenter:2.8.2

### Builder `function.distinct()`

`f.distinct(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Set">Set</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.distinct()` by jgea-experimenter:2.8.2

### Builder `function.distinctByKey()`

`f.distinctByKey(of; key; representer; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `key` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, K&gt;</code> |
| `representer` | npm | `f.any()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;, T&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Set">Set</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.distinctByKey()` by jgea-experimenter:2.8.2

### Builder `function.distinctSortedByKey()`

`f.distinctSortedByKey(of; key; representer; format; sort)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `key` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, K&gt;</code> |
| `representer` | npm | `f.first(of = f.sortedBy(by = null))` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;, T&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |
| `sort` | npm | `` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Set">Set</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.distinctByKey()` by jgea-experimenter:2.8.2

### Builder `function.eGridString()`

`f.eGridString(name; of; representations; separator; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `grid.string` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `representations` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `separator` | s | `;` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.eGridString()` by jgea-experimenter:2.8.2

### Builder `function.each()`

`f.each(mapF; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `mapF` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, R&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;R&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.each()` by jgea-experimenter:2.8.2

### Builder `function.emptySplitter()`

`f.emptySplitter(of; name; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `name` | s | `empty` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.emptySplitter()` by jgea-experimenter:2.8.2

### Builder `function.filter()`

`f.filter(condition; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `condition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;T&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.filter()` by jgea-experimenter:2.8.2

### Builder `function.first()`

`f.first(n; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i | `0` | <code>int</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.nTh()` by jgea-experimenter:2.8.2

### Builder `function.fittedGrid()`

`f.fittedGrid(name; of; predicate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `fitted.grid` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.fittedGrid()` by jgea-experimenter:2.8.2

### Builder `function.flat()`

`f.flat(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;? extends <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;&gt;</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.flat()` by jgea-experimenter:2.8.2

### Builder `function.format()`

`f.format(name; of; functions; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{functions}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `functions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;T, ?&gt;&gt;</code> |
| `format` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.format()` by jgea-experimenter:2.8.2

### Builder `function.fromBase64()`

`f.fromBase64(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.fromBase64()` by jgea-experimenter:2.8.2

### Builder `function.gridCompactness()`

`f.gridCompactness(predicate; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridCompactness()` by jgea-experimenter:2.8.2

### Builder `function.gridCount()`

`f.gridCount(predicate; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridCount()` by jgea-experimenter:2.8.2

### Builder `function.gridCoverage()`

`f.gridCoverage(predicate; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridCoverage()` by jgea-experimenter:2.8.2

### Builder `function.gridElongation()`

`f.gridElongation(predicate; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridElongation()` by jgea-experimenter:2.8.2

### Builder `function.gridFitH()`

`f.gridFitH(predicate; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridFitH()` by jgea-experimenter:2.8.2

### Builder `function.gridFitW()`

`f.gridFitW(predicate; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `predicate` | npm | `f.nonNull()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridFitW()` by jgea-experimenter:2.8.2

### Builder `function.gridH()`

`f.gridH(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridH()` by jgea-experimenter:2.8.2

### Builder `function.gridString()`

`f.gridString(name; of; elementF; separator; format; nullString; byRows; yMirror; xMirror)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `grid.string` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;&gt;</code> |
| `elementF` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `separator` | s | `;` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |
| `nullString` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `byRows` | b | `false` | <code>boolean</code> |
| `yMirror` | b | `false` | <code>boolean</code> |
| `xMirror` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridString()` by jgea-experimenter:2.8.2

### Builder `function.gridW()`

`f.gridW(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%2d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.gridW()` by jgea-experimenter:2.8.2

### Builder `function.iApply()`

`f.iApply(iF; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `iF` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.function.Function">Function</abbr>&lt;T, R&gt;&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, R&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.iApply()` by jgea-experimenter:2.8.2

### Builder `function.iComposition()`

`f.iComposition(of; before; then)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.function.Function">Function</abbr>&lt;B, C&gt;&gt;</code> |
| `before` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;A, B&gt;</code> |
| `then` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;C, D&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.function.Function">Function</abbr>&lt;A, D&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.iComposition()` by jgea-experimenter:2.8.2

### Builder `function.iEach()`

`f.iEach(iMapF; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `iMapF` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.function.Function">Function</abbr>&lt;T, R&gt;&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;R&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.iEach()` by jgea-experimenter:2.8.2

### Builder `function.identity()`

`f.identity()`

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.identity()` by jgea-experimenter:2.8.2

### Builder `function.inner()`

`f.inner(name; of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `inner` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Composed">Composed</abbr>&lt;C&gt;&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, C&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.inner()` by jgea-experimenter:2.8.2

### Builder `function.interpolated()`

`f.interpolated(name; s; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{s}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `s` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.interpolated()` by jgea-experimenter:2.8.2

### Builder `function.last()`

`f.last(n; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i | `-1` | <code>int</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.nTh()` by jgea-experimenter:2.8.2

### Builder `function.mapAggregate()`

`f.mapAggregate(name; of; aggregateF; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{aggregateF}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, T&gt;&gt;&gt;</code> |
| `aggregateF` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;T&gt;, K&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.mapAggregate()` by jgea-experimenter:2.8.2

### Builder `function.mapValue()`

`f.mapValue(key; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `key` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.mapValue()` by jgea-experimenter:2.8.2

### Builder `function.mappableKey()`

`f.mappableKey(name; key; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `{key}` | <code><abbr title="java.lang.String">String</abbr></code> |
| `key` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.core.Mappable">Mappable</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.mappableKey()` by jgea-experimenter:2.8.2

### Builder `function.mapper()`

`f.mapper(name; of; map)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `mapper` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `map` | npm |  | <code><abbr title="java.util.Map">Map</abbr>&lt;T, K&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, K&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.mapper()` by jgea-experimenter:2.8.2

### Builder `function.mathConst()`

`f.mathConst(v; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `v` | d |  | <code>double</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.mathConst()` by jgea-experimenter:2.8.2

### Builder `function.mathOp()`

`f.mathOp(of; args; op; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, Y&gt;</code> |
| `args` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;Y, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `op` | e |  | <code><abbr title="io.github.ericmedvet.jnb.buildable.MathOp">MathOp</abbr></code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.mathOp()` by jgea-experimenter:2.8.2

### Builder `function.max()`

`f.max(of; by; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `by` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, C&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.max()` by jgea-experimenter:2.8.2

### Builder `function.median()`

`f.median(p; by; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `p` | d | `50` | <code>double</code> |
| `by` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, C&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.percentile()` by jgea-experimenter:2.8.2

### Builder `function.min()`

`f.min(of; by; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `by` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, C&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.min()` by jgea-experimenter:2.8.2

### Builder `function.nTh()`

`f.nTh(n; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i |  | <code>int</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.nTh()` by jgea-experimenter:2.8.2

### Builder `function.nThMapValue()`

`f.nThMapValue(n; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i |  | <code>int</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.SequencedMap">SequencedMap</abbr>&lt;<abbr title="java.lang.String">String</abbr>, T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.nThMapValue()` by jgea-experimenter:2.8.2

### Builder `function.namedMerged()`

`f.namedMerged(name; of; mappers; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | interpolate `merged` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `mappers` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;T, <abbr title="java.util.SequencedMap">SequencedMap</abbr>&lt;<abbr title="java.lang.String">String</abbr>, K&gt;&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.SequencedMap">SequencedMap</abbr>&lt;<abbr title="java.lang.String">String</abbr>, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.namedMerged()` by jgea-experimenter:2.8.2

### Builder `function.nkTh()`

`f.nkTh(n; k; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i |  | <code>int</code> |
| `k` | i |  | <code>int</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.nkTh()` by jgea-experimenter:2.8.2

### Builder `function.nonNull()`

`f.nonNull(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Boolean">Boolean</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.nonNull()` by jgea-experimenter:2.8.2

### Builder `function.orElseD()`

`f.orElseD(of; v; format; name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `v` | d | `0.0` | <code>double</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `name` | s | interpolate `orElseD[{v}]` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.orElseD()` by jgea-experimenter:2.8.2

### Builder `function.orElseI()`

`f.orElseI(of; v; format; name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `v` | i | `0` | <code>int</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |
| `name` | s | interpolate `orElseD[{v}]` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.orElseI()` by jgea-experimenter:2.8.2

### Builder `function.pairFirst()`

`f.pairFirst(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `first` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;F, S&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, F&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.pairFirst()` by jgea-experimenter:2.8.2

### Builder `function.pairSecond()`

`f.pairSecond(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `second` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;F, S&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, S&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.pairSecond()` by jgea-experimenter:2.8.2

### Builder `function.pairerFirst()`

`f.pairerFirst(of; second; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, F&gt;</code> |
| `second` | npm |  | <code>S</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;F, S&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.pairerFirst()` by jgea-experimenter:2.8.2

### Builder `function.pairerSecond()`

`f.pairerSecond(of; first; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, S&gt;</code> |
| `first` | npm |  | <code>F</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;F, S&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.pairerSecond()` by jgea-experimenter:2.8.2

### Builder `function.percentile()`

`f.percentile(p; by; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `p` | d |  | <code>double</code> |
| `by` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, C&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.percentile()` by jgea-experimenter:2.8.2

### Builder `function.quantized()`

`f.quantized(q; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `q` | d |  | <code>double</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.quantized()` by jgea-experimenter:2.8.2

### Builder `function.replaceAll()`

`f.replaceAll(of; regex; replacement; format; name)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `regex` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `replacement` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |
| `name` | s | interpolate `replace.all` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.replaceAll()` by jgea-experimenter:2.8.2

### Builder `function.sd()`

`f.sd(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.sd()` by jgea-experimenter:2.8.2

### Builder `function.size()`

`f.size(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.size()` by jgea-experimenter:2.8.2

### Builder `function.sizeIf()`

`f.sizeIf(of; format; allF; mapF; predicate)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.filter(condition = predicate.always(); of = f.each(mapF = f.identity(); of = f.identity()))` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `allF` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapF` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `predicate` | npm | `predicate.always()` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.size()` by jgea-experimenter:2.8.2

### Builder `function.sizeIfGt()`

`f.sizeIfGt(of; format; allF; mapF; predicate; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.filter(condition = predicate.always(); of = f.each(mapF = f.identity(); of = f.identity()))` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `allF` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapF` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `predicate` | npm | `predicate.gt(t = 0)` | <code><abbr title="java.lang.String">String</abbr></code> |
| `t` | d | `0.0` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.size()` by jgea-experimenter:2.8.2

### Builder `function.sizeIfLt()`

`f.sizeIfLt(of; format; allF; mapF; predicate; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.filter(condition = predicate.always(); of = f.each(mapF = f.identity(); of = f.identity()))` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%3d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `allF` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mapF` | npm | `f.identity()` | <code><abbr title="java.lang.String">String</abbr></code> |
| `predicate` | npm | `predicate.lt(t = 0)` | <code><abbr title="java.lang.String">String</abbr></code> |
| `t` | d | `0.0` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.size()` by jgea-experimenter:2.8.2

### Builder `function.sortedBy()`

`f.sortedBy(by; of; reversed; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `by` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;T, K&gt;</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;T&gt;&gt;</code> |
| `reversed` | b | `false` | <code>boolean</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.sortedBy()` by jgea-experimenter:2.8.2

### Builder `function.subList()`

`f.subList(from; to; relative; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `from` | d |  | <code>double</code> |
| `to` | d |  | <code>double</code> |
| `relative` | b | `true` | <code>boolean</code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.subList()` by jgea-experimenter:2.8.2

### Builder `function.sum()`

`f.sum(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `format` | s | `%.1f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.sum()` by jgea-experimenter:2.8.2

### Builder `function.tdaMapToTable()`

`f.tdaMapToTable(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `to.table` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, double[]&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Table">Table</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Integer">Integer</abbr>, <abbr title="java.lang.Double">Double</abbr>&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.tdaMapToTable()` by jgea-experimenter:2.8.2

### Builder `function.timestamp()`

`f.timestamp(format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `format` | s | `%1$tH:%1$tM:%1$tS` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.time.LocalDateTime">LocalDateTime</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.timestamp()` by jgea-experimenter:2.8.2

### Builder `function.toBase64()`

`f.toBase64(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.toBase64()` by jgea-experimenter:2.8.2

### Builder `function.toString()`

`f.toString(name; of; nullString; keepNull; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `to.string` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code> |
| `nullString` | s | `` | <code><abbr title="java.lang.String">String</abbr></code> |
| `keepNull` | b | `false` | <code>boolean</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.toString()` by jgea-experimenter:2.8.2

### Builder `function.toaMapToTable()`

`f.toaMapToTable(name; of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `name` | s | `to.table` | <code><abbr title="java.lang.String">String</abbr></code> |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, T[]&gt;&gt;</code> |
| `format` | s | `%s` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jnb.datastructure.Table">Table</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>, <abbr title="java.lang.Integer">Integer</abbr>, T&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.toaMapToTable()` by jgea-experimenter:2.8.2

### Builder `function.uniqueness()`

`f.uniqueness(of; format)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.Collection">Collection</abbr>&lt;?&gt;&gt;</code> |
| `format` | s | `%5.3f` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction">FormattedNamedFunction</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Functions.uniqueness()` by jgea-experimenter:2.8.2

## Package `listener`

Aliases: `list`, `listener`

### Builder `listener.console()`

`list.console(defaultEFunctions; eFunctions; defaultKFunctions; kFunctions; deferred; onlyLast; eCondition; kCondition; logExceptions)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `defaultEFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `eFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `defaultKFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `logExceptions` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.console()` by jgea-experimenter:2.8.2

### Builder `listener.csv()`

`list.csv(defaultEFunctions; eFunctions; defaultKFunctions; kFunctions; deferred; onlyLast; eCondition; kCondition; path; errorString; intFormat; doubleFormat)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `defaultEFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `eFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;E, ?&gt;&gt;</code> |
| `defaultKFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `kFunctions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;K, ?&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |
| `path` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |
| `errorString` | s | `NA` | <code><abbr title="java.lang.String">String</abbr></code> |
| `intFormat` | s | `%d` | <code><abbr title="java.lang.String">String</abbr></code> |
| `doubleFormat` | s | `%.5e` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.csv()` by jgea-experimenter:2.8.2

### Builder `listener.onDone()`

`list.onDone(of; preprocessor; consumers; deferred; eCondition; kCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[ea.consumer.deaf()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onDone()` by jgea-experimenter:2.8.2

### Builder `listener.onKDone()`

`list.onKDone(of; preprocessor; consumers; deferred; eCondition; kCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code><abbr title="io.github.ericmedvet.jnb.datastructure.AccumulatorFactory">AccumulatorFactory</abbr>&lt;E, O, K&gt;</code> |
| `preprocessor` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super O, ? extends P&gt;</code> |
| `consumers` | npm[] | `[ea.consumer.deaf()]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.BiConsumer">BiConsumer</abbr>&lt;? super P, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;E&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;E, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.onKDone()` by jgea-experimenter:2.8.2

### Builder `listener.split()`

`list.split(splitter; inner; deferred; onlyLast; eCondition; kCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `splitter` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;OE, <abbr title="java.util.Collection">Collection</abbr>&lt;IE&gt;&gt;</code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;IE, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;OE&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;OE, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.split()` by jgea-experimenter:2.8.2

### Builder `listener.splitPair()`

`list.splitPair(splitter; inner; deferred; onlyLast; eCondition; kCondition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `splitter` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;OE, <abbr title="java.util.Collection">Collection</abbr>&lt;IE&gt;&gt;</code> |
| `inner` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;<abbr title="io.github.ericmedvet.jnb.datastructure.Pair">Pair</abbr>&lt;OE, IE&gt;, K&gt;&gt;</code> |
| `deferred` | b | `false` | <code>boolean</code> |
| `onlyLast` | b | `false` | <code>boolean</code> |
| `eCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;OE&gt;</code> |
| `kCondition` | npm | `predicate.always()` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;K&gt;</code> |

Produces <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.concurrent.Executor">Executor</abbr>, <abbr title="io.github.ericmedvet.jnb.datastructure.ListenerFactory">ListenerFactory</abbr>&lt;OE, K&gt;&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Listeners.splitPair()` by jgea-experimenter:2.8.2

## Package `misc`

Aliases: `m`, `misc`

### Builder `misc.defaultRG()`

`m.defaultRG(seed)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `seed` | i | `0` | <code>int</code> |

Produces <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.defaultRG()` by jgea-experimenter:2.8.2

### Builder `misc.grid()`

`m.grid(w; h; items)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `w` | i |  | <code>int</code> |
| `h` | i |  | <code>int</code> |
| `items` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;T&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.grid()` by jgea-experimenter:2.8.2

### Builder `misc.map()`

`m.map(keys; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `keys` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;K&gt;</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;V&gt;</code> |

Produces <code><abbr title="java.util.Map">Map</abbr>&lt;K, V&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.map()` by jgea-experimenter:2.8.2

### Builder `misc.nTh()`

`m.nTh(n; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `n` | i |  | <code>int</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;T&gt;</code> |

Produces <code>T</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.nTh()` by jgea-experimenter:2.8.2

### Builder `misc.nullValue()`

`m.nullValue()`

Produces <code><abbr title="java.lang.Object">Object</abbr></code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.nullValue()` by jgea-experimenter:2.8.2

### Builder `misc.range()`

`m.range(min; max)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `min` | d |  | <code>double</code> |
| `max` | d |  | <code>double</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.range()` by jgea-experimenter:2.8.2

### Builder `misc.sKeyMap()`

`m.sKeyMap(keys; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `keys` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;V&gt;</code> |

Produces <code><abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, V&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.sKeyMap()` by jgea-experimenter:2.8.2

### Builder `misc.sMap()`

`m.sMap(keys; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `keys` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.lang.String">String</abbr>, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.sMap()` by jgea-experimenter:2.8.2

### Builder `misc.sValueMap()`

`m.sValueMap(keys; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `keys` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;K&gt;</code> |
| `values` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="java.util.Map">Map</abbr>&lt;K, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.sValueMap()` by jgea-experimenter:2.8.2

### Builder `misc.sharedRG()`

`m.sharedRG(seed)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `seed` | i | `0` | <code>int</code> |

Produces <code><abbr title="java.util.random.RandomGenerator">RandomGenerator</abbr></code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.sharedRG()` by jgea-experimenter:2.8.2

### Builder `misc.supplier()`

`m.supplier(of)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm |  | <code>T</code> |

Produces <code><abbr title="java.util.function.Supplier">Supplier</abbr>&lt;T&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Miscs.supplier()` by jgea-experimenter:2.8.2

## Package `predicate`

### Builder `predicate.all()`

`predicate.all(conditions)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `conditions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.all()` by jgea-experimenter:2.8.2

### Builder `predicate.always()`

`predicate.always()`

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;?&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.always()` by jgea-experimenter:2.8.2

### Builder `predicate.any()`

`predicate.any(conditions)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `conditions` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.any()` by jgea-experimenter:2.8.2

### Builder `predicate.divisibleBy()`

`predicate.divisibleBy(f; divisor)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `divisor` | d |  | <code>double</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.divisibleBy()` by jgea-experimenter:2.8.2

### Builder `predicate.eq()`

`predicate.eq(f; v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, T&gt;</code> |
| `v` | npm |  | <code>T</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.eq()` by jgea-experimenter:2.8.2

### Builder `predicate.eqD()`

`predicate.eqD(f; v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `v` | d |  | <code><abbr title="java.lang.Double">Double</abbr></code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.eqD()` by jgea-experimenter:2.8.2

### Builder `predicate.eqI()`

`predicate.eqI(f; v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `v` | i |  | <code><abbr title="java.lang.Integer">Integer</abbr></code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.eqI()` by jgea-experimenter:2.8.2

### Builder `predicate.eqS()`

`predicate.eqS(f; v)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `v` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.eqS()` by jgea-experimenter:2.8.2

### Builder `predicate.gt()`

`predicate.gt(f; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `t` | d |  | <code>double</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.gt()` by jgea-experimenter:2.8.2

### Builder `predicate.gtEq()`

`predicate.gtEq(f; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `t` | d |  | <code>double</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.gtEq()` by jgea-experimenter:2.8.2

### Builder `predicate.inD()`

`predicate.inD(f; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Double">Double</abbr>&gt;</code> |
| `values` | d[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.inD()` by jgea-experimenter:2.8.2

### Builder `predicate.inI()`

`predicate.inI(f; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |
| `values` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.inI()` by jgea-experimenter:2.8.2

### Builder `predicate.inL()`

`predicate.inL(f; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.Long">Long</abbr>&gt;</code> |
| `values` | i[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Integer">Integer</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.inL()` by jgea-experimenter:2.8.2

### Builder `predicate.inS()`

`predicate.inS(f; values)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | s[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.String">String</abbr>&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.inS()` by jgea-experimenter:2.8.2

### Builder `predicate.lt()`

`predicate.lt(f; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `t` | d |  | <code>double</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.lt()` by jgea-experimenter:2.8.2

### Builder `predicate.ltEq()`

`predicate.ltEq(f; t)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `t` | d |  | <code>double</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.ltEq()` by jgea-experimenter:2.8.2

### Builder `predicate.matches()`

`predicate.matches(f; regex)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `f` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `regex` | s |  | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.matches()` by jgea-experimenter:2.8.2

### Builder `predicate.not()`

`predicate.not(condition)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `condition` | npm |  | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |

Produces <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jnb.buildable.Predicates.not()` by jgea-experimenter:2.8.2

## Package `viz.drawer`

Aliases: `viz.d`, `viz.drawer`

### Builder `viz.drawer.composition()`

`viz.d.composition(drawer; f)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `drawer` | npm |  | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;E&gt;</code> |
| `f` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, E&gt;</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Drawers.composition()` by jgea-experimenter:2.8.2

### Builder `viz.drawer.multi()`

`viz.d.multi(drawer; arrangement)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `drawer` | npm |  | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;E&gt;</code> |
| `arrangement` | e | `HORIZONTAL` | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer$Arrangement">Drawer$Arrangement</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;E&gt;&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Drawers.multi()` by jgea-experimenter:2.8.2

### Builder `viz.drawer.stacked()`

`viz.d.stacked(drawers; arrangement)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `drawers` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;? extends <abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;? super E&gt;&gt;</code> |
| `arrangement` | e | `VERTICAL` | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer$Arrangement">Drawer$Arrangement</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;E&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Drawers.stacked()` by jgea-experimenter:2.8.2

## Package `viz.function`

Aliases: `viz.f`, `viz.function`

### Builder `viz.function.csvPlotter()`

`viz.f.csvPlotter(of; columnNameJoiner; doubleFormat; delimiter; missingDataString; mode)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, P&gt;</code> |
| `columnNameJoiner` | s | `.` | <code><abbr title="java.lang.String">String</abbr></code> |
| `doubleFormat` | s | `%.3e` | <code><abbr title="java.lang.String">String</abbr></code> |
| `delimiter` | s | `	` | <code><abbr title="java.lang.String">String</abbr></code> |
| `missingDataString` | s | `nan` | <code><abbr title="java.lang.String">String</abbr></code> |
| `mode` | e | `PAPER_FRIENDLY` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.csv.Configuration$Mode">Configuration$Mode</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.csvPlotter()` by jgea-experimenter:2.8.2

### Builder `viz.function.imagePlotter()`

`viz.f.imagePlotter(of; w; h; configuration; secondary; type)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, P&gt;</code> |
| `w` | i | `-1` | <code>int</code> |
| `h` | i | `-1` | <code>int</code> |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration">Configuration</abbr></code> |
| `secondary` | b | `false` | <code>boolean</code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.imagePlotter()` by jgea-experimenter:2.8.2

### Builder `viz.function.toImage()`

`viz.f.toImage(of; drawer; w; h; type)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, D&gt;</code> |
| `drawer` | npm |  | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;D&gt;</code> |
| `w` | i | `-1` | <code>int</code> |
| `h` | i | `-1` | <code>int</code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.toImage()` by jgea-experimenter:2.8.2

### Builder `viz.function.toImagesVideo()`

`viz.f.toImagesVideo(of; drawer; w; h; frameRate; encoder)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;D&gt;&gt;</code> |
| `drawer` | npm |  | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;D&gt;</code> |
| `w` | i | `-1` | <code>int</code> |
| `h` | i | `-1` | <code>int</code> |
| `frameRate` | d | `10.0` | <code>double</code> |
| `encoder` | e | `DEFAULT` | <code><abbr title="io.github.ericmedvet.jviz.core.util.VideoUtils$EncoderFacility">VideoUtils$EncoderFacility</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.drawer.Video">Video</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.toImagesVideo()` by jgea-experimenter:2.8.2

### Builder `viz.function.toMultiImage()`

`viz.f.toMultiImage(of; drawer; w; h; type; arrangement)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, <abbr title="java.util.List">List</abbr>&lt;D&gt;&gt;</code> |
| `drawer` | npm |  | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer">Drawer</abbr>&lt;D&gt;</code> |
| `w` | i | `-1` | <code>int</code> |
| `h` | i | `-1` | <code>int</code> |
| `type` | s | `png` | <code><abbr title="java.lang.String">String</abbr></code> |
| `arrangement` | e | `HORIZONTAL` | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.Drawer$Arrangement">Drawer$Arrangement</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="java.lang.Object">Object</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.toMultiImage()` by jgea-experimenter:2.8.2

### Builder `viz.function.toVideo()`

`viz.f.toVideo(of; video; w; h; encoder)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, D&gt;</code> |
| `video` | npm |  | <code><abbr title="io.github.ericmedvet.jviz.core.drawer.VideoBuilder">VideoBuilder</abbr>&lt;D&gt;</code> |
| `w` | i | `-1` | <code>int</code> |
| `h` | i | `-1` | <code>int</code> |
| `encoder` | e | `DEFAULT` | <code><abbr title="io.github.ericmedvet.jviz.core.util.VideoUtils$EncoderFacility">VideoUtils$EncoderFacility</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.drawer.Video">Video</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.toVideo()` by jgea-experimenter:2.8.2

### Builder `viz.function.videoPlotter()`

`viz.f.videoPlotter(of; w; h; encoder; frameRate; configuration; secondary)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `of` | npm | `f.identity()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;X, P&gt;</code> |
| `w` | i | `-1` | <code>int</code> |
| `h` | i | `-1` | <code>int</code> |
| `encoder` | e | `DEFAULT` | <code><abbr title="io.github.ericmedvet.jviz.core.util.VideoUtils$EncoderFacility">VideoUtils$EncoderFacility</abbr></code> |
| `frameRate` | d | `10.0` | <code>double</code> |
| `configuration` | npm | `viz.plot.configuration.image()` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration">Configuration</abbr></code> |
| `secondary` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jnb.datastructure.NamedFunction">NamedFunction</abbr>&lt;X, <abbr title="io.github.ericmedvet.jviz.core.drawer.Video">Video</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.Functions.videoPlotter()` by jgea-experimenter:2.8.2

## Package `viz.plot.configuration`

Aliases: `viz.plot.c`, `viz.plot.conf`, `viz.plot.configuration`

### Builder `viz.plot.configuration.image()`

`viz.plot.c.image(axesShow; titleShow; axesIndependences; fontSizeRate; fontName; strokeSizeRate; markerSizeRate; alpha; linesMarkers; boxplotExtremeType; boxplotMidType; boxplotMarkers; marker; debug)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `axesShow` | e | `BORDER` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration$PlotMatrix$Show">Configuration$PlotMatrix$Show</abbr></code> |
| `titleShow` | e | `BORDER` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration$PlotMatrix$Show">Configuration$PlotMatrix$Show</abbr></code> |
| `axesIndependences` | e[] | `[ROWS, COLS]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration$PlotMatrix$Independence">Configuration$PlotMatrix$Independence</abbr>&gt;</code> |
| `fontSizeRate` | d | `1.0` | <code>double</code> |
| `fontName` | s | `SansSerif` | <code><abbr title="java.lang.String">String</abbr></code> |
| `strokeSizeRate` | d | `1.0` | <code>double</code> |
| `markerSizeRate` | d | `1.0` | <code>double</code> |
| `alpha` | d | `0.3` | <code>double</code> |
| `linesMarkers` | b | `false` | <code>boolean</code> |
| `boxplotExtremeType` | e | `IQR_1_5` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration$BoxPlot$ExtremeType">Configuration$BoxPlot$ExtremeType</abbr></code> |
| `boxplotMidType` | e | `MEDIAN` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration$BoxPlot$MidType">Configuration$BoxPlot$MidType</abbr></code> |
| `boxplotMarkers` | b | `false` | <code>boolean</code> |
| `marker` | e | `CIRCLE` | <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.XYPlotDrawer$Marker">XYPlotDrawer$Marker</abbr></code> |
| `debug` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.image.Configuration">Configuration</abbr></code>; built from `io.github.ericmedvet.jviz.buildable.builders.PlotConfigurations.image()` by jgea-experimenter:2.8.2

## Package `viz.plot.multi`

Aliases: `viz.plot.m`, `viz.plot.multi`

### Builder `viz.plot.multi.scatter()`

`viz.plot.m.scatter(xSubplot; ySubplot; group; x; y; predicateValue; condition; xRange; yRange; limitOneYForK)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `group` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.gtEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.ScatterMKPAF">ScatterMKPAF</abbr>&lt;E, K, <abbr title="java.lang.String">String</abbr>, X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.scatter()` by jgea-experimenter:2.8.2

### Builder `viz.plot.multi.xy()`

`viz.plot.m.xy(xSubplot; ySubplot; line; x; y; valueAggregator; minAggregator; maxAggregator; xRange; yRange; limitOneYForK; useKForX)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `line` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;?, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `valueAggregator` | npm | `f.median()` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `minAggregator` | npm | `f.percentile(p = 25)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `maxAggregator` | npm | `f.percentile(p = 75)` | <code><abbr title="java.util.function.Function">Function</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Number">Number</abbr>&gt;, <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |
| `useKForX` | b | `false` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.AggregatedXYDataSeriesMKPAF">AggregatedXYDataSeriesMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.xy()` by jgea-experimenter:2.8.2

### Builder `viz.plot.multi.yBoxplot()`

`viz.plot.m.yBoxplot(xSubplot; ySubplot; box; y; predicateValue; condition; yRange; limitOneYForK)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `xSubplot` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `ySubplot` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `box` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super R, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.gtEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `limitOneYForK` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.DistributionMKPAF">DistributionMKPAF</abbr>&lt;E, R, <abbr title="java.lang.String">String</abbr>, X&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.MultiPlots.yBoxplot()` by jgea-experimenter:2.8.2

## Package `viz.plot.single`

Aliases: `viz.plot.s`, `viz.plot.single`

### Builder `viz.plot.single.field()`

`viz.plot.s.field(title; fields; pointPairs; predicateValue; condition; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `fields` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, F&gt;&gt;</code> |
| `pointPairs` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super F, ? extends <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;, <abbr title="java.util.List">List</abbr>&lt;<abbr title="java.lang.Double">Double</abbr>&gt;&gt;&gt;&gt;</code> |
| `predicateValue` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.ltEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.VectorialFieldSEPAF">VectorialFieldSEPAF</abbr>&lt;E, K, X, F&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.field()` by jgea-experimenter:2.8.2

### Builder `viz.plot.single.grid()`

`viz.plot.s.grid(title; values; grids; predicateValue; condition; valueRange; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super G, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `grids` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="io.github.ericmedvet.jnb.datastructure.Grid">Grid</abbr>&lt;G&gt;&gt;&gt;</code> |
| `predicateValue` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.ltEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.UnivariateGridSEPAF">UnivariateGridSEPAF</abbr>&lt;E, K, X, G&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.grid()` by jgea-experimenter:2.8.2

### Builder `viz.plot.single.heat()`

`viz.plot.s.heat(title; values; maps; predicateValue; condition; valueRange; unique)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `values` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super V, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `maps` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Map">Map</abbr>&lt;<abbr title="io.github.ericmedvet.jviz.core.geometry.Polygon">Polygon</abbr>, V&gt;&gt;&gt;</code> |
| `predicateValue` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `condition` | npm | `predicate.ltEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `valueRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `unique` | b | `true` | <code>boolean</code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.HeatPolyMapSEPAF">HeatPolyMapSEPAF</abbr>&lt;E, K, X, V&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.heat()` by jgea-experimenter:2.8.2

### Builder `viz.plot.single.xyes()`

`viz.plot.s.xyes(title; points; x; y; predicateValue; unique; condition; xRange; yRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `points` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, <abbr title="java.util.Collection">Collection</abbr>&lt;P&gt;&gt;&gt;</code> |
| `x` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super P, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `y` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super P, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `predicateValue` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;E, X&gt;</code> |
| `unique` | b | `true` | <code>boolean</code> |
| `condition` | npm | `predicate.ltEq(t = 1)` | <code><abbr title="java.util.function.Predicate">Predicate</abbr>&lt;X&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSEPAF">XYDataSeriesSEPAF</abbr>&lt;E, K, X, P&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyes()` by jgea-experimenter:2.8.2

### Builder `viz.plot.single.xyrs()`

`viz.plot.s.xyrs(title; x; ys; xRange; yRange)`

| Param | Type | Default | Java type |
| --- | --- | --- | --- |
| `title` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super K, <abbr title="java.lang.String">String</abbr>&gt;</code> |
| `x` | npm |  | <code><abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;</code> |
| `ys` | npm[] | `[]` | <code><abbr title="java.util.List">List</abbr>&lt;<abbr title="java.util.function.Function">Function</abbr>&lt;? super E, ? extends <abbr title="java.lang.Number">Number</abbr>&gt;&gt;</code> |
| `xRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |
| `yRange` | npm | `m.range(max = Infinity; min = -Infinity)` | <code><abbr title="io.github.ericmedvet.jnb.datastructure.DoubleRange">DoubleRange</abbr></code> |

Produces <code><abbr title="io.github.ericmedvet.jviz.core.plot.accumulator.XYDataSeriesSRPAF">XYDataSeriesSRPAF</abbr>&lt;E, K&gt;</code>; built from `io.github.ericmedvet.jviz.buildable.builders.SinglePlots.xyrs()` by jgea-experimenter:2.8.2

