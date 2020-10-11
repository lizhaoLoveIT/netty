package cn.lizhaoloveit.servnet;

import java.util.List;
import java.util.Map;

/**
 * DESCRIPTION: Servnet 请求规范
 * Author: ammar
 * Date:   2020-10-11
 * Time:   12:07
 */
public interface NettyRequest {

    // 获取 URI，包含请求参数，?后的内容
    String getUri();
    // 不包含请求参数，请求路径
    String getPath();
    // 获取请求方法(GET、POST)
    String getMethod();
    // 获取所有请求参数
    Map<String, List<String>> getParameters();
    // 获取指定名称的请求参数
    List<String> getParameters(String name);
    // 获取指定名称的请求参数的第一个值
    String getParameter(String name);
}
