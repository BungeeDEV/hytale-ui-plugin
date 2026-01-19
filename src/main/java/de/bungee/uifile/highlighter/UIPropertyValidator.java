package de.bungee.uifile.highlighter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import de.bungee.uifile.completion.UITypeDefinitions;
import de.bungee.uifile.completion.UITypeDefinitions.PropertyInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Validates that properties used in UI components are valid for that component type
 */
public class UIPropertyValidator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        String text = element.getText().trim();

        // Check if this element looks like a property name
        if (text.isEmpty() || !Character.isUpperCase(text.charAt(0))) {
            return;
        }

        // Check if next sibling is a colon (indicating this is a property name)
        PsiElement nextElement = PsiTreeUtil.nextLeaf(element);
        if (nextElement == null || !nextElement.getText().trim().equals(":")) {
            return;
        }

        // Check if we're inside a Style block
        if (isInsideStyleBlock(element)) {
            // Validate against Style properties
            validateStyleProperty(element, holder, text);
            return;
        }

        // Find the parent component type
        String componentType = findParentComponentType(element);
        if (componentType == null || !UITypeDefinitions.isValidType(componentType)) {
            return;
        }

        // Get valid properties for this component
        List<PropertyInfo> validProperties = UITypeDefinitions.getPropertiesForType(componentType);

        // Check if the property is valid for this component
        boolean isValid = validProperties.stream()
            .anyMatch(prop -> prop.name().equals(text));

        if (!isValid) {
            // Check if it's a commonly valid property (for better error message)
            boolean isCommonProperty = isCommonProperty(text);

            String message;
            if (isCommonProperty) {
                message = String.format("Property '%s' is not supported for component type '%s'",
                    text, componentType);
            } else {
                message = String.format("Unknown property '%s' for component type '%s'",
                    text, componentType);
            }

            holder.newAnnotation(HighlightSeverity.WARNING, message)
                .range(element.getTextRange())
                .create();
        }
    }

    /**
     * Find the parent component type by searching backwards through the text
     */
    private String findParentComponentType(PsiElement element) {
        PsiElement current = element;
        int braceDepth = 0;

        while (current != null) {
            String text = current.getText();

            // Track brace depth
            if (text.equals("}")) {
                braceDepth++;
            } else if (text.equals("{")) {
                braceDepth--;

                // Found opening brace at our level, now look for the type name before it
                if (braceDepth < 0) {
                    PsiElement prev = PsiTreeUtil.prevLeaf(current);
                    while (prev != null) {
                        String prevText = prev.getText().trim();
                        if (!prevText.isEmpty() && !prevText.equals("\n") && !prevText.equals("\r\n")) {
                            // If we encounter an ID (starting with #), skip it and continue looking
                            if (prevText.startsWith("#")) {
                                prev = PsiTreeUtil.prevLeaf(prev);
                                continue;
                            }
                            // Check if this is a known UI type
                            if (UITypeDefinitions.isValidType(prevText)) {
                                return prevText;
                            }
                            break;
                        }
                        prev = PsiTreeUtil.prevLeaf(prev);
                    }
                }
            }

            current = PsiTreeUtil.prevLeaf(current);
        }

        return null;
    }

    /**
     * Check if a property name is a common property (like Style)
     */
    private boolean isCommonProperty(String propertyName) {
        return propertyName.equals("Style") ||
               propertyName.equals("Background") ||
               propertyName.equals("Padding") ||
               propertyName.equals("Margin") ||
               propertyName.equals("Width") ||
               propertyName.equals("Height") ||
               propertyName.equals("Visible") ||
               propertyName.equals("Enabled");
    }

    /**
     * Check if the element is inside a Style block
     */
    private boolean isInsideStyleBlock(PsiElement element) {
        PsiElement current = element;
        int braceDepth = 0;

        while (current != null) {
            String text = current.getText().trim();

            // Track brace depth
            if (text.equals("}") || text.equals(")")) {
                braceDepth++;
            } else if (text.equals("{") || text.equals("(")) {
                braceDepth--;

                // Found opening brace/paren at our level, check if it's preceded by "Style:"
                if (braceDepth < 0) {
                    PsiElement prev = PsiTreeUtil.prevLeaf(current);
                    while (prev != null) {
                        String prevText = prev.getText().trim();
                        if (!prevText.isEmpty() && !prevText.equals("\n") && !prevText.equals("\r\n")) {
                            // If we find ":" check what's before it
                            if (prevText.equals(":")) {
                                PsiElement beforeColon = PsiTreeUtil.prevLeaf(prev);
                                while (beforeColon != null) {
                                    String beforeText = beforeColon.getText().trim();
                                    if (!beforeText.isEmpty() && !beforeText.equals("\n") && !beforeText.equals("\r\n")) {
                                        return beforeText.equals("Style");
                                    }
                                    beforeColon = PsiTreeUtil.prevLeaf(beforeColon);
                                }
                            }
                            break;
                        }
                        prev = PsiTreeUtil.prevLeaf(prev);
                    }
                    // Reset for next level
                    braceDepth = 0;
                }
            }

            current = PsiTreeUtil.prevLeaf(current);
        }

        return false;
    }

    /**
     * Validate a property inside a Style block
     */
    private void validateStyleProperty(PsiElement element, AnnotationHolder holder, String propertyName) {
        // Get the list of valid style properties
        List<PropertyInfo> styleProperties = UITypeDefinitions.getStyleProperties();

        boolean isValid = styleProperties.stream()
            .anyMatch(prop -> prop.name().equals(propertyName));

        if (!isValid) {
            String message = String.format("Unknown style property '%s'", propertyName);
            holder.newAnnotation(HighlightSeverity.WARNING, message)
                .range(element.getTextRange())
                .create();
        }
    }
}

