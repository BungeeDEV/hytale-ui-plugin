package de.bungee.uifile.highlighter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Annotator to validate semicolon rules for UI files.
 *
 * <p>Semicolon Rules:
 * <ul>
 *   <li>Variable definitions ($Var = ...) REQUIRE semicolon</li>
 *   <li>Style definitions (@StyleDef = ...) REQUIRE semicolon</li>
 *   <li>Property assignments (PropertyName: Value) REQUIRE semicolon</li>
 *   <li>Component blocks (Group { ... }) NO semicolon after closing brace</li>
 *   <li>Tuple elements (Width: 500, Height: 320) NO semicolons inside parentheses</li>
 * </ul>
 */
public class UISemicolonValidator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Only process leaf elements (actual tokens from the lexer)
        if (element.getChildren().length > 0) {
            return; // Skip composite elements
        }

        String text = element.getText().trim();

        // Skip irrelevant elements
        if (text.isEmpty() || isStructuralToken(text)) {
            return;
        }

        // Check if this element is the start of a statement that needs a semicolon
        StatementInfo statementInfo = detectStatement(element);

        if (statementInfo != null && !statementInfo.hasSemicolon) {
            // Annotate the current element (the start of the statement)
            TextRange elementRange = element.getTextRange();

            // Get the end offset from the last element for the fix
            int fixOffset = statementInfo.lastElement.getTextRange().getEndOffset();

            holder.newAnnotation(HighlightSeverity.ERROR, "Missing semicolon at end of statement")
                .range(elementRange)
                .withFix(new AddSemicolonFix(fixOffset))
                .create();
        }

        // Check for unnecessary semicolons after closing braces
        if (text.equals("}")) {
            checkUnnecessarySemicolonAfterBrace(element, holder);
        }
    }

    private boolean isStructuralToken(String text) {
        return text.equals("{") || text.equals("}") ||
               text.equals("(") || text.equals(")") ||
               text.equals(",") || text.equals(";") ||
               text.equals(":") || text.equals("=");
    }

    /**
     * Detect if this element starts a statement that requires a semicolon.
     *
     * <p>Rules:
     * <ul>
     *   <li>Variable definitions: $VarName = ... → requires semicolon</li>
     *   <li>Style definitions: @StyleName = ... → requires semicolon (except if ends with } block)</li>
     *   <li>Property assignments: PropertyName: Value → requires semicolon (but not inside parentheses)</li>
     *   <li>Component blocks: ComponentName { } → NO semicolon after }</li>
     * </ul>
     */
    private StatementInfo detectStatement(PsiElement element) {
        String text = element.getText().trim();

        // Regel 5: Variable definition: $VarName = ...
        if (text.startsWith("$")) {
            return analyzeAssignment(element);
        }

        // Regel 5: Style definition: @StyleName = ...
        if (text.startsWith("@")) {
            return analyzeAssignment(element);
        }

        // Regel 6: Property assignment: PropertyName: value
        // Only check if we're NOT inside parentheses (Regel 4)
        if (isPropertyName(text) && !isInsideParentheses(element)) {
            PsiElement next = skipWhitespace(PsiTreeUtil.nextLeaf(element));
            if (next != null && next.getText().trim().equals(":")) {
                return analyzePropertyAssignment(next);
            }
        }

        return null;
    }

    /**
     * Check if text looks like a property name (starts with uppercase letter)
     */
    private boolean isPropertyName(String text) {
        return !text.isEmpty() && Character.isUpperCase(text.charAt(0)) &&
               Character.isJavaIdentifierStart(text.charAt(0));
    }

    /**
     * Regel 4: Check if element is inside parentheses. Elements inside parentheses use commas as separators, NOT
     * semicolons.
     * <p>
     * Example: (Width: 500, Height: 320) - no semicolons inside
     */
    private boolean isInsideParentheses(PsiElement element) {
        int parenDepth = 0;
        PsiElement current = PsiTreeUtil.prevLeaf(element);
        int stepsBack = 0;

        while (current != null && stepsBack < 100) {
            String text = current.getText().trim();

            if (text.isEmpty()) {
                current = PsiTreeUtil.prevLeaf(current);
                stepsBack++;
                continue;
            }

            if (text.equals(")")) {
                parenDepth++;
            } else if (text.equals("(")) {
                parenDepth--;
                if (parenDepth < 0) {
                    // We're inside parentheses
                    return true;
                }
            } else if (parenDepth == 0) {
                // At top level - check for statement boundaries
                if (text.equals(";") || text.equals("{") || text.equals("}")) {
                    // Hit a statement boundary at top level
                    return false;
                }
            }

            current = PsiTreeUtil.prevLeaf(current);
            stepsBack++;
        }

        return false;
    }

    /**
     * Regel 5: Analyze variable or style assignment: $X = ... or @Y = ...
     *
     * <p>These ALWAYS require a semicolon, UNLESS:
     * <ul>
     *   <li>The assignment uses a component block: $C.@Component { } → NO semicolon after }</li>
     * </ul>
     *
     * <p>Examples:
     * <pre>
     * $C = "../Common.ui";          // Requires semicolon
     * {@code @Style = TextButtonStyle(...);} // Requires semicolon
     * $C.@TextField { }              // NO semicolon after }
     * </pre>
     */
    private StatementInfo analyzeAssignment(PsiElement startElement) {
        PsiElement current = startElement;
        PsiElement lastElement = startElement;
        boolean foundEquals = false;
        boolean foundSemicolon = false;
        boolean endsWithBrace = false;

        int parenDepth = 0;
        int braceDepth = 0;

        // Move forward through the statement
        while (current != null) {
            PsiElement next = PsiTreeUtil.nextLeaf(current);
            if (next == null) {
                break;
            }

            String nextText = next.getText().trim();

            // Skip whitespace
            if (nextText.isEmpty()) {
                current = next;
                continue;
            }

            // Track equals sign
            if (!foundEquals && nextText.equals("=")) {
                foundEquals = true;
                current = next;
                continue;
            }

            // Track parentheses and braces
            if (nextText.equals("(")) {
                parenDepth++;
                lastElement = next;
            } else if (nextText.equals(")")) {
                parenDepth--;
                lastElement = next;
            } else if (nextText.equals("{")) {
                braceDepth++;
                lastElement = next;
            } else if (nextText.equals("}")) {
                braceDepth--;
                lastElement = next;
                // Regel 3: Closing brace at top level → NO semicolon required
                if (braceDepth == 0) {
                    endsWithBrace = true;
                }
            } else if (nextText.equals(";")) {
                // Found semicolon!
                foundSemicolon = true;
                break;
            } else if (parenDepth == 0 && braceDepth == 0) {
                // At top level, check if we've hit a new statement
                if (nextText.startsWith("$") || nextText.startsWith("@")) {
                    // This is the start of a new statement (variable or style definition)
                    break;
                }
                // Check if this looks like a property name followed by colon (new property assignment)
                if (isPropertyName(nextText)) {
                    PsiElement afterNext = skipWhitespace(PsiTreeUtil.nextLeaf(next));
                    if (afterNext != null && afterNext.getText().trim().equals(":")) {
                        // This is a new property assignment - we're done
                        break;
                    }
                }
                lastElement = next;
            } else {
                // Inside nested structure
                lastElement = next;
            }

            current = next;
        }

        // Regel 5 & 3: Report if no semicolon found AND doesn't end with brace
        if (foundEquals && !foundSemicolon && !endsWithBrace) {
            return new StatementInfo(lastElement, false);
        }

        return null;
    }

    /**
     * Regel 6: Analyze property assignment: PropertyName: value
     *
     * <p>Property assignments REQUIRE semicolon UNLESS:
     * <ul>
     *   <li>The line ends with { (block start) - this is a component, not a property</li>
     *   <li>We're inside parentheses (handled by caller)</li>
     * </ul>
     *
     * <p>Examples:
     * <pre>
     * Text: "Hello";                        // Requires semicolon
     * Anchor: (Width: 500, Height: 320);    // Requires semicolon
     * LayoutMode: Top;                      // Requires semicolon
     * Label {                                // NO semicolon - this is a component
     * </pre>
     */
    private StatementInfo analyzePropertyAssignment(PsiElement colonElement) {
        PsiElement lastElement = colonElement;
        boolean foundSemicolon = false;

        int parenDepth = 0;

        PsiElement current = colonElement;

        // Move forward through the value
        while (current != null) {
            PsiElement next = PsiTreeUtil.nextLeaf(current);
            if (next == null) {
                break;
            }

            String nextText = next.getText().trim();

            // Skip whitespace
            if (nextText.isEmpty()) {
                current = next;
                continue;
            }

            // Track parentheses
            if (nextText.equals("(")) {
                parenDepth++;
                lastElement = next;
            } else if (nextText.equals(")")) {
                parenDepth--;
                lastElement = next;
                // If paren depth goes negative, belongs to outer structure
                if (parenDepth < 0) {
                    break;
                }
            } else if (nextText.equals("{")) {
                // Regel 2: Opening brace after property name means this is a component block, not a property
                // Property assignments don't use braces
                return null;
            } else if (nextText.equals("}")) {
                // Hit closing brace - end of containing component block
                break;
            } else if (nextText.equals(";")) {
                // Found semicolon!
                foundSemicolon = true;
                break;
            } else if (parenDepth == 0) {
                // At top level (outside parentheses)
                if (nextText.startsWith("$") || nextText.startsWith("@")) {
                    // Check if this is a style reference (value) or a new statement
                    // If we just saw a colon, this @ is a reference value, not a new statement
                    PsiElement beforeThis = skipWhitespaceBackward(PsiTreeUtil.prevLeaf(next));
                    if (beforeThis != null && (beforeThis.getText().trim().equals(":") || beforeThis.getText().trim().equals("."))) {
                        // This is a style reference like "Style: @ButtonStyle" or "Style: $Common.@ButtonStyle
                        lastElement = next;
                        current = next;
                        continue;
                    }
                    // Start of new variable/style statement
                    break;
                } else if (isPropertyName(nextText)) {
                    // Could be another property - check if followed by colon
                    PsiElement afterNext = skipWhitespace(PsiTreeUtil.nextLeaf(next));
                    if (afterNext != null && afterNext.getText().trim().equals(":")) {
                        // Yes, it's a new property assignment
                        break;
                    }
                }
                lastElement = next;
            } else {
                // Inside parentheses
                lastElement = next;
            }

            current = next;
        }

        // Regel 6: Property assignments require semicolon
        if (!foundSemicolon) {
            return new StatementInfo(lastElement, false);
        }

        return null;
    }

    /**
     * Regel 3: Check for unnecessary semicolons after closing braces. Component blocks should NOT have semicolons after
     * the closing brace.
     *
     * <p>Example (WRONG): Group { } ;
     * <p>Example (CORRECT): Group { }
     */
    private void checkUnnecessarySemicolonAfterBrace(PsiElement braceElement, AnnotationHolder holder) {
        // Check if this is a component block closing brace
        if (!isComponentBlockClosingBrace(braceElement)) {
            return;
        }

        // Look for semicolon immediately after the closing brace
        PsiElement next = skipWhitespace(PsiTreeUtil.nextLeaf(braceElement));
        if (next != null && next.getText().trim().equals(";")) {
            TextRange range = next.getTextRange();
            holder.newAnnotation(HighlightSeverity.ERROR, "Unnecessary semicolon after component block")
                .range(range)
                .withFix(new RemoveSemicolonFix(next))
                .create();
        }
    }

    /**
     * Check if a closing brace belongs to a component block. We identify component blocks by checking if there's an
     * opening brace that follows an identifier (component name).
     */
    private boolean isComponentBlockClosingBrace(PsiElement braceElement) {
        // Track brace depth to find matching opening brace
        int braceDepth = 1; // We start at the closing brace
        PsiElement current = PsiTreeUtil.prevLeaf(braceElement);

        while (current != null && braceDepth > 0) {
            String text = current.getText().trim();

            if (text.isEmpty()) {
                current = PsiTreeUtil.prevLeaf(current);
                continue;
            }

            if (text.equals("}")) {
                braceDepth++;
            } else if (text.equals("{")) {
                braceDepth--;

                if (braceDepth == 0) {
                    // Found matching opening brace - check what's before it
                    PsiElement beforeBrace = skipWhitespaceBackward(PsiTreeUtil.prevLeaf(current));
                    if (beforeBrace != null) {
                        String beforeText = beforeBrace.getText().trim();
                        // Check if it's an identifier (component name or ID)
                        // Could be: Group {, Label {, #ID {, etc.
                        return beforeText.matches("[A-Za-z_][A-Za-z0-9_]*") || beforeText.startsWith("#");
                    }
                    return false;
                }
            }

            current = PsiTreeUtil.prevLeaf(current);
        }

        return false;
    }

    /**
     * Skip whitespace elements backwards
     */
    private PsiElement skipWhitespaceBackward(PsiElement element) {
        while (element != null && element.getText().trim().isEmpty()) {
            element = PsiTreeUtil.prevLeaf(element);
        }
        return element;
    }

    /**
     * Skip whitespace elements
     */
    private PsiElement skipWhitespace(PsiElement element) {
        while (element != null && element.getText().trim().isEmpty()) {
            element = PsiTreeUtil.nextLeaf(element);
        }
        return element;
    }

    /**
     * Information about a statement
     */
    private record StatementInfo(PsiElement lastElement, boolean hasSemicolon) {
    }
}

