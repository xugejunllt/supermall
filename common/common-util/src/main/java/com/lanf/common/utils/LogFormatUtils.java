package com.lanf.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class LogFormatUtils {

    /**
     * 格式化打印日志
     */
    public static void printFormatLog(Logger logger, String eventName, List<LogInfo> logInfoList, Object last) {

        StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.
                append(eventName).
                append("|");
        if (logInfoList != null) {
            for (LogInfo logInfo : logInfoList) {
                messageBuffer.append(logInfo.getKey()).
                        append("={}").
                        append("|");
            }
        }
        Object[] arguments = new Object[logInfoList.size() + 1];
        for (int i = 0; i < logInfoList.size(); i++) {
            arguments[i] = logInfoList.get(i).getValue();
        }
        messageBuffer.append("{}");
        arguments[logInfoList.size()] = last;

        logger.info(messageBuffer.toString(), arguments);
    }

    public static void printFormatLog(Logger logger, String eventName) {

        StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.
                append(eventName).
                append("|");
        logger.info(messageBuffer.toString());
    }

    public static void main(String[] args) {
      //  printFormatLog(log, "打印参数测试", Arrays.asList(new LogInfo("as", "好啊"), new LogInfo("as2", "好啊3")), "擦你哦");

        String asa = "asasa\n"+"asas111\n";
        String[] split = asa.split("\\n");

        StringBuffer stringBuffer = new StringBuffer();
        for (String s : split){
            System.out.println(s+"\n");
            stringBuffer.append(s);
        }


        System.out.println(stringBuffer.toString());
    }
    /**
     * 将异常日志转换为字符串
     *
     * @param e
     * @return
     */
    public static String getExceptionStr(Exception e) {
        Writer writer = null;
        PrintWriter printWriter = null;
        try {
            writer = new StringWriter();
            printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);

            String msg = writer.toString();
            String[] split = msg.split("\\n");

            StringBuffer stringBuffer = new StringBuffer();
            for (String s : split){
                Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                Matcher m = p.matcher(s);
                s = m.replaceAll("");
                stringBuffer.append(s);
                //用于日志切割
                stringBuffer.append("@");
            }
            return stringBuffer.toString();
        } finally {

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    log.error("堆栈转换异常");

                }
            }

        }


    }

  public static  String formatStartLog(String evenName){



        return  evenName+"@开始,";
  }


}
