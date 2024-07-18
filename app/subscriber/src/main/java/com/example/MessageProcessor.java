package com.example;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;

public class MessageProcessor {
    private final DatabaseService databaseService;

    public MessageProcessor(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public void processMessage(ServiceBusReceivedMessageContext context) throws SQLException {
        ServiceBusReceivedMessage message = context.getMessage();
        System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n", message.getMessageId(),
                message.getSequenceNumber(), message.getBody());

        databaseService.insertData(UUID.randomUUID().toString(), message.getBody().toString());
    }

    public void processError(ServiceBusErrorContext context) {
        System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
            || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
            || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
                reason, exception.getMessage());
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            System.out.printf("Message lock lost for message: %s%n", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Unable to sleep for period of time");
            }
        } else {
            System.out.printf("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
                reason, context.getException());
        }
    }

    /**
     * メッセージをDLQに送信するメソッドです。
     * @param context メッセージコンテキスト
     * @param credential トークンクレデンシャル
     */
    public void sendToDLQ(ServiceBusReceivedMessageContext context, TokenCredential credential) {
        System.out.println("Sending message to DLQ");
        ServiceBusReceiverClient serviceBusReceiverClient = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(System.getenv("SERVICE_BUS_FULLY_QUALIFIED_NAMESPACE"))
            .credential(credential)
            .receiver()
            .queueName("lower-case")
            .buildClient();
        serviceBusReceiverClient.deadLetter(context.getMessage());
    }

    /**
     * サービスバスメッセージの処理中に発生したエラーをログに記録し、リトライカウントをインクリメントします。
     * メッセージのカスタムプロパティ「retry-count」にリトライ回数をセットします（初回の場合は1がセットされます）。
     *
     * @param message 処理中のサービスバスメッセージ
     * @param e 発生した例外
     * @return 更新されたリトライカウントの値
     */
    public int logErrorAndIncrementRetryCount(ServiceBusReceivedMessage message, Exception e) {
        message.getApplicationProperties().put("retry-count", (int) message.getApplicationProperties().getOrDefault("retry-count", 0) + 1);
        System.out.println(message.getApplicationProperties().get("retry-count"));
        System.out.println(e.toString());
        System.out.println("Failed to process message: " + message.getBody());
        return (int) message.getApplicationProperties().get("retry-count");
    }

}