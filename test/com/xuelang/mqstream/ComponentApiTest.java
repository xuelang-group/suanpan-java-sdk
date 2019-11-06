package com.xuelang.mqstream;

import com.xuelang.mqstream.api.ComponentApi;
import com.xuelang.mqstream.api.response.Component;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * @Auther: zigui.zdf
 * @Date: 2019/11/4 17:17
 * @Description:
 */
public class ComponentApiTest {

    @BeforeClass
    public static void setUp() {
//        Map<String, String> envs = System.getenv();
//        envs.forEach((k, v) -> {
//            if (k.startsWith("SP_")) {
//                System.out.println(k + ":" + v);
//            }
//        });
        //在环境变量配置如下参数
//        SP_NODE_ID:e09e9110fadf11e99e032db7f486facf
//        SP_ACCESS_SECRET:2Ojkk3Xvh4IONtciPHZkAPegPpZj3O
//        SP_APP_ID:846
//        SP_ACCESS_KEY:ufyahC9aDTNEmi7W
//        SP_HOST:sp.xuelangyun.com
//        SP_APP_TYPE:stream
//        SP_API_HOST:spapi.xuelangyun.com
//        SP_USER_ID:100033
        //配置完如果不生效请重启ide
    }

    @Test
    public void listComponents() {
        ComponentApi componentApi = new ComponentApi();
        List<Component> components = componentApi.listComponents(9999);
        for (Component component : components) {
            System.out.println(component);
        }
    }

    @Test
    public void shareComponent() {
        ComponentApi componentApi = new ComponentApi();
        String s = componentApi.shareComponent(4635, "100077", "spos-backend");
        System.out.println(s);
    }
}
