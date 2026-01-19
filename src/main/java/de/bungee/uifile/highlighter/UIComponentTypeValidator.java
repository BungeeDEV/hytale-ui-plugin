package de.bungee.uifile.highlighter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import de.bungee.uifile.completion.UITypeDefinitions;
import org.jetbrains.annotations.NotNull;

/**
 * Validates that component types used in UI files are valid/known types
 */
public class UIComponentTypeValidator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        String text = element.getText().trim();

        // Skip if this is an ID (starts with #)
        if (text.startsWith("#")) {
            return;
        }

        // Check if this element looks like a potential component type name
        // Component types start with an uppercase letter and are followed by '{'
        if (text.isEmpty() || !Character.isUpperCase(text.charAt(0))) {
            return;
        }

        // Check if previous element is # (meaning this element is the ID name part)
        PsiElement prevElement = PsiTreeUtil.prevLeaf(element);
        while (prevElement != null && prevElement.getText().trim().isEmpty()) {
            prevElement = PsiTreeUtil.prevLeaf(prevElement);
        }

        if (prevElement != null && prevElement.getText().trim().equals("#")) {
            return; // This is an ID name, not a component type
        }

        // Check if next non-whitespace element is an opening brace or an ID (starting with #)
        PsiElement nextElement = PsiTreeUtil.nextLeaf(element);
        while (nextElement != null && nextElement.getText().trim().isEmpty()) {
            nextElement = PsiTreeUtil.nextLeaf(nextElement);
        }

        if (nextElement == null) {
            return;
        }

        String nextText = nextElement.getText().trim();

        // If next element starts with #, it's an ID - skip it and check for { after the ID
        if (nextText.startsWith("#")) {
            // Skip the # and ID name
            nextElement = PsiTreeUtil.nextLeaf(nextElement);
            while (nextElement != null && nextElement.getText().trim().isEmpty()) {
                nextElement = PsiTreeUtil.nextLeaf(nextElement);
            }

            if (nextElement == null || !nextElement.getText().trim().equals("{")) {
                return;
            }
        } else if (!nextText.equals("{")) {
            // If it's not # or {, this is not a component type
            return;
        }

        // Check if previous element suggests this is NOT a component type
        // (e.g., it's after a colon, meaning it's a property value)

        if (prevElement != null && prevElement.getText().trim().equals(":")) {
            return; // This is a property value, not a component type
        }

        // Now check if this is a valid component type
        if (!UITypeDefinitions.isValidType(text)) {
            String message = String.format("Unknown component type '%s'. Valid types: %s",
                text, String.join(", ", UITypeDefinitions.getUITypes()));

            holder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(element.getTextRange())
                .create();
        }
    }
}

