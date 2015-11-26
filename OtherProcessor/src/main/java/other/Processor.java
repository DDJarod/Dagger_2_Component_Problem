package other;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("javax.inject.Inject")
public class Processor extends AbstractProcessor
{
  private final Set<TypeElement> injectedClasses = new HashSet<TypeElement>();

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv)
  {
    if(!this.injectedClasses.isEmpty())
    {
      // the check will cause this processor to never process generated files, neither from this processor,
      // nor from others!
      return false;
    }
    final Filer filer = this.processingEnv.getFiler();

    for (final TypeElement typeElement : annotations)
    {
      final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(typeElement);
      this.log("processing run, already know " + this.injectedClasses.size() + " classes, check "
        + annotatedElements.size() + " new classes");

      for (final Element element : annotatedElements)
      {
        if(element.getKind() == ElementKind.CONSTRUCTOR)
        {
          final Element enclosingElement = element.getEnclosingElement();
          if(enclosingElement instanceof TypeElement &&
              enclosingElement.getSimpleName().toString().endsWith("Base"))
          {
            if(this.injectedClasses.add((TypeElement) enclosingElement))
            {
              this.log(enclosingElement.toString() + " added to list of classes to be processed");
            }
          }
        }
      }
    }

    if(!this.injectedClasses.isEmpty())
    {
      try
      {
        this.createTaskMembersProviderCollector(this.injectedClasses,
                                                filer);
      }
      catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
    }

    return false;
  }

  private void createTaskMembersProviderCollector(final Set<TypeElement> taskClasses,
                                                  final Filer filer) throws IOException
  {
    final com.squareup.javapoet.TypeSpec.Builder classBuilder =
      TypeSpec.classBuilder("GeneratedByOtherProcessor")
        .addModifiers(Modifier.PUBLIC,
                      Modifier.FINAL)
        .addSuperinterface(RandomInterface.class)
        .addMethod(MethodSpec.constructorBuilder()
          .addAnnotation(Inject.class)
          .addModifiers(Modifier.PUBLIC)
          .build());

    final com.squareup.javapoet.MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toString")
      .addAnnotation(Override.class)
      .addModifiers(Modifier.PUBLIC)
      .addCode("return ")
      .returns(String.class);

    boolean firstElement = true;
    for (final TypeElement taskClass : taskClasses)
    {
      if(firstElement)
      {
        firstElement = false;
        methodBuilder.addCode("$S \n",
                              taskClass.getQualifiedName().toString());
      }
      else
      {
        methodBuilder.addCode(" + $S \n",
                              taskClass.getQualifiedName().toString());
      }

      final FieldSpec elementInjector =
        FieldSpec.builder(ClassName.bestGuess(taskClass.getQualifiedName().toString()),
                          taskClass.getSimpleName().toString())
          .addAnnotation(Inject.class)
          .build();

      classBuilder.addField(elementInjector);

    }
    methodBuilder.addCode("; \n");

    final MethodSpec methodSpec = methodBuilder.build();

    classBuilder.addMethod(methodSpec);

    final JavaFile javaFile = JavaFile.builder("generated",
                                               classBuilder.build())
      .build();

    javaFile.writeTo(filer);
  }

  private void log(final String str)
  {
    final Kind note = Kind.NOTE;
    this.processingEnv.getMessager().printMessage(note,
                                                  str);
  }
}
