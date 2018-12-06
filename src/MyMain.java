import java.lang.instrument.Instrumentation;

public class MyMain {
  public static void premain(String agentOps, Instrumentation instrumentation) {
    System.out.println("pre1 执行.....");
  }

  public static void main(String[] args) {

  }
}
