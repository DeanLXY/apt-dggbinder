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

@OnClick(R.id.btn)
public void show(View view){
  // do sth
}
```

