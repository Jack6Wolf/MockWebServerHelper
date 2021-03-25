### MockWebServerPlus
用来更简单高效设置MockResponse的库！

#### Issue
MockWebServer是一个用来模拟网络请求/响应的伟大工具。为了添加响应，您需要设置MockResponse主体以及所需的所有属性

```java
@Rule public MockWebServer server = new MockWebServer();

@Test public void uglyTest() {
  server.enqueue(new MockResponse()
    .setStatusCode(200)
    .setBody({
               "array": [
                 1,
                 2,
                 3
               ],
               "boolean": true,
               "null": null,
               "number": 123,
               "object": {
                 "a": "b",
                 "c": "d",
                 "e": "f"
               },
               "string": "Hello World"
             })
    .addHeader("HeaderKey:HeaderValue")
    .responseDelay(3, SECONDS)
  );
  
  // execute request
  // assert
  // verify
}
```

想象一下，它带有大量的json响应。它将模糊方法内容，几乎无法阅读。


#### Solution
为了使它更具可读性，您可以使用fixture。将您对fixture的响应移开，只引用它们。
MockWebServerPlus是一个包装器，它包含了带有fixture特性的MockWebServer。

##### Create a fixture yaml file under resources/fixtures

```
src
├── test
│   ├── java
│   ├── resources
│   │   ├── fixtures
│   │   │   ├── foo_success.yaml
│   │   │   ├── foo_failure.yaml
```

```yaml
statusCode : 200       // as the name says
delay: 0               // delays the response
headers:               // adds to the response
- 'Auth:auth'
- 'key:value'
body: 'common/body_file.json' // can be any path under /fixtures folder
// or inline
body: >                       // can be any text, json, plain etc. Use > letter for scalar text
    {
      "array": [
        1,
        2,
        3
      ],
      "boolean": true,
      "null": null,
      "number": 123,
      "object": {
        "a": "b",
        "c": "d",
        "e": "f"
      },
      "string": "Hello World"
    }
```

### 使用文件名来引用它。就是这样简单!

```java
@Rule public MockWebServerPlus server = new MockWebServerPlus();

@Test public void readableTest() {
  server.enqueue("foo_success");
  
  // execute request
  // assert
  // verify
}
```

#### 使用生成的fixture .java来引用您的fixture。阅读Generate Fixtures.java部分
```java
server.enqueue(Fixtures.FOO_SUCCESS);
```

### 排队多个响应
```java
server.enqueue(Fixtures.FOO_SUCCESS, Fixtures.USER_REGISTER_SUCCESS);
```

### Custom Dispatcher
您可能希望在模拟web服务器中使用自定义调度程序。

Fixtures can be used directly inside a Dispatcher:

```
new Dispatcher() {
  @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
    return Fixture.parseFrom("simple).toMockResponse();
  }
}
```

### Generate Fixtures.java
您总是可以使用纯文本来引用fixture。

```java
server.enqueue("foo_success");
```

but you can also generate Fixtures.java file to have all of them with a task. This will make your code more type-safe.
Put the following task into your build.gradle file and execute it when you add/modify your fixture resources.

```groovy
task generateFixtures(dependsOn: copyTestResources) << {
  def directory = projectDir.path + '/src/test/java'
  new File(directory + '/fixtures').mkdir()
  def path = directory + "/fixtures/Fixtures.java"

  def builder = '' << ''
  builder.append("package fixtures;\n\n")
  builder.append("public class Fixtures {\n\n")
  builder.append("  private Fixtures() {\n")
  builder.append("    //no instance\n")
  builder.append("  }\n\n")

  def resources = android.sourceSets.test.resources.srcDirs.getAt(0)
  if (resources != null) {
    resources.eachDirMatch("fixtures") { dir ->
      def fixturesFile = dir
      fixturesFile.eachFile(FileType.FILES) { file ->
        if (file.name.endsWith(".yaml")) {
          String fileName = file.name.split('\\.')[0]
          builder.append("  public static final String ")
              .append(fileName.toUpperCase())
              .append(" = ")
              .append('\"')
              .append(fileName)
              .append('\";\n')
        }
      }
    }
  }
  builder.append("}\n")

  new File(path).write(builder.toString())
}

```

Above solution will generate Fixtures.java when you execute it manually. But you might forget to execute it, you can
make it dependent for any task to make it automated. Whenever preBuild is executed, it will also execute this task

```groovy
preBuild.dependsOn generateFixtures
```

### Install
```groovy
testCompile 'com.orhanobut:mockwebserverplus:2.0.0'

// This is optional, but in order to be up-to-date with OkHttp changes, you can use the latest version
testCompile 'com.squareup.okhttp3:mockwebserver:3.7.0'  
```

#### Other proxy methods
```java
MockWebServerPlus.server()         // returns MockWebServer instance
MockWebServerPlus.takeRequest()    // returns RecordedRequest
MockWebServerPlus.url(String path) // returns url to execute
MockWebServerPlus.setDispatcher(Dispatcher dispatcher)  // any custom dispatcher
MockWebServerPlus.enqueue(SocketPolicy socketPolicy)    // Useful for network errors, such as DISCONNECT etc
```

### Get the fixture object
```java
Fixture fixture = Fixture.parseFrom(Fixtures.SIMPLE);
```

### For non-android modules
For non-android modules, you may need to add the following tasks to copy your resources into classes dir
```groovy
task copyTestResources(type: Copy) {
  from sourceSets.test.resources
  into "${projectDir}/src/test/java"
}
```
**注意，Android工程不应该使用它！**

#### Credits
[MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) from Square

### License
<pre>
Copyright 2017 Orhan Obut

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
