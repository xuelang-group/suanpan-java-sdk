package com.xuelang.suanpan.client;

/**
 * 提供静态build方法，用于构建spClient实例；
 * 对于用户组件开发者，需要通过该类提供的build方法创建spClient实例，用于和平台交互及流计算
 */
public class SpClientBuilder {

    private static volatile SpClient spClient;

    /**
     * 构建spClient实例，单例模式
     * @return spClient单例对象
     */
    public static SpClient build(){
        if (null == spClient) {
            synchronized (SpClientBuilder.class) {
                if (null == spClient) {
                    spClient = new SpClient();
                }
            }
        }

        return spClient;
    }


    /**
     * 本地调试模式下，构建算盘平台的client
     * @param spConnectionParam 算盘后面板，需要调试的组件的连接信息
     * @return spClient单例对象
     */
    public static SpClient buildDebugMode(SpConnectionParam spConnectionParam){
        if (null == spClient) {
            synchronized (SpClientBuilder.class) {
                if (null == spClient) {
                    spClient = new SpClient(spConnectionParam);
                }
            }
        }

        return spClient;
    }
}
