package io.github.itokagimaru.artifact.auction;

/**
 * オークション操作の結果を表すクラス
 * 
 * 成功/失敗とエラーメッセージ、成功時のデータを保持する。
 * 例外を使わずに結果を表現するためのResult型パターン。
 * 
 * @param <T> 成功時のデータ型
 */
public class Result<T> {
    
    private final boolean success;
    private final T data;
    private final String errorMessage;

    private Result(boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    /**
     * 成功結果を作成（データなし）
     */
    public static <T> Result<T> success() {
        return new Result<>(true, null, null);
    }

    /**
     * 成功結果を作成（データあり）
     * 
     * @param data 成功時のデータ
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null);
    }

    /**
     * 失敗結果を作成
     * 
     * @param errorMessage エラーメッセージ
     */
    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(false, null, errorMessage);
    }

    /**
     * 成功かどうか
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 失敗かどうか
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * 成功時のデータを取得
     * 
     * @return データ（失敗時はnull）
     */
    public T getData() {
        return data;
    }

    /**
     * エラーメッセージを取得
     * 
     * @return エラーメッセージ（成功時はnull）
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
