package io.github.ericmedvet.jgea.experimenter.net;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author "Eric Medvet" on 2023/03/26 for jgea
 */
public class NetUtils {
  private NetUtils() {
  }

  public static double getCPULoad() {
    return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemLoadAverage();
  }

  public static MachineInfo getMachineInfo() {
    return new MachineInfo(getMachineName(), getNumberOfProcessors(), getCPULoad());
  }

  public static String getMachineName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown";
    }
  }

  public static int getNumberOfProcessors() {
    return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getAvailableProcessors();
  }

  public static ProcessInfo getProcessInfo() {
    return new ProcessInfo(
        getProcessName(),
        getUserName(),
        getProcessUsedMemory(),
        getProcessMaxMemory()
    );
  }

  public static long getProcessMaxMemory() {
    return Runtime.getRuntime().maxMemory();
  }

  public static String getProcessName() {
    return ManagementFactory.getRuntimeMXBean().getName();
  }

  public static long getProcessUsedMemory() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  public static String getUserName() {
    return System.getProperty("user.name");
  }

}
