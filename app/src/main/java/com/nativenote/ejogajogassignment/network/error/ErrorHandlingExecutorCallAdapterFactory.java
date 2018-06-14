package com.nativenote.ejogajogassignment.network.error;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


/**
 * Created by IMTIAZ on 12/26/15.
 */
public class ErrorHandlingExecutorCallAdapterFactory extends CallAdapter.Factory {

    private final Executor callbackExecutor;

    public ErrorHandlingExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if ($Gson$Types.getRawType(returnType) != Call.class) {
            return null;
        }
        final Type responseType = getCallResponseType(returnType);

        return new CallAdapter<Object, Object>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Object adapt(Call<Object> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call);
            }
        };
    }

    static Type getCallResponseType(Type returnType) {
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
        final Type responseType = getSingleParameterUpperBound((ParameterizedType) returnType);

        // Ensure the Call response type is not Response, we automatically deliver the Response object.
        if ($Gson$Types.getRawType(responseType) == Response.class) {
            throw new IllegalArgumentException(
                    "Call<T> cannot use Response as its generic parameter. "
                            + "Specify the response body type only (e.g., Call<TweetResponse>).");
        }
        return responseType;
    }


    public static Type getSingleParameterUpperBound(ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException(
                    "Expected one type argument but got: " + Arrays.toString(types));
        }
        Type paramType = types[0];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    static final class ExecutorCallbackCall<T> implements Call<T> {
        private final Executor callbackExecutor;
        private final Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void enqueue(Callback<T> callback) {
            delegate.enqueue(new ExecutorCallback<>(callbackExecutor, callback));
        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return null;
        }
    }

    static final class ExecutorCallback<T> implements Callback<T> {
        private final Executor callbackExecutor;
        private final Callback<T> delegate;

        ExecutorCallback(Executor callbackExecutor, Callback<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override
        public void onResponse(final Call<T> call, final Response<T> response) {
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    delegate.onResponse(call, response);
                }
            });
        }

        @Override
        public void onFailure(final Call<T> call, Throwable t) {
            RetrofitException exception;
            if (t instanceof IOException) {
                exception = RetrofitException.networkError((IOException) t);
            } else {
                exception = RetrofitException.unexpectedError(t);
            }
            final RetrofitException finalException = exception;
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    delegate.onFailure(call, finalException);
                }
            });
        }
    }

    public static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable r) {
            handler.post(r);
        }
    }
}