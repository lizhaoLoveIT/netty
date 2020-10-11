package cn.lizhaoloveit.servnet;

/**
 * DESCRIPTION:Servnet 响应规范
 * Author: ammar
 * Date:   2020-10-11
 * Time:   12:10
 */
public interface NettyResponse {

    // 将响应写入到 Channel
    void write(String content) throws Exception;

}
