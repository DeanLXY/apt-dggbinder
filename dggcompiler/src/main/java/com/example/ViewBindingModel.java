package com.example;

/**
 * @BindView(R.id.tv)
 * TextView tv;
 */

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/12
 * @Project: DggBind
 */
public class ViewBindingModel {
    public VariableElement element;
    public int resId;
    public String varName;
    public TypeMirror typeMirror;

    public ViewBindingModel(Element element) {
        this.element = (VariableElement) element;
        this.resId = element.getAnnotation(BindView.class).value();
        this.varName = this.element.getSimpleName().toString();
        this.typeMirror = this.element.asType();
    }
}
