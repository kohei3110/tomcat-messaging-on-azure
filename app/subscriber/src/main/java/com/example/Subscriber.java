package com.example;

import java.sql.SQLException;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;

public class Subscriber {

    /**
     * Azure Service Busからメッセージを非同期に処理するメインメソッド。
     * <p>
     * このメソッドは、Azure Service Busからメッセージを受信し、それらのメッセージに対して処理を行います。
     * 処理には、データベースへのメッセージの保存、エラー発生時のリトライ処理、およびデッドレターキューへのメッセージの転送が含まれます。
     * メッセージの処理は、指定されたリトライポリシーに従って行われます。
     * </p>
     * <p>
     * このメソッドでは、Service Busのキューとデッドレターキューの両方に対してプロセッサクライアントを構築し、
     * それぞれのクライアントを起動してメッセージの処理を開始します。処理が完了した後、クライアントは閉じられます。
     * </p>
     *
     * @param args コマンドライン引数（このアプリケーションでは使用されません）
     * @throws InterruptedException メインスレッドが中断された場合にスローされます。
     * @throws SQLException データベース操作中にエラーが発生した場合にスローされます。
     */
    public static void main(String[] args) throws InterruptedException, SQLException {
        MessageProcessor messageProcessor = new MessageProcessor(new DatabaseService(DatabaseConnectionManager.getConnection()));

        TokenCredential credential = new ClientSecretCredentialBuilder()
            .clientId(System.getenv("AZURE_CLIENT_ID"))
            .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
            .tenantId(System.getenv("AZURE_TENANT_ID"))
            .build();

        ServiceBusClientFactory factory = new ServiceBusClientFactory();
        ServiceBusUtil util = new ServiceBusUtil();
        ServiceBusProcessorClient processorClient = factory.buildQueueClient(credential, messageProcessor);
        ServiceBusProcessorClient dlqClient = factory.buildDlqClient(credential);

        util.startAndStopClient(processorClient, 20);
        util.startAndStopClient(dlqClient, 20);
    }
}