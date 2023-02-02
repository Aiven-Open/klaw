package io.aiven.klaw.uglify;

import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UglifyFiles {

  String sourceDirJsFiles = "./target/classes/static/js/";
  String cssDir = "./target/classes/static/assets/css/";

  // js files
  public UglifyFiles() {
    String osName = System.getProperty("os.name");
    System.out.println("OS : " + osName);
    Runtime rt = Runtime.getRuntime();

    uglifyJsFiles(osName, rt);
    uglifyCssFiles(osName, rt);
  }

  private void uglifyCssFiles(String osName, Runtime rt) {
    String styleCssFile = cssDir + "style.css";
    String styleBlueDarkCssFile = cssDir + "colors/blue-dark.css";
    File style = new File(styleCssFile);
    File bluedark = new File(styleBlueDarkCssFile);

    if (!style.exists()) {
      style = new File("./core/" + styleCssFile);
    }

    if (!bluedark.exists()) {
      bluedark = new File("./core/" + styleBlueDarkCssFile);
    }

    String commandToExec =
        "uglifycss " + style.getAbsolutePath() + " --output " + style.getAbsolutePath();
    executeCommand(rt, commandToExec, osName);

    commandToExec =
        "uglifycss " + bluedark.getAbsolutePath() + " --output " + bluedark.getAbsolutePath();
    executeCommand(rt, commandToExec, osName);
  }

  private void uglifyJsFiles(String osName, Runtime rt) {

    File f = new File(sourceDirJsFiles);
    File[] filesInDir = f.listFiles();
    if (filesInDir == null) {
      File file = new File("./core/" + sourceDirJsFiles);
      filesInDir = file.listFiles();
    }
    if (filesInDir != null) {

      for (File file : filesInDir) {
        String commandToExec =
            "uglifyjs " + file.getAbsolutePath() + " -o " + file.getAbsolutePath();
        executeCommand(rt, commandToExec, osName);
      }
    }
  }

  private void executeCommand(Runtime rt, String commandToExec, String osName) {
    String commandPrefix = "";

    if (osName.startsWith("Windows")) {
      commandPrefix = "cmd.exe /c ";
    } else {
      commandPrefix = "";
    }

    commandToExec = commandPrefix + commandToExec;

    System.out.println(commandToExec);
    try {
      rt.exec(commandToExec);
    } catch (IOException e) {
      log.error("Exception:", e);
    }
  }

  public static void main(String[] args) {
    new UglifyFiles();
  }
}
