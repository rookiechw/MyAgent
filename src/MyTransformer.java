import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class MyTransformer implements ClassFileTransformer {
  final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
  final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer)
      throws IllegalClassFormatException {
    className = className.replace("/", ".");
    // 判断加载的class的包路径是不是需要监控的类
    if ("org.chw.gateway.EurekaServiceStart".equals(className)) {
      CtClass ctclass = null;
      try {
        String methodName = "main";
        // 使用全称,用于取得字节码类<使用javassist>
        ctclass = ClassPool.getDefault().get(className);
        String outputStr = "\nSystem.out.println(\"this method " + methodName
            + " cost:\" +(endTime - startTime) +\"ms.\");";

        // 得到这方法实例
        CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);
        // 新定义一个方法叫做比如sayHello$old
        String newMethodName = methodName + "$old";
        // 将原来的方法名字修改
        ctmethod.setName(newMethodName);

        // 创建新的方法，复制原来的方法，名字为原来的名字
        CtMethod newMethod = CtNewMethod.copy(ctmethod, methodName, ctclass, null);

        // 构建新的方法体
        StringBuilder bodyStr = new StringBuilder();
        bodyStr.append("{");
        bodyStr.append(prefix);
        // 调用原有代码，类似于method();($$)表示所有的参数
        bodyStr.append(newMethodName + "($$);\n");
        bodyStr.append(postfix);
        bodyStr.append(outputStr);
        bodyStr.append("}");
        // 替换新方法
        newMethod.setBody(bodyStr.toString());
        // 增加新方法
        ctclass.addMethod(newMethod);
        System.err.println(outputStr);
        return ctclass.toBytecode();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return null;
  }
}
