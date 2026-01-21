package de.bungee.uifile.preview;

import com.intellij.ui.JBColor;
import java.awt.*;
import java.util.*;
import java.util.List;

public class UIModelParser {
    private static final int MAX_RECURSION_DEPTH = 50;
    private static final int MAX_PARSING_TIME_MS = 5000;

    private static final Set<String> GROUP_TYPES = new HashSet<>(Arrays.asList(
        "Group", "Container", "Content", "DecoratedContainer", "Overlay", "Page",
        "Pages", "Panel", "SectionContainer", "Wrapper", "ActionButtonContainer",
        "Row", "RowHintContainer", "RowLabelContainer", "Title", "HeaderSearch", "Legend",
        "Window", "Dialog", "Popup", "Modal", "Frame", "View", "Screen",
        "ScrollView", "ScrollContainer", "Stack", "HStack", "VStack", "ZStack",
        "Grid", "GridRow", "GridColumn", "Box", "Card", "Section", "Flex", "FlexRow",
        "FlexColumn", "FlexBox", "Layout", "Main", "Body", "Header", "Footer", "Sidebar"
    ));

    private static final Set<String> LABEL_TYPES = new HashSet<>(Arrays.asList(
        "Label", "CenteredTitleLabel", "HotkeyLabel", "LabelAffix", "PanelTitle",
        "RowLabel", "StatNameLabel", "StatNameValueLabel", "TitleLabel", "Text",
        "TextLabel", "Heading", "Title", "Subtitle", "Caption", "Description"
    ));

    private static final Set<String> BUTTON_TYPES = new HashSet<>(Arrays.asList(
        "Button", "ActionButton", "BackButton", "ColumnButton", "DestructiveTextButton",
        "PrimaryButton", "PrimaryTextButton", "SecondaryButton", "SecondaryTextButton",
        "SmallSecondaryTextButton", "TabButton", "TagTextButton", "TertiaryTextButton",
        "ToggleButton", "ToolButton", "TextButton", "MenuItem", "IconButton", "LinkButton"
    ));

    private static final Set<String> TEXTFIELD_TYPES = new HashSet<>(Arrays.asList(
        "TextField", "NumberField", "MultilineTextField", "CompactTextField", "TextArea",
        "Input", "SearchField", "PasswordField"
    ));

    private static final Set<String> CHECKBOX_TYPES = new HashSet<>(Arrays.asList(
        "CheckBox", "CheckBoxWithLabel", "LabeledCheckBox", "Toggle", "Switch"
    ));

    private static final Set<String> IMAGE_TYPES = new HashSet<>(Arrays.asList(
        "Image", "AssetImage", "BackgroundImage", "Icon", "Sprite", "Texture",
        "Picture", "Photo", "Avatar"
    ));

    private static final Set<String> PROGRESSBAR_TYPES = new HashSet<>(Arrays.asList(
        "ProgressBar", "CircularProgressBar", "LoadingBar", "Slider", "RangeSlider"
    ));

    private static final Set<String> DIVIDER_TYPES = new HashSet<>(Arrays.asList(
        "Divider", "Separator", "ContentSeparator", "VerticalSeparator", "Sep",
        "ActionButtonSeparator", "VerticalActionButtonSeparator", "PanelSeparatorFancy",
        "HSeparator", "VSeparator", "HorizontalDivider", "VerticalDivider", "LineSeparator",
        "Line", "Rule", "HR"
    ));

    private String content;
    private int pos = 0;
    private final Map<String, Map<String, Object>> globalStyles = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private int componentIdCounter = 0;
    private int currentRecursionDepth = 0;
    private long parseStartTime = 0;
    private final Set<Integer> visitedPositions = new HashSet<>();

    private int[] getLineAndColumn(int position) {
        if (content == null || position < 0 || position >= content.length()) {
            return new int[]{0, 0};
        }

        int line = 0;
        int column = 0;

        for (int i = 0; i < position; i++) {
            if (content.charAt(i) == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }
        }

        return new int[]{line, column};
    }

    public static UIModel parse(String content) {
        UIModelParser parser = new UIModelParser();
        parser.content = content;
        return parser.doParse();
    }

    private UIModel doParse() {
        UIModel model = new UIModel();
        parseStartTime = System.currentTimeMillis();

        if (content == null || content.trim().isEmpty()) {
            return model;
        }

        parseGlobalSection(model);

        pos = 0;
        while (pos < content.length()) {
            skipWhitespaceAndComments();
            if (pos >= content.length()) {
                break;
            }

            if (peek("@") || peek("$import") || peek("$")) {
                skipToNextStatement();
                continue;
            }

            UIModel.Component component = parseComponent(model);
            if (component != null) {
                model.addComponent(component);
            } else {
                pos++;
            }
        }

        return model;
    }

    private void parseGlobalSection(UIModel model) {
        pos = 0;
        while (pos < content.length()) {
            skipWhitespaceAndComments();
            if (pos >= content.length()) {
                break;
            }

            if (peek("@") && !peek("@style") && !isPartOfProperty()) {
                if (!parseGlobalStyle(model)) {
                    break;
                }
                continue;
            }

            if (peek("$")) {
                if (!parseVariable(model)) {
                    break;
                }
                continue;
            }

            break;
        }
        pos = 0;
    }

