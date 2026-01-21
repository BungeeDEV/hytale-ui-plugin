package de.bungee.uifile.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import de.bungee.uifile.UILanguage;
import de.bungee.uifile.completion.UITypeDefinitions.PropertyInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides code completion for UI files
 */
public class UICompletionContributor extends CompletionContributor {

    public UICompletionContributor() {
        // Completion for all contexts in UI files
        extend(CompletionType.BASIC,
            PlatformPatterns.psiElement().inFile(
                PlatformPatterns.psiFile().withLanguage(UILanguage.INSTANCE)
            ),
            new CompletionProvider<>() {
                @Override
                protected void addCompletions(@NotNull CompletionParameters parameters,
                    @NotNull ProcessingContext context,
                    @NotNull CompletionResultSet result) {
                    PsiElement position = parameters.getPosition();

                    // Check if we're inside a complex property value block (e.g., Padding: (Full: 20))
                    String complexPropertyContext = findComplexPropertyContext(position);
                    if (complexPropertyContext != null) {
                        // We're inside a complex property value - suggest sub-properties
                        addComplexPropertyCompletions(result, complexPropertyContext);
                        return;
                    }

                    // Check if we're trying to complete a property value
                    String propertyName = findPropertyNameBeforeColon(position);
                    if (propertyName != null) {
                        // We're after a colon, completing a value
                        addValueCompletions(result, propertyName);
                        return;
                    }

                    // Check if we're inside a Style block (between parentheses after "Style:")
                    String styleContext = findStyleBlockContext(position);
                    if (styleContext != null) {
                        // We're inside a Style block - suggest style properties
                        String componentType = findParentComponentType(position);
                        if (componentType != null) {
                            addStylePropertyCompletions(result, componentType);
                            return;
                        }
                    }

                    // Get context - are we inside a component block?
                    String componentType = findParentComponentType(position);

                    if (componentType != null) {
                        // We're inside a component - suggest properties
                        addPropertyCompletions(result, componentType);
                    }

                    // Always suggest UI component types
                    addTypeCompletions(result);
                }
            }
        );
    }

    /**
     * Find if we're inside a complex property value block (e.g., Padding: (Full: 20))
     * Returns the property name (e.g., "Padding") if inside such a block
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
                                if (!beforeColonText.isEmpty() && !beforeColonText.equals("\n") && !beforeColonText.equals("\r\n")) {
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
               propertyName.equals("NumberFieldContainerAnchor");
    }

    /**
     * Add completions for sub-properties inside complex property blocks
     */
    private void addComplexPropertyCompletions(@NotNull CompletionResultSet result, String propertyName) {
        List<String> subProperties = getSubPropertiesForComplexProperty(propertyName);

        for (String subProp : subProperties) {
            result.addElement(
                LookupElementBuilder.create(subProp)
                    .withTypeText(propertyName + " sub-property")
                    .withInsertHandler((insertContext, item) -> {
                        // Add colon and space after the sub-property
                        int offset = insertContext.getEditor().getCaretModel().getOffset();
                        insertContext.getDocument().insertString(offset, ": ");
                        insertContext.getEditor().getCaretModel().moveToOffset(offset + 2);
                    })
            );
        }
    }

    /**
     * Get sub-properties for a complex property
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
            default -> List.of();
        };
    }

    /**
     * Find the property name before a colon (to provide value completions)
     */
    private String findPropertyNameBeforeColon(PsiElement element) {
        PsiElement current = element;

        // Search backwards for a colon
        while (current != null) {
            String text = current.getText().trim();

            // Skip whitespace
            if (text.isEmpty() || text.equals("\n") || text.equals("\r\n")) {
                current = PsiTreeUtil.prevLeaf(current);
                continue;
            }

            // If we hit a colon, look for the property name before it
            if (text.equals(":")) {
                PsiElement prev = PsiTreeUtil.prevLeaf(current);
                while (prev != null) {
                    String prevText = prev.getText().trim();
                    if (!prevText.isEmpty() && !prevText.equals("\n") && !prevText.equals("\r\n")) {
                        // Check if it looks like a property name (starts with uppercase)
                        if (Character.isUpperCase(prevText.charAt(0))) {
                            return prevText;
                        }
                        break;
                    }
                    prev = PsiTreeUtil.prevLeaf(prev);
                }
                return null;
            }

            // If we hit a semicolon, closing brace, or opening brace, stop
            if (text.equals(";") || text.equals("}") || text.equals("{") ||
                text.equals(")") || text.equals("(")) {
                return null;
            }

            current = PsiTreeUtil.prevLeaf(current);
        }

        return null;
    }

