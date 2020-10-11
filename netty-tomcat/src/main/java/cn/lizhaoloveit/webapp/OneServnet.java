package cn.lizhaoloveit.webapp;

import cn.lizhaoloveit.servnet.NettyRequest;
import cn.lizhaoloveit.servnet.NettyResponse;
import cn.lizhaoloveit.servnet.Servnet;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-11
 * Time:   13:57
 */
public class OneServnet extends Servnet {
    @Override
    public void doGet(NettyRequest request, NettyResponse response) throws Exception {
        String uri = request.getUri();
        String path = request.getPath();
        String method = request.getMethod();
        String name = request.getParameter("name");

        String content = "uri = " + uri + "\n" +
                "path = " + path + "\n" +
                "method = " + method + "\n" +
                "name = " + name + "\n";
        response.write(content);
    }

    @Override
    public void doPost(NettyRequest request, NettyResponse response) throws Exception {
        doGet(request, response);
    }
}