    private boolean parseGlobalStyle(UIModel model) {
        int start = pos;
        consume("@");
        String styleName = readIdentifier();
        skipWhitespaceAndComments();

        if (consume("=")) {
            Object value = parseValue(model);

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> styleMap = (Map<String, Object>) value;
                globalStyles.put(styleName, styleMap);
                model.addStyle(styleName, styleMap);
            } else {
                variables.put(styleName, value);
                model.setVariable(styleName, value);
            }

            consume(";");
            return true;
        }
        pos = start;
        return false;
    }

    private boolean parseVariable(UIModel model) {
        consume("$");
        String varName = readIdentifier();
        skipWhitespaceAndComments();

        if (consume(".")) {
            skipToNextStatement();
            return true;
        } else if (consume("=")) {
            Object value = parseValue(model);
            variables.put(varName, value);
            model.setVariable(varName, value);
            consume(";");
            return true;
        }
        return false;
    }

    private boolean isPartOfProperty() {
        int checkPos = pos - 1;
        while (checkPos >= 0 && Character.isWhitespace(content.charAt(checkPos))) {
            checkPos--;
        }
        return checkPos >= 0 && (content.charAt(checkPos) == ':' || content.charAt(checkPos) == '=');
    }

    private UIModel.Component parseComponent(UIModel model) {
        skipWhitespaceAndComments();
        if (pos >= content.length()) {
            return null;
        }

        int startPos = pos;
        String fullType = readComponentType();
        if (fullType.isEmpty()) {
            return null;
        }

        String type = extractComponentType(fullType);
        skipWhitespaceAndComments();

        String id = null;
        if (consume("#")) {
            id = readIdentifier();
            skipWhitespaceAndComments();
        }

        if (!peek("{")) {
            pos = startPos;
            return null;
        }

        consume("{");
        Map<String, Object> properties = new HashMap<>();
        List<UIModel.Component> children = new ArrayList<>();
        parseComponentBody(properties, children, model);
        consume("}");

        UIModel.Component component = createComponent(type, properties, children, model);
        if (component != null) {
            component.setId(id != null ? id : "component_" + (componentIdCounter++));

            // Store source position for navigation
            int[] lineCol = getLineAndColumn(startPos);
            component.setSourcePosition(lineCol[0], lineCol[1]);
        }

        return component;
    }

    private String readComponentType() {
        int start = pos;
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '$' || c == '@' || c == '.') {
                pos++;
            } else {
                break;
            }
        }
        return content.substring(start, pos);
    }

    private String extractComponentType(String fullType) {
        if (fullType.contains(".@")) {
            int idx = fullType.indexOf(".@");
            return fullType.substring(idx + 2);
        } else if (fullType.contains(".")) {
            return fullType.substring(fullType.lastIndexOf('.') + 1);
        } else if (fullType.startsWith("@")) {
            return fullType.substring(1);
        }
        return fullType;
    }

    private void parseComponentBody(Map<String, Object> properties, List<UIModel.Component> children, UIModel model) {
        int maxIterations = 1000;
        int iterations = 0;
        int lastPos = -1;

        while (pos < content.length() && !peek("}")) {
            checkParsingLimits(iterations++, maxIterations, lastPos);
            lastPos = pos;

            skipWhitespaceAndComments();
            if (peek("}")) {
                break;
            }

            int propStart = pos;
            String identifier = peekIdentifier();
            if (identifier.isEmpty()) {
                pos++;
                continue;
            }

            int afterIdentifier = pos + identifier.length();
            skipWhitespaceAt(afterIdentifier);

            if (afterIdentifier < content.length() &&
                (content.charAt(afterIdentifier) == ':' || content.charAt(afterIdentifier) == '=')) {
                parseProperty(properties, identifier, model);
            } else {
                pos = propStart;
                UIModel.Component child = parseComponent(model);
                if (child != null) {
                    children.add(child);
                } else {
                    pos++;
                }
            }
        }
    }

    private void checkParsingLimits(int iterations, int maxIterations, int lastPos) {
        if (System.currentTimeMillis() - parseStartTime > MAX_PARSING_TIME_MS) {
            throw new RuntimeException("Parsing timeout in parseComponentBody");
        }
        if (iterations > maxIterations) {
            throw new RuntimeException("Too many iterations in parseComponentBody - possible infinite loop");
        }
        if (pos == lastPos) {
            pos++;
        }
    }

    private void parseProperty(Map<String, Object> properties, String identifier, UIModel model) {
        pos += identifier.length();
        skipWhitespaceAndComments();
        consume(":");
        consume("=");
        Object value = parseValue(model);
        properties.put(identifier, value);
        skipWhitespaceAndComments();
        consume(";");
    }

    private void skipWhitespaceAt(int position) {
        while (position < content.length() && Character.isWhitespace(content.charAt(position))) {
            position++;
        }
    }

    private String peekIdentifier() {
        int start = pos;
        int tempPos = pos;
        while (tempPos < content.length()) {
            char c = content.charAt(tempPos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                tempPos++;
            } else {
                break;
            }
        }
        return content.substring(start, tempPos);
    }

    private Object parseValue(UIModel model) {
        checkTimeout();
        checkRecursionDepth();
        checkInfiniteLoop();

        currentRecursionDepth++;
        try {
            return parseValueInternal(model);
        } finally {
            currentRecursionDepth--;
        }
    }

    private void checkTimeout() {
        if (System.currentTimeMillis() - parseStartTime > MAX_PARSING_TIME_MS) {
            throw new RuntimeException("Parsing timeout - exceeded " + MAX_PARSING_TIME_MS + "ms");
        }
    }

    private void checkRecursionDepth() {
        if (currentRecursionDepth >= MAX_RECURSION_DEPTH) {
            throw new RuntimeException("Maximum recursion depth exceeded: " + MAX_RECURSION_DEPTH);
        }
    }

    private void checkInfiniteLoop() {
        if (visitedPositions.contains(pos)) {
            int positionCount = 0;
            for (int visitedPos : visitedPositions) {
                if (visitedPos == pos) {
                    positionCount++;
                }
            }
            if (positionCount > 3) {
                pos++;
            }
        }
        visitedPositions.add(pos);
    }

    private Object parseValueInternal(UIModel model) {
        skipWhitespaceAndComments();

        if (peek("(")) {
            return parseMap(model);
        }
        if (peek("\"")) {
            return parseString();
        }
        if (peek("#")) {
            return parseColorValue();
        }
        if (peek("@")) {
            return parseStyleReference(model);
        }

        return parseToken(model);
    }

    private Object parseStyleReference(UIModel model) {
        consume("@");
        String styleRef = readIdentifier();

        Map<String, Object> style = model.getStyle(styleRef);
        if (style != null) {
            return style;
        }

        if (globalStyles.containsKey(styleRef)) {
            return globalStyles.get(styleRef);
        }

        return "@" + styleRef;
    }

    private Object parseToken(UIModel model) {
        String value = readValueToken();

        if (value.startsWith("$")) {
            String varName = value.substring(1);
            Object varValue = model.getVariable(varName);
            if (varValue != null) {
                return varValue;
            }
            if (variables.containsKey(varName)) {
                return variables.get(varName);
            }
        }

        try {
            if (value.contains(".")) {
                return Float.parseFloat(value);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            if ("true".equalsIgnoreCase(value)) {
                return true;
            }
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }
            return value;
        }
    }

    private Map<String, Object> parseMap(UIModel model) {
        Map<String, Object> map = new HashMap<>();
        consume("(");

        int maxIterations = 1000;
        int iterations = 0;
        int lastPos = -1;

        while (pos < content.length() && !peek(")")) {
            checkParsingLimits(iterations++, maxIterations, lastPos);
            lastPos = pos;

            skipWhitespaceAndComments();
            if (peek(")")) {
                break;
            }

            String key = readIdentifier();
            if (key.isEmpty()) {
                pos++;
                continue;
            }

            skipWhitespaceAndComments();

            if (consume(":") || consume("=")) {
                Object value = parseValue(model);
                map.put(key, value);
            }

            skipWhitespaceAndComments();
            if (!consume(",")) {
                consume(";");
            }
        }

        consume(")");
        return map;
    }

    private String parseString() {
        consume("\"");
        StringBuilder sb = new StringBuilder();
        while (pos < content.length() && content.charAt(pos) != '"') {
            if (content.charAt(pos) == '\\' && pos + 1 < content.length()) {
                pos++;
                char escaped = content.charAt(pos);
                switch (escaped) {
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    default:
                        sb.append(escaped);
                }
            } else {
                sb.append(content.charAt(pos));
            }
            pos++;
        }
        consume("\"");
        return sb.toString();
    }

    private Map<String, Object> parseColorValue() {
        Map<String, Object> colorMap = new HashMap<>();

        if (peek("$")) {
            consume("$");
            String varName = readIdentifier();
            Object varValue = variables.get(varName);

            if (varValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> varMap = (Map<String, Object>) varValue;
                return varMap;
            }

            pos -= varName.length() + 1;
        }

        String hex = readColor();
        colorMap.put("hex", hex);

        skipWhitespaceAndComments();

        if (peek("(")) {
            consume("(");
            skipWhitespaceAndComments();
            String alphaStr = readValueToken();
            consume(")");
            try {
                float alphaValue = Float.parseFloat(alphaStr);
                colorMap.put("alpha", Math.max(0f, Math.min(1f, alphaValue)));
            } catch (NumberFormatException e) {
                colorMap.put("alpha", 1.0f);
            }
        } else {
            colorMap.put("alpha", 1.0f);
        }

        return colorMap;
    }

    private UIModel.Component createComponent(String type, Map<String, Object> properties,
        List<UIModel.Component> children, UIModel model) {
        UIModel.Component component;

        if (GROUP_TYPES.contains(type)) {
            component = createGroup(properties, children);
        } else if (LABEL_TYPES.contains(type)) {
            component = createLabel(properties, model);
        } else if (BUTTON_TYPES.contains(type)) {
            component = createButton(properties, model);
        } else if (TEXTFIELD_TYPES.contains(type)) {
            component = createTextField(properties);
        } else if (CHECKBOX_TYPES.contains(type)) {
            component = createCheckBox(properties);
        } else if (IMAGE_TYPES.contains(type)) {
            component = createImage(properties);
        } else if (PROGRESSBAR_TYPES.contains(type)) {
            component = createProgressBar(properties);
        } else if (DIVIDER_TYPES.contains(type)) {
            component = createDivider(type, properties);
        } else {
            component = createGroup(properties, children);
        }

        applyCommonProperties(component, properties, model);
        return component;
    }

    private UIModel.GroupComponent createGroup(Map<String, Object> properties, List<UIModel.Component> children) {
        UIModel.GroupComponent group = new UIModel.GroupComponent();

        Object layoutMode = getProperty(properties, "LayoutMode", "Layout");
        if (layoutMode != null) {
            group.setLayoutMode(layoutMode.toString());
        }

        Object gap = getProperty(properties, "Gap", "Spacing");
        if (gap instanceof Number) {
            group.setGap(((Number) gap).intValue());
        }

        Object wrapContent = properties.get("WrapContent");
        if (wrapContent instanceof Boolean) {
            group.setWrapContent((Boolean) wrapContent);
        }

        Object contentAlign = getProperty(properties, "ContentAlignment", "Alignment");
        if (contentAlign != null) {
            group.setContentAlignment(contentAlign.toString());
        }

        for (UIModel.Component child : children) {
            group.addChild(child);
        }

        return group;
    }

    private Object getProperty(Map<String, Object> properties, String... keys) {
        for (String key : keys) {
            Object value = properties.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private UIModel.LabelComponent createLabel(Map<String, Object> properties, UIModel model) {
        UIModel.LabelComponent label = new UIModel.LabelComponent();

        // Text
        Object text = properties.get("Text");
        if (text != null) {
            String textStr = resolveLocalizedString(text.toString());
            label.setText(textStr);
        }

        // Style - can be inline or reference
        Object style = properties.get("Style");
        if (style != null) {
            applyLabelStyle(label, style, model);
        }

        return label;
    }

    private void applyLabelStyle(UIModel.LabelComponent label, Object styleValue, UIModel model) {
        Map<String, Object> styleMap = resolveStyle(styleValue, model);
        if (styleMap == null) {
            return;
        }

        // FontSize
        Object fontSize = styleMap.get("FontSize");
        if (fontSize instanceof Number) {
            label.setFontSize(((Number) fontSize).intValue());
        }

        // TextColor
        Object textColor = styleMap.get("TextColor");
        if (textColor instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> colorMap = (Map<String, Object>) textColor;
            label.setTextColor(parseColorMap(colorMap));
        }

        // RenderBold
        Object bold = styleMap.get("RenderBold");
        if (bold instanceof Boolean) {
            label.setBold((Boolean) bold);
        }

        // RenderUppercase
        Object uppercase = styleMap.get("RenderUppercase");
        if (uppercase instanceof Boolean) {
            label.setUppercase((Boolean) uppercase);
        }

        // LetterSpacing
        Object letterSpacing = styleMap.get("LetterSpacing");
        if (letterSpacing instanceof Number) {
            label.setLetterSpacing(((Number) letterSpacing).floatValue());
        }

        // HorizontalAlignment or Alignment
        Object hAlign = styleMap.get("HorizontalAlignment");
        if (hAlign == null) {
            hAlign = styleMap.get("Alignment");
        }
        if (hAlign == null) {
            hAlign = styleMap.get("TextAlign");
        }
        if (hAlign != null) {
            label.setHorizontalAlignment(hAlign.toString());
        }

        // VerticalAlignment
        Object vAlign = styleMap.get("VerticalAlignment");
        if (vAlign != null) {
            label.setVerticalAlignment(vAlign.toString());
        }
    }

    private UIModel.ButtonComponent createButton(Map<String, Object> properties, UIModel model) {
        UIModel.ButtonComponent button = new UIModel.ButtonComponent();

        // Text property
        Object text = properties.get("Text");
        if (text != null) {
            button.setText(resolveLocalizedString(text.toString()));
        }

        // Icon
        if (properties.containsKey("Icon") || properties.containsKey("IconPath")) {
            button.setHasIcon(true);
        }

        // Style - can be inline or reference
        Object style = properties.get("Style");
        if (style != null) {
            Map<String, Object> styleMap = resolveStyle(style, model);
            if (styleMap != null) {
                applyButtonStyle(button, styleMap);

                // Also extract text from style if not set directly
                if (button.getText().isEmpty()) {
                    Object styleText = styleMap.get("Text");
                    if (styleText != null) {
                        button.setText(resolveLocalizedString(styleText.toString()));
                    }
                }
            }
        }

        // Label property - sometimes buttons have a separate Label child component
        Object label = properties.get("Label");
        if (label instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> labelMap = (Map<String, Object>) label;

            // Extract text from label
            Object labelText = labelMap.get("Text");
            if (labelText != null) {
                button.setText(resolveLocalizedString(labelText.toString()));
            }

            // Extract label style
            Object labelStyle = labelMap.get("Style");
            if (labelStyle != null) {
                Map<String, Object> labelStyleMap = resolveStyle(labelStyle, model);
                if (labelStyleMap != null) {
                    UIModel.LabelComponent lblStyle = new UIModel.LabelComponent();
                    applyLabelStyleToComponent(lblStyle, labelStyleMap);
                    button.setLabelStyle(lblStyle);
                }
            }
        }

        return button;
    }

    private void applyButtonStyle(UIModel.ButtonComponent button, Map<String, Object> styleMap) {
        // resolveStyle already extracts Default state if present, so we can use styleMap directly

        // Background
        Object bg = styleMap.get("Background");
        if (bg instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bgMap = (Map<String, Object>) bg;
            button.setBackground(parseColorMap(bgMap));
        }

        // LabelStyle for button text
        Object labelStyle = styleMap.get("LabelStyle");
        if (labelStyle instanceof Map) {
            UIModel.LabelComponent lblStyle = new UIModel.LabelComponent();
            @SuppressWarnings("unchecked")
            Map<String, Object> labelStyleMap = (Map<String, Object>) labelStyle;
            applyLabelStyleToComponent(lblStyle, labelStyleMap);
            button.setLabelStyle(lblStyle);
        }

        // BorderRadius for rounded buttons
        Object borderRadius = styleMap.get("BorderRadius");
        if (borderRadius instanceof Number) {
            button.setBorderRadius(((Number) borderRadius).intValue());
        }

        // Border for outlined buttons
        Object border = styleMap.get("Border");
        if (border instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> borderMap = (Map<String, Object>) border;

            Color borderColor = null;
            int borderWidth = 1;

            Object color = borderMap.get("Color");
            if (color instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> colorMap = (Map<String, Object>) color;
                borderColor = parseColorMap(colorMap);
            }

            Object width = borderMap.get("Width");
            if (width instanceof Number) {
                borderWidth = ((Number) width).intValue();
            }

            if (borderColor != null) {
                button.setBorder(borderColor, borderWidth);
            }
        }
    }

    private void applyLabelStyleToComponent(UIModel.LabelComponent label, Map<String, Object> styleMap) {
        Object fontSize = styleMap.get("FontSize");
        if (fontSize instanceof Number) {
            label.setFontSize(((Number) fontSize).intValue());
        }

        Object textColor = styleMap.get("TextColor");
        if (textColor instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> colorMap = (Map<String, Object>) textColor;
            label.setTextColor(parseColorMap(colorMap));
        }

        Object bold = styleMap.get("RenderBold");
        if (bold instanceof Boolean) {
            label.setBold((Boolean) bold);
        }
    }

    private UIModel.TextFieldComponent createTextField(Map<String, Object> properties) {
        UIModel.TextFieldComponent textField = new UIModel.TextFieldComponent();

        Object placeholder = getProperty(properties, "PlaceholderText", "Placeholder");
        if (placeholder != null) {
            textField.setPlaceholder(placeholder.toString());
        }

        return textField;
    }

    private UIModel.CheckBoxComponent createCheckBox(Map<String, Object> properties) {
        UIModel.CheckBoxComponent checkBox = new UIModel.CheckBoxComponent();

        Object text = getProperty(properties, "Text", "Label");
        if (text != null) {
            checkBox.setText(resolveLocalizedString(text.toString()));
        }

        Object checked = getProperty(properties, "Checked", "Selected");
        if (checked instanceof Boolean) {
            checkBox.setChecked((Boolean) checked);
        }

        return checkBox;
    }

    private UIModel.ImageComponent createImage(Map<String, Object> properties) {
        UIModel.ImageComponent image = new UIModel.ImageComponent();

        Object texturePath = getProperty(properties, "TexturePath", "Texture", "Image", "Icon", "Src", "Source");
        if (texturePath != null) {
            image.setTexturePath(texturePath.toString());
        }

        return image;
    }

    private UIModel.ProgressBarComponent createProgressBar(Map<String, Object> properties) {
        UIModel.ProgressBarComponent progressBar = new UIModel.ProgressBarComponent();

        Object value = getProperty(properties, "Value", "Progress");
        if (value instanceof Number) {
            progressBar.setValue(((Number) value).floatValue());
        }

        return progressBar;
    }

    private UIModel.DividerComponent createDivider(String type, Map<String, Object> properties) {
        UIModel.DividerComponent divider = new UIModel.DividerComponent();

        boolean vertical = type.toLowerCase().contains("vertical") || type.equals("VSeparator");

        Object orientation = properties.get("Orientation");
        if (orientation != null) {
            vertical = orientation.toString().equalsIgnoreCase("Vertical");
        }

        divider.setVertical(vertical);

        return divider;
    }

    private void applyCommonProperties(UIModel.Component component, Map<String, Object> properties, UIModel model) {
        applyDimensions(component, properties);
        applyAnchor(component, properties);
        applyFlexWeight(component, properties);
        applyBackground(component, properties);
        applyPadding(component, properties);
        applyMargin(component, properties);
        applyBorder(component, properties);
        applyBorderRadius(component, properties);
        applyOpacity(component, properties);
        applyVisibility(component, properties);
        applyMinMaxDimensions(component, properties);
        applyZIndex(component, properties);
        applyShadow(component, properties);
        applyAlignment(component, properties);
        applyStyleRef(component, properties);
    }

    private void applyDimensions(UIModel.Component component, Map<String, Object> properties) {
        Object width = properties.get("Width");
        Object height = properties.get("Height");
        if (width instanceof Number || height instanceof Number) {
            int w = width instanceof Number ? ((Number) width).intValue() : 0;
            int h = height instanceof Number ? ((Number) height).intValue() : 0;
            component.setDimensions(w, h);
        }
    }

    private void applyFlexWeight(UIModel.Component component, Map<String, Object> properties) {
        Object flexWeight = properties.get("FlexWeight");
        if (flexWeight instanceof Number) {
            component.setFlexWeight(((Number) flexWeight).floatValue());
        }
    }

    private void applyBackground(UIModel.Component component, Map<String, Object> properties) {
        Object background = properties.get("Background");
        if (background instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bgMap = (Map<String, Object>) background;

            if (bgMap.containsKey("Color")) {
                Object colorObj = bgMap.get("Color");
                if (colorObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> colorMap = (Map<String, Object>) colorObj;
                    component.setBackground(parseColorMap(colorMap));
                }
            } else {
                component.setBackground(parseColorMap(bgMap));
            }
        }
    }

    private void applyPadding(UIModel.Component component, Map<String, Object> properties) {
        Object padding = properties.get("Padding");
        if (padding != null) {
            applyBoxProperty(component, padding, true);
        }
    }

    private void applyMargin(UIModel.Component component, Map<String, Object> properties) {
        Object margin = properties.get("Margin");
        if (margin != null) {
            applyBoxProperty(component, margin, false);
        }
    }

    private void applyBorderRadius(UIModel.Component component, Map<String, Object> properties) {
        Object borderRadius = properties.get("BorderRadius");
        if (borderRadius instanceof Number) {
            component.setBorderRadius(((Number) borderRadius).intValue());
        }
    }

    private void applyOpacity(UIModel.Component component, Map<String, Object> properties) {
        Object opacity = getProperty(properties, "Opacity", "Alpha");
        if (opacity instanceof Number) {
            component.setOpacity(((Number) opacity).floatValue());
        }
    }

    private void applyVisibility(UIModel.Component component, Map<String, Object> properties) {
        Object visible = properties.get("Visible");
        if (visible instanceof Boolean) {
            component.setVisible((Boolean) visible);
        }
    }

    private void applyMinMaxDimensions(UIModel.Component component, Map<String, Object> properties) {
        Object minWidth = properties.get("MinWidth");
        if (minWidth instanceof Number) {
            component.setMinWidth(((Number) minWidth).intValue());
        }
        Object minHeight = properties.get("MinHeight");
        if (minHeight instanceof Number) {
            component.setMinHeight(((Number) minHeight).intValue());
        }
        Object maxWidth = properties.get("MaxWidth");
        if (maxWidth instanceof Number) {
            component.setMaxWidth(((Number) maxWidth).intValue());
        }
        Object maxHeight = properties.get("MaxHeight");
        if (maxHeight instanceof Number) {
            component.setMaxHeight(((Number) maxHeight).intValue());
        }
    }

    private void applyZIndex(UIModel.Component component, Map<String, Object> properties) {
        Object zIndex = properties.get("ZIndex");
        if (zIndex instanceof Number) {
            component.setZIndex(((Number) zIndex).intValue());
        }
    }

    private void applyAlignment(UIModel.Component component, Map<String, Object> properties) {
        Object hAlign = properties.get("HorizontalAlignment");
        if (hAlign != null) {
            component.setHorizontalAlignment(hAlign.toString());
        }
        Object vAlign = properties.get("VerticalAlignment");
        if (vAlign != null) {
            component.setVerticalAlignment(vAlign.toString());
        }
    }

    private void applyStyleRef(UIModel.Component component, Map<String, Object> properties) {
        Object styleRef = properties.get("Style");
        if (styleRef instanceof String && ((String) styleRef).startsWith("@")) {
            component.setStyleRef(((String) styleRef).substring(1));
        }
    }

    private void applyAnchor(UIModel.Component component, Map<String, Object> properties) {
        Object anchor = properties.get("Anchor");
        if (anchor instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> anchorMap = (Map<String, Object>) anchor;
            applyAnchorMap(component, anchorMap);
        }
    }

    private void applyBorder(UIModel.Component component, Map<String, Object> properties) {
        Object border = properties.get("Border");
        if (border instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> borderMap = (Map<String, Object>) border;

            Color borderColor = null;
            int borderWidth = 1;

            Object color = borderMap.get("Color");
            if (color instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> colorMap = (Map<String, Object>) color;
                borderColor = parseColorMap(colorMap);
            }

            Object width = borderMap.get("Width");
            if (width instanceof Number) {
                borderWidth = ((Number) width).intValue();
            }

            if (borderColor != null) {
                component.setBorder(borderColor, borderWidth);
            }
        }
    }

    private void applyShadow(UIModel.Component component, Map<String, Object> properties) {
        Object shadow = getProperty(properties, "Shadow", "DropShadow");
        if (shadow instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> shadowMap = (Map<String, Object>) shadow;

            Color shadowColor = null;
            int offsetX = 0;
            int offsetY = 2;
            int blur = 4;

            Object color = shadowMap.get("Color");
            if (color instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> colorMap = (Map<String, Object>) color;
                shadowColor = parseColorMap(colorMap);
            }

            Object xOffset = getProperty(shadowMap, "OffsetX", "X");
            if (xOffset instanceof Number) {
                offsetX = ((Number) xOffset).intValue();
            }

            Object yOffset = getProperty(shadowMap, "OffsetY", "Y");
            if (yOffset instanceof Number) {
                offsetY = ((Number) yOffset).intValue();
            }

            Object blurRadius = getProperty(shadowMap, "Blur", "BlurRadius");
            if (blurRadius instanceof Number) {
                blur = ((Number) blurRadius).intValue();
            }

            if (shadowColor != null) {
                component.setShadow(shadowColor, offsetX, offsetY, blur);
            }
        }
    }

    private void applyAnchorMap(UIModel.Component component, Map<String, Object> anchorMap) {
        UIModel.Anchor anchor = new UIModel.Anchor();

        Object width = anchorMap.get("Width");
        if (width instanceof Number) {
            anchor.width = ((Number) width).intValue();
        }
        Object height = anchorMap.get("Height");
        if (height instanceof Number) {
            anchor.height = ((Number) height).intValue();
        }
        Object full = anchorMap.get("Full");
        if (full instanceof Number) {
            anchor.full = ((Number) full).intValue();
        }

        Object flexWeight = anchorMap.get("FlexWeight");
        if (flexWeight instanceof Number) {
            anchor.flexWeight = ((Number) flexWeight).floatValue();
            component.setFlexWeight(anchor.flexWeight);
        }

        applyAnchorOffsets(anchor, anchorMap);
        applyAnchorFillOptions(anchor, anchorMap);
        applyAnchorAlignment(anchor, anchorMap);

        component.setAnchor(anchor);

        if (anchor.width != null || anchor.height != null) {
            component.setDimensions(
                anchor.width != null ? anchor.width : component.getPreferredWidth(),
                anchor.height != null ? anchor.height : component.getPreferredHeight()
            );
        }
    }

    private void applyAnchorOffsets(UIModel.Anchor anchor, Map<String, Object> anchorMap) {
        Object top = anchorMap.get("Top");
        if (top instanceof Number) {
            anchor.top = ((Number) top).intValue();
        }

        Object bottom = anchorMap.get("Bottom");
        if (bottom instanceof Number) {
            anchor.bottom = ((Number) bottom).intValue();
        }

        Object left = anchorMap.get("Left");
        if (left instanceof Number) {
            anchor.left = ((Number) left).intValue();
        }

        Object right = anchorMap.get("Right");
        if (right instanceof Number) {
            anchor.right = ((Number) right).intValue();
        }
    }

    private void applyAnchorFillOptions(UIModel.Anchor anchor, Map<String, Object> anchorMap) {
        Object fillHorizontal = anchorMap.get("FillHorizontal");
        if (fillHorizontal instanceof Boolean) {
            anchor.fillHorizontal = (Boolean) fillHorizontal;
        }

        Object fillVertical = anchorMap.get("FillVertical");
        if (fillVertical instanceof Boolean) {
            anchor.fillVertical = (Boolean) fillVertical;
        }

        Object fillParent = getProperty(anchorMap, "FillParent", "Fill");
        if (fillParent instanceof Boolean) {
            anchor.fillParent = (Boolean) fillParent;
        }
    }

    private void applyAnchorAlignment(UIModel.Anchor anchor, Map<String, Object> anchorMap) {
        Object hAlign = getProperty(anchorMap, "HorizontalAlignment", "Horizontal");
        if (hAlign != null) {
            anchor.horizontalAlignment = hAlign.toString();
        }

        Object vAlign = getProperty(anchorMap, "VerticalAlignment", "Vertical");
        if (vAlign != null) {
            anchor.verticalAlignment = vAlign.toString();
        }
    }

    private void applyBoxProperty(UIModel.Component component, Object value, boolean isPadding) {
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> boxMap = (Map<String, Object>) value;
            int[] values = extractBoxValues(boxMap);
            if (isPadding) {
                component.setPadding(values[0], values[1], values[2], values[3]);
            } else {
                component.setMargin(values[0], values[1], values[2], values[3]);
            }
        } else if (value instanceof Number) {
            int val = ((Number) value).intValue();
            if (isPadding) {
                component.setPadding(val, val, val, val);
            } else {
                component.setMargin(val, val, val, val);
            }
        }
    }

    private int[] extractBoxValues(Map<String, Object> map) {
        int full = getIntValue(map, "Full", 0);
        int horizontal = getIntValue(map, "Horizontal", full);
        int vertical = getIntValue(map, "Vertical", full);
        int top = getIntValue(map, "Top", vertical);
        int bottom = getIntValue(map, "Bottom", vertical);
        int left = getIntValue(map, "Left", horizontal);
        int right = getIntValue(map, "Right", horizontal);

        return new int[]{top, right, bottom, left};
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private Map<String, Object> resolveStyle(Object styleValue, UIModel model) {
        return resolveStyleInternal(styleValue, model, new HashSet<>());
    }

    private Map<String, Object> resolveStyleInternal(Object styleValue, UIModel model, Set<String> visitedStyles) {
        if (System.currentTimeMillis() - parseStartTime > MAX_PARSING_TIME_MS) {
            return null;
        }

        if (styleValue instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) styleValue;

            if (map.containsKey("Default") || map.containsKey("Hover") || map.containsKey("Active")) {
                Object defaultState = map.get("Default");
                if (defaultState instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> defaultMap = (Map<String, Object>) defaultState;
                    return mergeStyles(map, defaultMap);
                }
            }

            return map;
        } else if (styleValue instanceof String styleRef) {
            if (styleRef.startsWith("@")) {
                styleRef = styleRef.substring(1);
            }

            if (visitedStyles.contains(styleRef)) {
                return null;
            }
            visitedStyles.add(styleRef);

            Map<String, Object> style = model.getStyle(styleRef);
            if (style != null) {
                return resolveStyleInternal(style, model, visitedStyles);
            }

            Map<String, Object> globalStyle = globalStyles.get(styleRef);
            if (globalStyle != null) {
                return resolveStyleInternal(globalStyle, model, visitedStyles);
            }
        }
        return null;
    }

    private Map<String, Object> mergeStyles(Map<String, Object> parent, Map<String, Object> child) {
        Map<String, Object> merged = new HashMap<>(parent);
        merged.remove("Default");
        merged.remove("Hover");
        merged.remove("Active");
        merged.remove("Disabled");
        merged.remove("Selected");
        merged.putAll(child);
        return merged;
    }

    private String resolveLocalizedString(String text) {
        if (text.startsWith("%")) {
            int lastDot = text.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < text.length() - 1) {
                String key = text.substring(lastDot + 1);
                return key.substring(0, 1).toUpperCase() + key.substring(1);
            }
        }
        return text;
    }

    private Color parseColorMap(Map<String, Object> colorMap) {
        Object hex = colorMap.get("hex");
        if (hex == null) {
            hex = colorMap.get("Hex");
        }
        if (hex == null) {
            return JBColor.WHITE;
        }

        Object alpha = colorMap.get("alpha");
        if (alpha == null) {
            alpha = colorMap.get("Alpha");
        }

        float alphaVal = 1.0f;
        if (alpha instanceof Number) {
            alphaVal = ((Number) alpha).floatValue();
        }

        return parseColor(hex.toString(), alphaVal);
    }

    private Color parseColor(String hex, float alpha) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }

            int r, g, b;
            float a = alpha;

            if (hex.length() == 8) {
                // RRGGBBAA format - Hytale UI sometimes uses this
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
                int alphaInt = Integer.parseInt(hex.substring(6, 8), 16);
                a = alphaInt / 255.0f;
            } else if (hex.length() >= 6) {
                // RRGGBB format
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
            } else if (hex.length() == 3) {
                // RGB shorthand format: #FFF -> #FFFFFF
                r = Integer.parseInt(String.valueOf(hex.charAt(0)) + hex.charAt(0), 16);
                g = Integer.parseInt(String.valueOf(hex.charAt(1)) + hex.charAt(1), 16);
                b = Integer.parseInt(String.valueOf(hex.charAt(2)) + hex.charAt(2), 16);
            } else {
                return JBColor.WHITE;
            }

            int alphaInt = (int) (a * 255);
            return new JBColor(new Color(r, g, b, alphaInt), new Color(r, g, b, alphaInt));
        } catch (Exception e) {
            return JBColor.WHITE;
        }
    }

    // Helper methods
    private void skipWhitespaceAndComments() {
        while (pos < content.length()) {
            // Skip whitespace
            while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) {
                pos++;
            }

            // Skip single-line comments
            if (pos + 1 < content.length() && content.charAt(pos) == '/' && content.charAt(pos + 1) == '/') {
                while (pos < content.length() && content.charAt(pos) != '\n') {
                    pos++;
                }
                continue;
            }

            // Skip multi-line comments
            if (pos + 1 < content.length() && content.charAt(pos) == '/' && content.charAt(pos + 1) == '*') {
                pos += 2;
                while (pos + 1 < content.length()) {
                    if (content.charAt(pos) == '*' && content.charAt(pos + 1) == '/') {
                        pos += 2;
                        break;
                    }
                    pos++;
                }
                continue;
            }

            break;
        }
    }

    private void skipToNextStatement() {
        while (pos < content.length() && content.charAt(pos) != ';' && content.charAt(pos) != '\n') {
            pos++;
        }
        if (pos < content.length()) {
            pos++; // Skip the ; or \n
        }
    }

    private String readIdentifier() {
        skipWhitespaceAndComments();
        int start = pos;
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                pos++;
            } else {
                break;
            }
        }
        return content.substring(start, pos);
    }

    private String readValueToken() {
        skipWhitespaceAndComments();
        int start = pos;
        while (pos < content.length()) {
            char c = content.charAt(pos);
            if (Character.isWhitespace(c) || c == '(' || c == ')' || c == ';' ||
                c == ',' || c == ':' || c == '=' || c == '{' || c == '}') {
                break;
            }
            pos++;
        }
        return content.substring(start, pos);
    }

    private String readColor() {
        int start = pos;
        pos++; // skip #
        while (pos < content.length() && isHexChar(content.charAt(pos))) {
            pos++;
        }
        return content.substring(start, pos);
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean consume(String expected) {
        skipWhitespaceAndComments();
        if (pos + expected.length() <= content.length() && content.startsWith(expected, pos)) {
            pos += expected.length();
            return true;
        }
        return false;
    }

    private boolean peek(String expected) {
        int tempPos = pos;
        while (tempPos < content.length() && Character.isWhitespace(content.charAt(tempPos))) {
            tempPos++;
        }
        return tempPos + expected.length() <= content.length() && content.startsWith(expected, tempPos);
    }
}

