package cn.lizhaoloveit.tomcat;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-11
 * Time:   13:55
 */
public class BasePackageTest {
    public static void main(String[] args) {
        TomcatServer tomcatServer = new TomcatServer("cn.lizhaoloveit.webapp");
        tomcatServer.start();
    }
}
