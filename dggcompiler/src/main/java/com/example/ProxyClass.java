package com.example;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @Author: xujie.wang
 * @Email: xujie.wang@17zuoye.com
 * @Date: 2017/5/12
 * @Project: DggBind
 */

public class ProxyClass {
    private TypeElement typeElement;
    private Elements elementUtils;
    public List<ViewBindingModel> viewBindingModels;

    public ProxyClass(TypeElement parentElement, Elements elementUtils) {

        this.typeElement = parentElement;
        this.elementUtils = elementUtils;
        this.viewBindingModels = new ArrayList<>();
    }

    public void addViewBindingModel(ViewBindingModel viewBindingModel){
        if (viewBindingModel != null) {
            viewBindingModels.add(viewBindingModel);
        }
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public List<ViewBindingModel> getViewBindingModels() {
        return viewBindingModels;
    }
}
