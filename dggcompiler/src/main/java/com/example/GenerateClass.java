package com.example;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/12
 * @Project: DggBind
 */

public class GenerateClass {
    private ProxyClass proxyClass;

    public GenerateClass(ProxyClass proxyClass) {
        this.proxyClass = proxyClass;
    }

    /**
     * note:
     * public final void inject(T target,View view)
     * @param method
     * @return
     */
    public MethodSpec.Builder generateMethod(String method){
        MethodSpec
                .methodBuilder(method)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addParameter(TypeName.get(this.proxyClass.getTypeElement().asType()), "target")
                .addParameter(ClassName.get("android.view", "View"), "view");
        return null;
    }

    public TypeSpec.Builder generateType(){
        return null;
    }
}
