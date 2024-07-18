package com.example;

import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;

public class RetryConfigurator {

    /**
     * 指数バックオフアルゴリズムを使用してリトライ間の待機時間を計算する{@link IntervalFunction}を返します。
     * この関数は、リトライの間に指数関数的に増加する待機時間を生成します。初回のリトライでは、待機時間は初期値として設定されます。
     * その後、待機時間は指定された乗数で乗算されます。
     *
     * @return 指数バックオフアルゴリズムに基づく待機時間を計算する{@link IntervalFunction}。
     */
    private static IntervalFunction getIntervalFunction() {
        return IntervalFunction.ofExponentialBackoff(1000, 2);
    }

    /**
     * 指定されたリトライIDに基づいてリトライ設定を生成し、それを使用する{@link Retry}オブジェクトを返します。
     * このメソッドでは、リトライの最大試行回数、リトライ間の待機時間、およびリトライ間の待機時間の増加関数を設定します。
     *
     * @param retryId リトライ設定を識別するためのID。このIDは、生成される{@link Retry}オブジェクトに関連付けられます。
     * @return 指定された設定を使用する{@link Retry}オブジェクト。
     */
    public static Retry getRetryConfig(String retryId) {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3) // 最大リトライ回数
            .waitDuration(Duration.ofMillis(1000)) // リトライ間の待機時間（ミリ秒）
            .intervalFunction(getIntervalFunction()) // リトライ間の待機時間の増加関数
            .build();
        return Retry.of(retryId, config);
    }
}