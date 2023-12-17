package edu.school21.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes({"edu.school21.processor.HtmlForm", "edu.school21.processor.HtmlInput"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class HtmlProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(HtmlForm.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                processFormAnnotation((TypeElement) element);
            }
        }
        return true;
    }

    private void processFormAnnotation(TypeElement element) {
        HtmlForm formAnnotation = element.getAnnotation(HtmlForm.class);
        String fileName = formAnnotation.fileName();
        String action = formAnnotation.action();
        String method = formAnnotation.method();

        try {
            PrintWriter writer = new PrintWriter(processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", fileName, element).openWriter());
            writer.println("<form action = \"" + action + "\" method = \"" + method + "\">");

            for (Element enclosedElement : element.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.FIELD) {
                    processInputAnnotation(enclosedElement, writer);
                }
            }

            writer.println("</form>");
            writer.close();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error creating HTML file");
        }
    }

    private void processInputAnnotation(Element element, PrintWriter writer) {
        HtmlInput inputAnnotation = element.getAnnotation(HtmlInput.class);
        String type = inputAnnotation.type();
        String name = inputAnnotation.name();
        String placeholder = inputAnnotation.placeholder();

        writer.println("\t<input type = \"" + type + "\" name = \"" + name + "\" placeholder = \"" + placeholder + "\">");
    }
}
