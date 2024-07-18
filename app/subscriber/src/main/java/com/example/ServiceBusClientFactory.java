package com.example;

import java.sql.SQLException;
import java.util.function.Consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

import io.github.resilience4j.retry.Retry;

public class ServiceBusClientFactory {

    public ServiceBusProcessorClient buildQueueClient(TokenCredential credential, MessageProcessor messageProcessor) {
        return new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(System.getenv("SERVICE_BUS_FULLY_QUALIFIED_NAMESPACE"))
            .credential(credential)
            .processor()
            .queueName("lower-case")
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(new Consumer<ServiceBusReceivedMessageContext>() {
                @Override
                public void accept(ServiceBusReceivedMessageContext context) {
                    // リトライ設定の取得
                    Retry retry = RetryConfigurator.getRetryConfig("my-retry");
                    // リトライロジックを適用してメッセージを処理
                    Retry.decorateRunnable(retry, () -> {
                        try {
                            // messageProcessor.processMessage(context);
                            throw new SQLException("Simulated database error");
                        } catch (SQLException e) {
                            int retryCount = messageProcessor.logErrorAndIncrementRetryCount(context.getMessage(), e);
                            if (retryCount >= 3) {
                                // リトライ回数が 3 回を超えた場合は、メッセージを DLQ に送信
                                messageProcessor.sendToDLQ(context, credential);
                            } else {
                                throw new RuntimeException(e);
                            }
                        }    
                    }).run();
                }
            })
            .processError(new Consumer<ServiceBusErrorContext>() {
                @Override
                public void accept(ServiceBusErrorContext context) {
                    try {
                        messageProcessor.processError(context);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }    
                }
            })
            // 【ご参考】Java 8 以降の場合は、メソッド参照を使用してメッセージを処理することができます。
            // .processMessage(this::processMessage)
            // .processError(this::processError)
            .buildProcessorClient();
    }

    public ServiceBusProcessorClient buildDlqClient(TokenCredential credential) {
        return new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(System.getenv("SERVICE_BUS_FULLY_QUALIFIED_NAMESPACE"))
            .credential(credential)
            .processor()
            .queueName("lower-case/$deadletterqueue")
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .processMessage(new Consumer<ServiceBusReceivedMessageContext>() {
                @Override
                public void accept(ServiceBusReceivedMessageContext context) {
                    System.out.printf("Dead-lettered message received: Sequence #: %s. Contents: %s%n",
                        context.getMessage().getSequenceNumber(), context.getMessage().getBody());
                }
            })
            .processError(new Consumer<ServiceBusErrorContext>() {
                @Override
                public void accept(ServiceBusErrorContext context) {
                    try {
                        new MessageProcessor(new DatabaseService(DatabaseConnectionManager.getConnection()))
                            .processError(context);
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }    
                }
            })
            // 【ご参考】Java 8 以降の場合は、メソッド参照を使用してメッセージを処理することができます。
            // .processMessage(this::processMessage)
            // .processError(this::processError)
            .buildProcessorClient();
    }
}
