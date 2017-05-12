# 利用APT实现Android编译时注解

[TOC]

## APT

> Annotaion Processing Tool(注解处理工具)

### APT概述

Java为了提高其扩展性，提供了反射机制可以处理注解。其实我们还可以在编译时处理注解，这就要用到官方为我们提供的注解处理工具APT(Annotaion Processing Tool).

为了提高易读性，我们先来看以下一段代码

```java
public class MainActivity extends Activity{
  public void onCreate(Bundle savedInstance){
    super.onCreate(savedInstance);
    setContentView(R.layout.activity_main);
    TextView textView = (TextView) findViewById(R.id.tv);
    textView.setText("Hello World");
  }
}
```

这段代码非常直观，通俗易懂。但是每次都写这些无聊的代码却是也降低了我们做开发的乐趣，同时也降低了开发的效率。再来看以下一段代码（ButterKnife Dagger2..）

```java
public class MainActivity extends Activity{
  @BindView(R.id.tv)
    TextView textView
  public void onCreate(Bundle savedInstance){
    super.onCreate(savedInstance);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    }
}
```

> 注：ButterKnife已经在8.0.1后更改为编译时处理注解（APT）(具体版本不清楚，反正7.0.1版本还没有这么做)

这两段代码比对后，已经有了比较明显的趋向性

## 实现目标

1. BindView
2. Click

```java
@BindView(R.id.tv)
TextView textView;

@Click(R.id.btn)
public void show(View view){
  // do sth
}
```

       	3. 生成的代理类（实际就是我们没有写的那些findViewById）

```java
public class MainActivity$$Proxy implements IProxy<MainActivity>{
  public void inject(MainActivity activity,View root){
    target.textView = (TextView)root.findViewById(R.id.tv);
    ...
    root.findViewById(R.id.btn)
        .setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View view){
            show(view);
          }
        })
  }
}
```

> 注：上边这段代码需要通过代码生成

## 实现步骤

### 项目结构

1. annotation --------声明的注解Module（Java项目）
2. compiler     -------- 解析注解的Module（Java项目），不需要打包到apk中
3. api                -------- 关联项目与compiler 的Module（一般为Android项目，原因主要是涉及 Activity/View/Fragment 组件）
4. app               ====== 我们自己的项目

> Q: 为什么要使用三个Module来实现
>
> A: compiler是不需要打包进去的，它的目的仅仅是在build的时候帮助我们创建代理类，也可以理解为帮我们写我们不想写的代码



> Q: 这几个Module的依赖关系是怎样的
>
> A： api 依赖 Annotation
>
> ​        compiler 依赖Annotation
>
> ​       app 依赖 Annotation/api/ apt project(':compiler')

### 实现Annotation对应的 Module

1. 创建Java项目
2. 创建两个注解

```java
@Target({
        ElementType.FIELD
})
@Retention(
        RetentionPolicy.CLASS
)
public @interface BindView {
    int value() default View.NO_ID;
}
```

```java
@Target({
        ElementType.METHOD
})
@Retention(
        RetentionPolicy.CLASS
)
public @interface Click {
    int value() default View.NO_ID;
}
```

> 注： View.NO_ID = -1; 默认的控件id

### 实现Compiler对应的Module

#### 依赖的库

* compile 'com.squareup:javapoet:1.7.0'
* compile 'com.google.auto.service:auto-service:1.0-rc2'

> 注：javapoet 是square 公司开发的能够快速生成java类的库
>
> auto-service 是为了帮助我们实现 META-INF/services/javax.annotation.processing.Processor 文件

1. compiler 需要依赖Annotation 的Module
2. 创建一个新的类继承AbstractProcessor
3. 给当前Processor添加注解@AutoService(Processor.class)
   1. 此时可以省去创建META-INF/services.javax.annotation.processing.Processor配置文件

```java
@AutoService(Processor.class)
pulic class DggProcessor extends AbstractProcessor{
  
}
```

#### AbstractProcessor方法介绍

1. init方法（初始化）

```java
@Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }
```

* elementUtils 根据元素获取当前元素的其他信息（包名 注释）
* filer 生成代码的目录 这里默认为 app（当前项目）build/generated/source/apt/debug 目录
* messager 在Gradle Console输出build过程中的日志信息 



2. getSupportedAnnotationTypes方法

   > 支持解析的注解类型

3. getSupportedSourceVersion方法

   > 支持jdk编译的版本一般为

   ```java
   public SourceVersion getSupportedSourceVersion() {
           return SourceVersion.latestSupported();
   }
   ```

4. process方法

   > AbstractProcessor的核心方法，用于生成对应的代理类

   * 直观解读

   ```java
   public class MainActivity extends Activity{
   @BindView(R.id.tv)
     TextView textView;
     public void onCreate(Bundle onSaveInstance){
       super.onCreate(onSaveInstance);
       setContentView(R.layout.activity_main);
       
       // Note: 这里还没有做注入
       //在添加完@BindView的注解后，直接在build中点击rebuild
     }
   }
   ```

   ​	

   ***

   将上面的代码自己生成自己的代理类。大概是↓这个样子

   ```java
   public class MainActivity$$Proxy implements IProxy<MainActivity>{
     public void inject(MainActivity activity,View root){
       activity.textView = (TextView)root.findViewById(R.id.tv);
       ...
     }
   }
   ```

   > 注：此时生成的代理类是自己的项目是相互独立的

   ​

   #### IProxy介绍

   > 这个是为了以后关联项目与代理类的时候方便调用inject方法做注入

   * 实现

   ```java
   public interface IProxy<T>{
     public void inject(T target,View root);
   }
   ```

   ​

   代理类生成

   [代理类生成请参考square的生成方法](https://github.com/square/javapoet)

   Example

   ```java
   package com.example.helloworld;

   public final class HelloWorld {
     public static void main(String[] args) {
       System.out.println("Hello, JavaPoet!");
     }
   }
   ```

   ↓

   ```java
   MethodSpec main = MethodSpec.methodBuilder("main")
       .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
       .returns(void.class)
       .addParameter(String[].class, "args")
       .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
       .build();

   TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
       .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
       .addMethod(main)
       .build();

   JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
       .build();

   javaFile.writeTo(System.out);
   ```

   ​

   #### process方法继续介绍

   ​