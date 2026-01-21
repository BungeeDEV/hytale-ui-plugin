package de.bungee.uifile.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UILexer extends LexerBase {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int currentOffset;
    private IElementType currentTokenType;
    private int currentTokenEnd;

    // Token Types
    public static final UITokenType COMPONENT = new UITokenType("COMPONENT");
    public static final UITokenType PROPERTY = new UITokenType("PROPERTY");
    public static final UITokenType STRING = new UITokenType("STRING");
    public static final UITokenType NUMBER = new UITokenType("NUMBER");
    public static final UITokenType COLOR = new UITokenType("COLOR");
    public static final UITokenType IDENTIFIER = new UITokenType("IDENTIFIER");
    public static final UITokenType LBRACE = new UITokenType("LBRACE");
    public static final UITokenType RBRACE = new UITokenType("RBRACE");
    public static final UITokenType LPAREN = new UITokenType("LPAREN");
    public static final UITokenType RPAREN = new UITokenType("RPAREN");
    public static final UITokenType COLON = new UITokenType("COLON");
    public static final UITokenType SEMICOLON = new UITokenType("SEMICOLON");
    public static final UITokenType COMMA = new UITokenType("COMMA");
    public static final UITokenType HASH = new UITokenType("HASH");
    public static final UITokenType AT = new UITokenType("AT");
    public static final UITokenType EQUALS = new UITokenType("EQUALS");
    public static final UITokenType DOLLAR = new UITokenType("DOLLAR");
    public static final UITokenType DOT = new UITokenType("DOT");
    public static final UITokenType COMMENT = new UITokenType("COMMENT");
    public static final UITokenType WHITE_SPACE = new UITokenType("WHITE_SPACE");
    public static final UITokenType BAD_CHARACTER = new UITokenType("BAD_CHARACTER");

    // Known Components - vollständige Liste aus Hytale UI System
    private static final String[] COMPONENTS = {
        // Layout Components
        "Group", "Container", "Panel", "DecoratedContainer", "SectionContainer",
        "Row", "RowHintContainer", "RowLabelContainer", "VBox", "HBox", "Wrapper",
        "ScrollGroup", "Content",

        // Button Components
        "Button", "TextButton", "ActionButton", "BackButton", "ColumnButton",
        "PrimaryButton", "SecondaryButton", "ToggleButton", "ToolButton",
        "PrimaryTextButton", "SecondaryTextButton", "TertiaryTextButton",
        "DestructiveTextButton", "SmallSecondaryTextButton", "TagTextButton",
        "ItemSlotButton", "TabButton",

        // Text/Label Components
        "Label", "TitleLabel", "CenteredTitleLabel", "Title", "PanelTitle",
        "RowLabel", "StatNameLabel", "StatNameValueLabel", "HotkeyLabel", "LabelAffix",

        // Input Components
        "TextField", "CompactTextField", "MultilineTextField", "NumberField",
        "SliderNumberField", "FloatSliderNumberField",

        // Selection Components
        "CheckBox", "LabeledCheckBox", "DropdownBox", "BlockSelector",

        // Slider Components
        "Slider", "FloatSlider",

        // Item/Inventory Components
        "ItemIcon", "ItemSlot", "ItemGrid", "ItemPreviewComponent",
        "BlockSelector", "BlockSelectorStyle",

        // Image Components
        "Image", "BackgroundImage", "AssetImage", "Icon", "Sprite",

        // Progress Components
        "ProgressBar", "CircularProgressBar",

        // Tab/Navigation Components
        "TabNavigation", "Tab", "TabSeparator", "Pages", "Page",
        "MenuItem", "Legend",

        // Separators
        "Separator", "Sep", "VerticalSeparator", "Divider",
        "ContentSeparator", "PanelSeparatorFancy",
        "ActionButtonSeparator", "VerticalActionButtonSeparator",

        // Containers & Overlays
        "ActionButtonContainer", "PageOverlay", "Overlay", "OfflineOverlay",
        "PopupMenuLayerStyle", "SceneBlur",

        // Specialized Components
        "CharacterPreviewComponent", "PlayerPreviewComponent",
        "HeaderSearch", "HotkeyRow", "DoubleArrowKeyHotkeyRow",
        "EditionCard", "ReorderableListGrip", "DefaultSpinner",

        // Style Components
        "Style", "LabelStyle", "TextButtonStyle", "ButtonStyle", "CheckBoxStyle",
        "DropdownBoxStyle", "ColorPickerDropdownBoxStyle", "FileDropdownBoxStyle",
        "InputFieldStyle", "ItemGridStyle", "PatchStyle", "PopupMenuLayerStyle",
        "ScrollbarStyle", "SliderStyle", "TabNavigationStyle", "TextTooltipStyle",
        "ColorPickerStyle"
    };

    // Known Properties - vollständige Liste aus Hytale UI System
    private static final String[] PROPERTIES = {
        // Core Layout & Positioning
        "Anchor", "LayoutMode", "Layout", "Padding", "Margin", "FlexWeight",
        "Spacing", "Direction", "Alignment", "PanelAlign",

        // Size Properties
        "Width", "Height", "Full", "Size",
        "MaxWidth", "MinWidth", "MaxHeight", "MinHeight",
        "CollapsedWidth", "ExpandedWidth",

        // Directional Properties
        "Top", "Bottom", "Left", "Right",
        "Horizontal", "Vertical",
        "HorizontalPadding", "VerticalPadding",
        "HorizontalBorder", "VerticalBorder",

        // Alignment Properties
        "HorizontalAlignment", "VerticalAlignment",
        "Center", "Start", "End",

        // Visual/Background Properties
        "Background", "Border", "Color", "TextColor", "OutlineColor",
        "TexturePath", "MaskTexturePath", "IconTexturePath", "BarTexturePath",
        "LabelMaskTexturePath", "EffectTexturePath",
        "DefaultBackground", "HoveredBackground", "PressedBackground",
        "ButtonBackground", "ButtonFill", "SelectedEntryIconBackground",
        "SlotBackground", "OpacitySelectorBackground", "OverlayColor",

        // Visibility & Interaction
        "Visible", "HitTestVisible", "Enabled", "Disabled",
        "OnlyVisibleWhenHovered", "CloseOnClickOutside",

        // Text & Font Properties
        "Text", "PlaceholderText", "TooltipText", "PanelTitleText",
        "FontSize", "FontName", "LetterSpacing",
        "RenderBold", "RenderUppercase", "RenderItalics", "Wrap",
        "MaxLength", "MaxVisibleLines",
        "TextSpans", "TooltipTextSpans",

        // Style Properties & References
        "Style", "LabelStyle", "ButtonStyle", "CheckBoxStyle", "TabStyle",
        "ScrollbarStyle", "SliderStyle", "InputFieldStyle", "ItemGridStyle",
        "TextTooltipStyle", "TooltipStyle", "PopupStyle", "PlaceholderStyle",
        "PanelScrollbarStyle", "ColorPickerStyle",
        "Default", "Hovered", "Pressed", "Disabled", "Checked", "Unchecked",
        "HoveredLabelStyle", "PressedLabelStyle", "DefaultLabelStyle",
        "SelectedEntryLabelStyle", "EntryLabelStyle",
        "SelectedStyle", "SelectedButtonStyle", "CheckedStyle",
        "SelectedTab", "SelectedTabStyle",

        // Input & Form Properties
        "Value", "MinValue", "MaxValue", "Step",
        "Format", "MaxDecimalPlaces", "NumberFieldMaxDecimalPlaces",
        "ReadOnly", "IsReadOnly", "Password", "PasswordChar",
        "AutoGrow", "AutoScrollDown", "KeepScrollPosition",

        // Item/Inventory Properties
        "ShowQualityBackground", "RenderItemQualityBackground", "ShowQuantity",
        "SlotSize", "SlotIconSize", "SlotSpacing", "SlotsPerRow",
        "Capacity", "Count", "PerRow",
        "DefaultItemIcon", "SlotDeleteIcon", "SlotDropIcon",
        "SlotHoverOverlay", "BrokenSlotBackgroundOverlay", "BrokenSlotIconOverlay",
        "QuantityPopupSlotOverlay", "ItemScale", "AreItemsDraggable",
        "DurabilityBar", "DurabilityBarAnchor", "DurabilityBarBackground",

        // Slider & Progress Properties
        "Min", "Max", "Bar", "Handle",
        "DraggedHandle", "HoveredHandle",

        // Icon & Image Properties
        "Icon", "IconWidth", "IconHeight", "IconAnchor", "IconOpacity",
        "IconSelected", "IconTexturePath", "Image", "ImageUW",
        "Source", "Stretch", "AspectRatio", "Tint", "Scale",
        "Frame", "FramesPerSecond",

        // Dropdown & Selection Properties
        "Items", "SelectedIndex", "AllowUnselection", "MaxSelection",
        "EntryHeight", "EntryIconWidth", "EntryIconHeight",
        "DropdownBackground", "EntrySounds",

        // Scrollbar Properties
        "ScrollEnabled", "ScrollbarVisible", "ShowScrollbar",
        "HorizontalScroll", "VerticalScroll",

        // Sound Properties
        "Sounds", "TabSounds", "ChangedSound", "CollapseSound", "ExpandSound",
        "ItemStackActivateSound",
        "Volume", "MinPitch", "MaxPitch",

        // Tooltip Properties
        "TextTooltipShowDelay", "TextTooltipHideDelay",

        // Navigation & Tabs
        "Pages", "SelectedTab",

        // Padding/Margin Advanced
        "ContentPadding", "ButtonPadding",

        // Effect & Animation
        "EffectWidth", "EffectHeight", "EffectOffset",

        // Offset & Transform
        "Offset", "ShadowColor", "ShadowOffset",

        // Decoration & Border
        "Decoration", "TextFieldDecoration",
        "BorderColor", "BorderWidth", "BorderRadius",

        // Action & Events
        "ActionName", "Activate", "OnClick", "OnChange", "OnClose",

        // Input Binding
        "InputBindingKey", "InputBindingKeyPrefix", "KeyBindingLabel",
        "BindingLabelStyle",

        // Miscellaneous
        "Id", "ShowLabel", "InfoDisplay",
        "Anchor", "Frame", "MouseHover",
        "PanelWidth", "ReorderableListGrip",
        "NumberFieldContainerAnchor", "NumberFieldStyle",
        "ClearButtonStyle"
    };

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.currentOffset = startOffset;
        advance();
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return currentTokenType;
    }

    @Override
    public int getTokenStart() {
        return startOffset;
    }

    @Override
    public int getTokenEnd() {
        return currentTokenEnd;
    }

    @Override
    public void advance() {
        if (currentOffset >= endOffset) {
            currentTokenType = null;
            return;
        }

        startOffset = currentOffset;
        char c = buffer.charAt(currentOffset);

        // Whitespace
        if (Character.isWhitespace(c)) {
            currentTokenType = WHITE_SPACE;
            while (currentOffset < endOffset && Character.isWhitespace(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            currentTokenEnd = currentOffset;
            return;
        }

        // Comments (// style)
        if (c == '/' && currentOffset + 1 < endOffset && buffer.charAt(currentOffset + 1) == '/') {
            currentTokenType = COMMENT;
            while (currentOffset < endOffset && buffer.charAt(currentOffset) != '\n') {
                currentOffset++;
            }
            currentTokenEnd = currentOffset;
            return;
        }

        // Strings
        if (c == '"') {
            currentTokenType = STRING;
            currentOffset++;
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                currentOffset++;
                if (ch == '"') {
                    break;
                }
                if (ch == '\\' && currentOffset < endOffset) {
                    currentOffset++;
                }
            }
            currentTokenEnd = currentOffset;
            return;
        }

        // Hex colors
        if (c == '#') {
            currentTokenType = HASH;
            currentOffset++;
            int hexStart = currentOffset;
            while (currentOffset < endOffset && isHexDigit(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            if (currentOffset - hexStart >= 3) { // At least 3 hex digits
                currentTokenType = COLOR;
            }
            currentTokenEnd = currentOffset;
            return;
        }

        // Numbers (including negative numbers and decimals)
        if (Character.isDigit(c) || (c == '-' && currentOffset + 1 < endOffset && Character.isDigit(
            buffer.charAt(currentOffset + 1)))) {
            currentTokenType = NUMBER;
            if (c == '-') {
                currentOffset++;
            }
            while (currentOffset < endOffset && (Character.isDigit(buffer.charAt(currentOffset))
                                                 || buffer.charAt(currentOffset) == '.')) {
                currentOffset++;
            }
            currentTokenEnd = currentOffset;
            return;
        }

        // Single-character tokens
        switch (c) {
            case '{':
                currentTokenType = LBRACE;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case '}':
                currentTokenType = RBRACE;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case '(':
                currentTokenType = LPAREN;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case ')':
                currentTokenType = RPAREN;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case ':':
                currentTokenType = COLON;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case ';':
                currentTokenType = SEMICOLON;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case ',':
                currentTokenType = COMMA;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case '@':
                currentTokenType = AT;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case '=':
                currentTokenType = EQUALS;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case '$':
                currentTokenType = DOLLAR;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
            case '.':
                currentTokenType = DOT;
                currentOffset++;
                currentTokenEnd = currentOffset;
                return;
        }

        // Identifiers (Components and Properties)
        if (Character.isJavaIdentifierStart(c)) {
            StringBuilder identifier = new StringBuilder();
            while (currentOffset < endOffset && (Character.isJavaIdentifierPart(buffer.charAt(currentOffset))
                                                 || buffer.charAt(currentOffset) == '_')) {
                identifier.append(buffer.charAt(currentOffset));
                currentOffset++;
            }

            String word = identifier.toString();

            // Check if it's a component
            for (String comp : COMPONENTS) {
                if (comp.equals(word)) {
                    currentTokenType = COMPONENT;
                    currentTokenEnd = currentOffset;
                    return;
                }
            }

            // Check if it's a property
            for (String prop : PROPERTIES) {
                if (prop.equals(word)) {
                    currentTokenType = PROPERTY;
                    currentTokenEnd = currentOffset;
                    return;
                }
            }

            currentTokenType = IDENTIFIER;
            currentTokenEnd = currentOffset;
            return;
        }

        // Unknown character
        currentTokenType = BAD_CHARACTER;
        currentOffset++;
        currentTokenEnd = currentOffset;
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }
}