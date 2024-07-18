package com.example;

import java.util.concurrent.TimeUnit;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;

public class ServiceBusUtil {
    
    /**
     * 指定されたServiceBusProcessorClientを開始し、指定された時間（秒）待機した後に停止します。
     * 
     * @param client ServiceBusProcessorClientのインスタンス
     * @param waitTimeSeconds クライアントが実行される時間（秒）
     * @throws InterruptedException スレッドが割り込まれた場合にスローされます
     */
    public void startAndStopClient(ServiceBusProcessorClient client, int waitTimeSeconds) throws InterruptedException {
        client.start();
        TimeUnit.SECONDS.sleep(waitTimeSeconds);
        client.close();
    }
}
