package cn.lizhaoloveit.servnet;

/**
 * DESCRIPTION:定义 Servnet 规范
 * Author: ammar
 * Date:   2020-10-11
 * Time:   12:11
 */
public abstract class Servnet {
    public abstract void doGet(NettyRequest request, NettyResponse response) throws Exception;
    public abstract void doPost(NettyRequest request, NettyResponse response) throws Exception;
}
