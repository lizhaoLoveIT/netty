package cn.lizhaoloveit.tomcat;

import cn.lizhaoloveit.servnet.NettyRequest;
import cn.lizhaoloveit.servnet.NettyResponse;
import cn.lizhaoloveit.servnet.Servnet;

/**
 * DESCRIPTION:
 * Author: ammar
 * Date:   2020-10-11
 * Time:   12:33
 */
public class DefaultServnet extends Servnet {
    @Override
    public void doGet(NettyRequest request, NettyResponse response) throws Exception {
        // http://localhost:8888/some/xxx/ooo?name=zs
        // uri:/some/xxx/ooo?name=zs
        // path:/some/xxx/ooo
        String[] split = request.getUri().split("/");
        String servnetName = split[1];
        response.write("404 - this servnet not found" + servnetName);
    }

    @Override
    public void doPost(NettyRequest request, NettyResponse response) throws Exception {
        doGet(request, response);
    }
}
