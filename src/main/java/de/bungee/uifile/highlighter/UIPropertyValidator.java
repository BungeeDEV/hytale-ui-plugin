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

        // Check if we're inside a complex property value block (e.g., Padding: (Full: 20))
        String complexPropertyContext = findComplexPropertyContext(element);
        if (complexPropertyContext != null) {
            // Validate against sub-properties for this complex property
            validateComplexPropertySubProperty(element, holder, text, complexPropertyContext);
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
     * Find if we're inside a complex property value block (e.g., Padding: (Full: 20)) Returns the property name (e.g.,
     * "Padding") if inside such a block
     */
    private String findComplexPropertyContext(PsiElement element) {
        PsiElement current = element;
        int parenDepth = 0;

        // Search backwards for matching parentheses
        while (current != null) {
            String text = current.getText().trim();

            if (text.equals(")")) {
                parenDepth++;
            } else if (text.equals("(")) {
                parenDepth--;
                if (parenDepth < 0) {
                    // Found the opening parenthesis, now look for the property name before it
                    PsiElement prev = PsiTreeUtil.prevLeaf(current);
                    while (prev != null) {
                        String prevText = prev.getText().trim();
                        if (prevText.equals(":")) {
                            // Found colon, check for property name before it
                            PsiElement beforeColon = PsiTreeUtil.prevLeaf(prev);
                            while (beforeColon != null) {
                                String beforeColonText = beforeColon.getText().trim();
                                if (!beforeColonText.isEmpty() && !beforeColonText.equals("\n")
                                    && !beforeColonText.equals("\r\n")) {
                                    // Check if this is a complex property (Padding, Margin, Anchor, etc.)
                                    if (isComplexProperty(beforeColonText)) {
                                        return beforeColonText;
                                    }
                                    break;
                                }
                                beforeColon = PsiTreeUtil.prevLeaf(beforeColon);
                            }
                            break;
                        } else if (!prevText.isEmpty() && !prevText.equals("\n") && !prevText.equals("\r\n")) {
                            break;
                        }
                        prev = PsiTreeUtil.prevLeaf(prev);
                    }
                    break;
                }
            }

            current = PsiTreeUtil.prevLeaf(current);
        }

        return null;
    }

    /**
     * Check if a property name represents a complex property with sub-properties
     */
    private boolean isComplexProperty(String propertyName) {
        return propertyName.equals("Padding") ||
               propertyName.equals("Margin") ||
               propertyName.equals("Anchor") ||
               propertyName.equals("Background") ||
               propertyName.equals("ScrollbarStyle") ||
               propertyName.equals("TextTooltipStyle") ||
               propertyName.equals("Sounds") ||
               propertyName.equals("DurabilityBarAnchor") ||
               propertyName.equals("NumberFieldContainerAnchor") ||
               propertyName.equals("ItemGridStyle") ||
               propertyName.equals("Bar") ||
               propertyName.equals("Handle");
    }

    /**
     * Validate a sub-property inside a complex property value block
     */
    private void validateComplexPropertySubProperty(PsiElement element, AnnotationHolder holder,
        String propertyName, String complexProperty) {
        List<String> validSubProperties = getSubPropertiesForComplexProperty(complexProperty);

        if (!validSubProperties.contains(propertyName)) {
            String message = String.format("Unknown sub-property '%s' for '%s'", propertyName, complexProperty);
            holder.newAnnotation(HighlightSeverity.WARNING, message)
                .range(element.getTextRange())
                .create();
        }
    }

    /**
     * Get valid sub-properties for a complex property
     */
    private List<String> getSubPropertiesForComplexProperty(String propertyName) {
        return switch (propertyName) {
            case "Padding", "Margin" -> List.of("Full", "Top", "Bottom", "Left", "Right", "Horizontal", "Vertical");
            case "Anchor" -> List.of("Width", "Height", "Top", "Bottom", "Left", "Right",
                "Full", "Horizontal", "Vertical", "MaxWidth", "MinWidth",
                "MaxHeight", "MinHeight");
            case "Background" -> List.of("TexturePath", "Color", "Border", "HorizontalBorder", "VerticalBorder");
            case "ScrollbarStyle" -> List.of("Spacing", "Size", "OnlyVisibleWhenHovered");
            case "TextTooltipStyle" -> List.of("MaxWidth", "LabelStyle");
            case "Sounds" -> List.of("Volume", "MinPitch", "MaxPitch");
            case "DurabilityBarAnchor", "NumberFieldContainerAnchor" ->
                List.of("Width", "Height", "Top", "Bottom", "Left", "Right");
            case "ItemGridStyle" -> List.of("SlotSize", "SlotIconSize", "SlotSpacing", "SlotBackground");
            case "Bar", "Handle" -> List.of("Background", "Color", "Width", "Height");
            default -> List.of();
        };
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
                // Found opening brace at our level (depth 0 means we're at the same nesting level as the property)
                if (braceDepth == 0) {
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
                    // If we didn't find a valid type, return null
                    return null;
                }
                braceDepth--;
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
                                    if (!beforeText.isEmpty() && !beforeText.equals("\n") && !beforeText.equals(
                                        "\r\n")) {
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

