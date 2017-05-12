# 利用APT实现Android编译时注解

[TOC]

## APT

> Annotaion Processing Tool(注解处理工具)

### APT概述

Java为了提高其扩展性，提供了反射机制可以处理注解。其实我们还可以在编译时处理注解，这就要用到官方为我们提供的注解处理工具APT(Annotaion Processing Tool).

为了提高易读性，我们先来看以下一段代码