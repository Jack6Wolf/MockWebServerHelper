package com.orhanobut.sample;

import com.orhanobut.mockwebserverplus.MockWebServerPlus;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import fixtures.Fixtures;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.assertNotNull;

public class FooTest {

  @Rule public MockWebServerPlus server = new MockWebServerPlus();

  @Test public void testFoo() throws IOException {
    server.enqueue(Fixtures.SIMPLE);

    Request request = new Request.Builder()
        .url(server.url("/jack"))
        .get()
        .build();

    OkHttpClient client = new OkHttpClient();
    Response response = client.newCall(request).execute();
    System.out.println(response.body().string());
    assertNotNull(response);
  }
}
