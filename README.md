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