    /**
     * Add value completions for a specific property
     */
    private void addValueCompletions(@NotNull CompletionResultSet result, String propertyName) {
        List<String> suggestions = UIValueCompletions.getValueSuggestionsForProperty(propertyName);

        for (String value : suggestions) {
            String description = UIValueCompletions.getValueDescription(propertyName, value);

            result.addElement(
                LookupElementBuilder.create(value)
                    .withTypeText(propertyName)
                    .withTailText(description.isEmpty() ? "" : " - " + description, true)
            );
        }

        // Add specific examples for complex properties
        if (propertyName.equals("Background")) {
            for (String example : UIValueCompletions.getBackgroundExamples()) {
                result.addElement(
                    LookupElementBuilder.create(example)
                        .withPresentableText(example)
                        .withTypeText("Example")
                        .withTailText(" - Background pattern example", true)
                );
            }
        } else if (propertyName.equals("Anchor")) {
            for (String example : UIValueCompletions.getAnchorExamples()) {
                result.addElement(
                    LookupElementBuilder.create(example)
                        .withPresentableText(example)
                        .withTypeText("Example")
                        .withTailText(" - Anchor configuration", true)
                );
            }
        } else if (propertyName.equals("Padding") || propertyName.equals("Margin")) {
            for (String example : UIValueCompletions.getPaddingExamples()) {
                result.addElement(
                    LookupElementBuilder.create(example)
                        .withPresentableText(example)
                        .withTypeText("Example")
                        .withTailText(" - " + propertyName + " configuration", true)
                );
            }
        } else if (propertyName.equals("ScrollbarStyle")) {
            for (String example : UIValueCompletions.getScrollbarStyleExamples()) {
                result.addElement(
                    LookupElementBuilder.create(example)
                        .withPresentableText(example)
                        .withTypeText("Example")
                        .withTailText(" - Scrollbar style", true)
                );
            }
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

                // Found opening brace, now look for the type name before it
                if (braceDepth < 0) {
                    PsiElement prev = PsiTreeUtil.prevLeaf(current);
                    while (prev != null) {
                        String prevText = prev.getText().trim();
                        if (!prevText.isEmpty() && !prevText.equals("\n") && !prevText.equals("\r\n")) {
                            // Skip IDs (starting with #)
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
     * Check if we're inside a Style block by looking for "Style:(" pattern Returns "Style" if inside a style block,
     * null otherwise
     */
    private String findStyleBlockContext(PsiElement element) {
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
                    // Found the opening parenthesis, now look for "Style:" before it
                    PsiElement prev = PsiTreeUtil.prevLeaf(current);
                    while (prev != null) {
                        String prevText = prev.getText().trim();
                        if (prevText.equals(":")) {
                            // Found colon, check for "Style" before it
                            PsiElement beforeColon = PsiTreeUtil.prevLeaf(prev);
                            while (beforeColon != null) {
                                String beforeColonText = beforeColon.getText().trim();
                                if (!beforeColonText.isEmpty()) {
                                    if (beforeColonText.equals("Style")) {
                                        return "Style";
                                    }
                                    break;
                                }
                                beforeColon = PsiTreeUtil.prevLeaf(beforeColon);
                            }
                            break;
                        } else if (!prevText.isEmpty() && !prevText.equals("\n")) {
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
     * Add UI component type completions
     */
    private void addTypeCompletions(@NotNull CompletionResultSet result) {
        for (String type : UITypeDefinitions.getUITypes()) {
            // Bestimme die Kategorie für bessere Organisation
            String category = categorizeComponent(type);

            result.addElement(
                LookupElementBuilder.create(type + " {\n    \n}")
                    .withPresentableText(type)
                    .withTypeText(category)
                    .withInsertHandler((insertContext, item) -> {
                        // Move the cursor inside the braces
                        int offset = insertContext.getEditor().getCaretModel().getOffset();
                        insertContext.getEditor().getCaretModel().moveToOffset(offset - 2);
                    })
                    .bold()
            );
        }
    }

    /**
     * Kategorisiere UI-Components für bessere Übersichtlichkeit
     */
    private String categorizeComponent(String type) {
        // Layout Components
        if (type.contains("Group") || type.contains("Container") || type.equals("Panel") ||
            type.equals("Row") || type.equals("Content") || type.equals("Wrapper")) {
            return "Layout";
        }
        // Button Components
        if (type.contains("Button")) {
            return "Button";
        }
        // Text/Label Components
        if (type.contains("Label") || type.contains("Title")) {
            return "Text";
        }
        // Input Components
        if (type.contains("TextField") || type.contains("Field") || type.contains("Input")) {
            return "Input";
        }
        // Item/Inventory Components
        if (type.contains("Item") || type.contains("Slot") || type.contains("Grid")) {
            return "Inventory";
        }
        // Image Components
        if (type.contains("Image") || type.equals("Icon") || type.equals("Sprite")) {
            return "Image";
        }
        // Slider Components
        if (type.contains("Slider") || type.contains("Progress")) {
            return "Control";
        }
        // Selection Components
        if (type.contains("CheckBox") || type.contains("Dropdown") || type.contains("Selector")) {
            return "Selection";
        }
        // Tab/Navigation Components
        if (type.contains("Tab") || type.contains("Navigation") || type.contains("Page") || type.contains("Menu")) {
            return "Navigation";
        }
        // Separators
        if (type.contains("Separator") || type.equals("Divider") || type.equals("Sep")) {
            return "Layout";
        }
        // Overlays
        if (type.contains("Overlay") || type.contains("Popup")) {
            return "Overlay";
        }
        // Preview Components
        if (type.contains("Preview")) {
            return "Preview";
        }
        // Style Components
        if (type.contains("Style")) {
            return "Style";
        }
        return "UI Component";
    }

    /**
     * Add property completions for a specific component type
     */
    private void addPropertyCompletions(@NotNull CompletionResultSet result, String componentType) {
        List<PropertyInfo> properties = UITypeDefinitions.getPropertiesForType(componentType);

        for (PropertyInfo prop : properties) {
            String insertText = getInsertTextForProperty(prop);

            result.addElement(
                LookupElementBuilder.create(insertText)
                    .withPresentableText(prop.name())
                    .withTypeText(prop.valueType())
                    .withTailText(": " + prop.description(), true)
                    .withInsertHandler((insertContext, item) -> {
                        // Move the cursor to the value position
                        int offset = insertContext.getEditor().getCaretModel().getOffset();
                        String text = insertContext.getDocument().getText();

                        // Find the position after the colon and space
                        int colonPos = text.indexOf(':', Math.max(0, offset - insertText.length()));
                        if (colonPos != -1) {
                            insertContext.getEditor().getCaretModel().moveToOffset(colonPos + 2);
                        }
                    })
            );
        }
    }

    /**
     * Add style property completions when inside a Style block
     */
    private void addStylePropertyCompletions(@NotNull CompletionResultSet result, String componentType) {
        List<PropertyInfo> styleProperties = UITypeDefinitions.getSubPropertiesForProperty(componentType, "Style");

        for (PropertyInfo prop : styleProperties) {
            String insertText = getInsertTextForStyleProperty(prop);

            result.addElement(
                LookupElementBuilder.create(insertText)
                    .withPresentableText(prop.name())
                    .withTypeText(prop.valueType())
                    .withTailText(" - " + prop.description(), true)
                    .withInsertHandler((insertContext, item) -> {
                        // Move the cursor to the value position
                        int offset = insertContext.getEditor().getCaretModel().getOffset();
                        String text = insertContext.getDocument().getText();

                        // Find the position after the colon
                        int colonPos = text.indexOf(':', Math.max(0, offset - insertText.length()));
                        if (colonPos != -1) {
                            insertContext.getEditor().getCaretModel().moveToOffset(colonPos + 1);
                        }
                    })
            );
        }
    }

    /**
     * Generate appropriate insert text based on a property type
     */
    private String getInsertTextForProperty(PropertyInfo prop) {
        return switch (prop.valueType()) {
            case "color" -> prop.name() + ": #";
            case "string" -> prop.name() + ": \"\";";
            case "number" -> prop.name() + ": 0;";
            case "boolean" -> prop.name() + ": true;";
            case "style block" -> prop.name() + ": ();";
            case "padding value", "margin value" -> prop.name() + ": (Full: 0);";
            case "anchor value" -> prop.name() + ": (Width: 0, Height: 0);";
            default -> prop.name() + ": ;";
        };
    }

    /**
     * Generate appropriate insert text for style properties (inside Style block)
     */
    private String getInsertTextForStyleProperty(PropertyInfo prop) {
        return switch (prop.valueType()) {
            case "color" -> prop.name() + ":#";
            case "string" -> prop.name() + ":\"\"";
            case "boolean" -> prop.name() + ":true";
            case "alignment" -> prop.name() + ":Center";
            default -> prop.name() + ":";
        };
    }
}

