package com.example;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

public class Subscriber {
    public static void main(String[] args) throws InterruptedException, SQLException {
        TokenCredential credential = new ClientSecretCredentialBuilder()
            .clientId(System.getenv("AZURE_CLIENT_ID"))
            .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
            .tenantId(System.getenv("AZURE_TENANT_ID"))
            .build();

        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(System.getenv("SERVICE_BUS_FULLY_QUALIFIED_NAMESPACE"))
            .credential(credential)
            .processor()
            .queueName("lower-case")
            .processMessage(new Consumer<ServiceBusReceivedMessageContext>() {
                @Override
                public void accept(ServiceBusReceivedMessageContext context) {
                    try {
                        new MessageProcessor(new DatabaseService(DatabaseConnectionManager.getConnection()))
                            .processMessage(context);
                    } catch (SQLException e) {
                        System.out.println(e.toString());
                    }
                }
            })
            .processError(new Consumer<ServiceBusErrorContext>() {
                @Override
                public void accept(ServiceBusErrorContext context) {
                    try {
                        new MessageProcessor(new DatabaseService(DatabaseConnectionManager.getConnection()))
                            .processError(context);
                    } catch (SQLException e) {
                        System.out.println(e.toString());
                    }
                }
            })
            // 【ご参考】Java 8 以降の場合は、メソッド参照を使用してメッセージを処理することができます。
            // .processMessage(this::processMessage)
            // .processError(this::processError)
            .buildProcessorClient();

        processorClient.start();

        TimeUnit.SECONDS.sleep(300);

        processorClient.close();
    }
}