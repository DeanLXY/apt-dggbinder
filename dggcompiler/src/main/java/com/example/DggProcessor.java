package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class DggProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, ProxyClass> proxyClassMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> options = new LinkedHashSet<>();
        options.add(BindView.class.getCanonicalName());
        return options;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
        Set<? extends Element> bindViewElements = env.getElementsAnnotatedWith(BindView.class);
        for (Element element : bindViewElements) {
            //BindView Field Parameter
            boolean valid = isValid(BindView.class, element);
//        messager.printMessage(Diagnostic.Kind.ERROR,"process...."+bindViewElements.size()+"::"+valid);
            if (!valid) {
                return true;
            }

            processBindView(element);
        }
        return false;
    }

    /**
     * deal with bindView element
     *
     * @param element
     */
    private void processBindView(Element element) {
//        messager.printMessage(Diagnostic.Kind.ERROR,"processBindView....");
        ProxyClass proxyClass = getProxyClass(element);
        ViewBindingModel viewBindingModel = new ViewBindingModel(element);
        proxyClass.addViewBindingModel(viewBindingModel);

        /*GenerateClass generateClass = new GenerateClass(proxyClass);
        generateClass.generateMethod()*/
//        public final void inject(T target,View view)
        MethodSpec.Builder injectMethodBuilder = MethodSpec
                .methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get(Override.class))
                .addParameter(TypeName.get(proxyClass.getTypeElement().asType()), "target")
                .addParameter(ClassName.get("android.view", "View"), "root");
        List<ViewBindingModel> viewBindingModels = proxyClass.getViewBindingModels();
        for (ViewBindingModel model : viewBindingModels) {
            // add fbc (TextView textView = (TextView)root.findViewById(R.id.tv)
            injectMethodBuilder.addStatement(
                    "target.$N =($T)root.findViewById($L)",
                    model.varName,
                    ClassName.get(model.typeMirror),
                    model.resId);
        }
        // typeSpec
        TypeSpec iProxy = TypeSpec
                .classBuilder(proxyClass.getTypeElement().getSimpleName() + "$$Proxy")
                .addModifiers( Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.wangx.dggapi", "IProxy"), TypeName.get(proxyClass.getTypeElement().asType())))
                .addMethod(injectMethodBuilder.build())
                .build();
        String packageName = elementUtils.getPackageOf(proxyClass.getTypeElement()).getQualifiedName().toString();
        JavaFile javaFile = JavaFile
                .builder(packageName, iProxy)
                .build();
        //writeJavaFile
        try {
            javaFile.writeTo(processingEnv.getFiler());
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * generate proxy (a class)
     *
     * @param element
     * @return
     */
    private ProxyClass getProxyClass(Element element) {
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        String parentQualifiedName = typeElement.getQualifiedName().toString();
        ProxyClass proxyClass = proxyClassMap.get(parentQualifiedName);
        if (proxyClass == null) {
            proxyClass = new ProxyClass(typeElement, elementUtils);
            proxyClassMap.put(parentQualifiedName, proxyClass);
        }

        return proxyClass;
    }

    /**
     * BindView Annotaion is Valid on Right Place (Filed or Parameter)
     *
     * @param bindViewClass
     * @param element
     * @return
     */
    private boolean isValid(Class<BindView> bindViewClass, Element element) {
        TypeElement parentElement = (TypeElement) element.getEnclosingElement();

        // modifier
        Set<Modifier> parentModifiers = parentElement.getModifiers();
        for (Modifier modifier : parentModifiers) {
            if (modifier == Modifier.PRIVATE || modifier == Modifier.STATIC) {
                // inValid
                messager.printMessage(Diagnostic.Kind.ERROR, "The Parent Element (class)'s modifier is not allow private | static.");
                return false;
            }
        }

        //kind(maybe class interface Enum)
        ElementKind parentKind = parentElement.getKind();
        if (parentKind != ElementKind.CLASS) {
            messager.printMessage(Diagnostic.Kind.ERROR, "The Parent Element (class)'s kind not allowed");
            // inValid
            return false;
        }

        // android & Java FrameWork is inValid
        String parentQualifiedName = parentElement.getQualifiedName().toString();
        if (parentQualifiedName.startsWith("android.") || parentQualifiedName.startsWith("java.")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "The Parent Element (class) can't be os Element(android.xx  java.xx");
            return false;
        }

        return true;
    }
}
