package com.github.sputnik906.lang.utils;

import java.io.File;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {
  public static void clearDir(File dir){
    for (File file: dir.listFiles()) {
      if (file.isDirectory())
        clearDir(file);
      file.delete();
    }
  }

  public static void createOrClearDir(File dir){
    dir.mkdirs();
    clearDir(dir);
  }
}
