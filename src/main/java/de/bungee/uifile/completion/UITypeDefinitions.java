package de.bungee.uifile.completion;

import java.util.*;

/**
 * Definitions of UI component types and their available properties
 */
public class UITypeDefinitions {

    public record PropertyInfo(String name, String description, String valueType, List<PropertyInfo> subProperties) {
        // Constructor for properties without sub-properties
        public PropertyInfo(String name, String description, String valueType) {
            this(name, description, valueType, Collections.emptyList());
        }

        public boolean hasSubProperties() {
            return subProperties != null && !subProperties.isEmpty();
        }
    }

    private static final Map<String, List<PropertyInfo>> UI_TYPES = new HashMap<>();

    // Style sub-properties for Label components
    private static final List<PropertyInfo> LABEL_STYLE_PROPERTIES = Arrays.asList(
        new PropertyInfo("FontSize", "Font size in pixels", "number"),
        new PropertyInfo("TextColor", "Text color (hex or @reference)", "color"),
        new PropertyInfo("RenderBold", "Render text in bold", "boolean"),
        new PropertyInfo("RenderUppercase", "Render text in uppercase", "boolean"),
        new PropertyInfo("RenderItalics", "Render text in italics", "boolean"),
        new PropertyInfo("HorizontalAlignment", "Horizontal text alignment (Start|Center|End)", "alignment"),
        new PropertyInfo("VerticalAlignment", "Vertical text alignment (Start|Center|End)", "alignment"),
        new PropertyInfo("Alignment", "Combined alignment (Center)", "alignment"),
        new PropertyInfo("FontName", "Font family name (Default|Secondary)", "string"),
        new PropertyInfo("Wrap", "Enable text wrapping", "boolean"),
        new PropertyInfo("LetterSpacing", "Letter spacing value", "number"),
        new PropertyInfo("OutlineColor", "Text outline color", "color")
    );

    static {
        // Common properties for all UI components (except those with restrictions)
        List<PropertyInfo> commonProps = Arrays.asList(
            new PropertyInfo("Background", "Background color of the component", "color"),
            new PropertyInfo("Style", "Styling properties (FontSize, TextColor, RenderBold, etc.)", "style block",
                LABEL_STYLE_PROPERTIES),
            new PropertyInfo("Padding", "Padding around the component", "padding value"),
            new PropertyInfo("Margin", "Margin around the component", "margin value"),
            new PropertyInfo("Width", "Width of the component", "number"),
            new PropertyInfo("Height", "Height of the component", "number"),
            new PropertyInfo("Visible", "Visibility of the component", "boolean"),
            new PropertyInfo("Enabled", "Whether the component is enabled", "boolean")
        );

        // Common properties without Style (for components like Group)
        List<PropertyInfo> commonPropsNoStyle = Arrays.asList(
            new PropertyInfo("Background", "Background color of the component", "color"),
            new PropertyInfo("Padding", "Padding around the component", "padding value"),
            new PropertyInfo("Margin", "Margin around the component", "margin value"),
            new PropertyInfo("Width", "Width of the component", "number"),
            new PropertyInfo("Height", "Height of the component", "number"),
            new PropertyInfo("Visible", "Visibility of the component", "boolean"),
            new PropertyInfo("Enabled", "Whether the component is enabled", "boolean")
        );

        // Group component - does NOT support Style attribute
        List<PropertyInfo> groupProps = new ArrayList<>(commonPropsNoStyle);
        groupProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions (Width, Height, Top, Bottom, Left, Right, MaxWidth, MinWidth, Full, Horizontal, Vertical)", "anchor value"),
            new PropertyInfo("Layout", "Layout type for children", "layout value"),
            new PropertyInfo("LayoutMode", "Layout mode for children (TopScrolling, MiddleCenter, Left, Right, Full, Middle, Bottom, BottomScrolling, CenterMiddle, Top, LeftCenterWrap, Center)", "layout value"),
            new PropertyInfo("Spacing", "Spacing between children", "number"),
            new PropertyInfo("FlexWeight", "Flex weight for layout distribution (1-5)", "number"),
            new PropertyInfo("ScrollbarStyle", "Scrollbar styling (Spacing, Size, OnlyVisibleWhenHovered, or reference)", "style reference"),
            new PropertyInfo("HitTestVisible", "Whether the element should receive mouse/touch input", "boolean"),
            new PropertyInfo("TextTooltipShowDelay", "Delay in seconds before showing text tooltip", "number"),
            new PropertyInfo("TextTooltipStyle", "Style configuration for text tooltips (MaxWidth, LabelStyle, etc.)", "style reference"),
            new PropertyInfo("TooltipText", "Localized tooltip text key (e.g., %client.inventory.crafting.ingredient.tooltip)", "string"),
            new PropertyInfo("AutoScrollDown", "Automatically scroll to bottom when content is added", "boolean"),
            new PropertyInfo("KeepScrollPosition", "Maintain scroll position when content changes", "boolean"),
            new PropertyInfo("MaskTexturePath", "Path to mask texture for clipping/masking effects", "string")
        ));
        UI_TYPES.put("Group", groupProps);

        // Label component
        // Style block supports: FontSize, TextColor, RenderBold, RenderUppercase, RenderItalics,
        // HorizontalAlignment, VerticalAlignment, Alignment, FontName, Wrap, LetterSpacing, OutlineColor
        List<PropertyInfo> labelProps = new ArrayList<>(commonProps);
        labelProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Text content of the label", "string"),
            new PropertyInfo("Anchor", "Anchor point and dimensions for positioning", "anchor value"),
            new PropertyInfo("FlexWeight", "Flex weight for layout distribution", "number"),
            new PropertyInfo("TextSpans", "Text spans for formatted text", "text spans"),
            new PropertyInfo("MaskTexturePath", "Path to mask texture for gradient effects", "string"),
            new PropertyInfo("TextTooltipStyle", "Style for text tooltips", "style reference"),
            new PropertyInfo("TooltipTextSpans", "Text spans for tooltip content", "text spans")
        ));
        UI_TYPES.put("Label", labelProps);

        // Button component
        List<PropertyInfo> buttonProps = new ArrayList<>(commonProps);
        buttonProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Text on the button", "string"),
            new PropertyInfo("TextColor", "Color of the button text", "color"),
            new PropertyInfo("HoverBackground", "Background color on hover", "color"),
            new PropertyInfo("PressedBackground", "Background color when pressed", "color"),
            new PropertyInfo("DisabledBackground", "Background color when disabled", "color"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("Button", buttonProps);

        // TextButton component - similar to Button but without LayoutMode
        List<PropertyInfo> textButtonProps = new ArrayList<>(commonProps);
        textButtonProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Text on the button", "string"),
            new PropertyInfo("TextColor", "Color of the button text", "color"),
            new PropertyInfo("HoverBackground", "Background color on hover", "color"),
            new PropertyInfo("PressedBackground", "Background color when pressed", "color"),
            new PropertyInfo("DisabledBackground", "Background color when disabled", "color"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("TextButton", textButtonProps);

        // TextField / TextInput component
        List<PropertyInfo> textFieldProps = new ArrayList<>(commonProps);
        textFieldProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Default text value", "string"),
            new PropertyInfo("PlaceholderText", "Placeholder text", "string"),
            new PropertyInfo("TextColor", "Color of the text", "color"),
            new PropertyInfo("PlaceholderColor", "Color of placeholder text", "color"),
            new PropertyInfo("MaxLength", "Maximum text length", "number"),
            new PropertyInfo("ReadOnly", "Whether the field is read-only", "boolean"),
            new PropertyInfo("Password", "Password input mode", "boolean"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("TextField", textFieldProps);
        UI_TYPES.put("TextInput", textFieldProps);

        // Image component
        List<PropertyInfo> imageProps = new ArrayList<>(commonProps);
        imageProps.addAll(Arrays.asList(
            new PropertyInfo("Source", "Image source path", "string"),
            new PropertyInfo("Stretch", "Image stretch mode", "stretch value"),
            new PropertyInfo("AspectRatio", "Maintain aspect ratio", "boolean"),
            new PropertyInfo("Tint", "Image tint color", "color")
        ));
        UI_TYPES.put("Image", imageProps);

        // CheckBox component
        List<PropertyInfo> checkBoxProps = new ArrayList<>(commonProps);
        checkBoxProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Label text", "string"),
            new PropertyInfo("Checked", "Whether checked by default", "boolean"),
            new PropertyInfo("TextColor", "Color of the text", "color"),
            new PropertyInfo("CheckColor", "Color of the check mark", "color"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("CheckBox", checkBoxProps);

        // Slider component
        List<PropertyInfo> sliderProps = new ArrayList<>(commonProps);
        sliderProps.addAll(Arrays.asList(
            new PropertyInfo("Value", "Current value", "number"),
            new PropertyInfo("MinValue", "Minimum value", "number"),
            new PropertyInfo("MaxValue", "Maximum value", "number"),
            new PropertyInfo("Step", "Step increment", "number"),
            new PropertyInfo("TrackColor", "Color of the track", "color"),
            new PropertyInfo("ThumbColor", "Color of the thumb", "color"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("Slider", sliderProps);

        // Panel component
        List<PropertyInfo> panelProps = new ArrayList<>(commonProps);
        panelProps.addAll(Arrays.asList(
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("ScrollEnabled", "Enable scrolling", "boolean")
        ));
        UI_TYPES.put("Panel", panelProps);

        // ScrollView component
        List<PropertyInfo> scrollViewProps = new ArrayList<>(commonProps);
        scrollViewProps.addAll(Arrays.asList(
            new PropertyInfo("ScrollbarVisible", "Show scrollbar", "boolean"),
            new PropertyInfo("ScrollbarColor", "Scrollbar color", "color"),
            new PropertyInfo("HorizontalScroll", "Enable horizontal scroll", "boolean"),
            new PropertyInfo("VerticalScroll", "Enable vertical scroll", "boolean")
        ));
        UI_TYPES.put("ScrollView", scrollViewProps);

        // Input component (similar to TextField)
        List<PropertyInfo> inputProps = new ArrayList<>(commonProps);
        inputProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Default text value", "string"),
            new PropertyInfo("PlaceholderText", "Placeholder text", "string"),
            new PropertyInfo("TextColor", "Color of the text", "color"),
            new PropertyInfo("PlaceholderColor", "Color of placeholder text", "color"),
            new PropertyInfo("MaxLength", "Maximum text length", "number"),
            new PropertyInfo("ReadOnly", "Whether the field is read-only", "boolean"),
            new PropertyInfo("Password", "Password input mode", "boolean"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("Input", inputProps);

        // Container component (similar to Panel but can have layout)
        List<PropertyInfo> containerProps = new ArrayList<>(commonProps);
        containerProps.addAll(Arrays.asList(
            new PropertyInfo("LayoutMode", "Layout mode for children", "layout value"),
            new PropertyInfo("Spacing", "Spacing between children", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("ScrollEnabled", "Enable scrolling", "boolean")
        ));
        UI_TYPES.put("Container", containerProps);

        // ItemIcon component
        List<PropertyInfo> itemIconProps = new ArrayList<>(commonProps);
        itemIconProps.addAll(Arrays.asList(
            new PropertyInfo("ItemId", "Item identifier", "string"),
            new PropertyInfo("IconSize", "Size of the icon", "number"),
            new PropertyInfo("Tint", "Icon tint color", "color")
        ));
        UI_TYPES.put("ItemIcon", itemIconProps);

        // ItemSlot component
        List<PropertyInfo> itemSlotProps = new ArrayList<>(commonProps);
        itemSlotProps.addAll(Arrays.asList(
            new PropertyInfo("ItemId", "Item identifier", "string"),
            new PropertyInfo("SlotSize", "Size of the slot", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("HighlightColor", "Highlight color", "color"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("ItemSlot", itemSlotProps);

        // ItemSlotButton component
        List<PropertyInfo> itemSlotButtonProps = new ArrayList<>(commonProps);
        itemSlotButtonProps.addAll(Arrays.asList(
            new PropertyInfo("ItemId", "Item identifier", "string"),
            new PropertyInfo("SlotSize", "Size of the slot", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("HoverBackground", "Background color on hover", "color"),
            new PropertyInfo("PressedBackground", "Background color when pressed", "color"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("ItemSlotButton", itemSlotButtonProps);

        // ItemGrid component (container for items with layout)
        List<PropertyInfo> itemGridProps = new ArrayList<>(commonProps);
        itemGridProps.addAll(Arrays.asList(
            new PropertyInfo("Columns", "Number of columns", "number"),
            new PropertyInfo("Rows", "Number of rows", "number"),
            new PropertyInfo("Spacing", "Spacing between items", "number"),
            new PropertyInfo("CellSize", "Size of each cell", "number")
        ));
        UI_TYPES.put("ItemGrid", itemGridProps);

        // FloatSlider component
        List<PropertyInfo> floatSliderProps = new ArrayList<>(commonProps);
        floatSliderProps.addAll(Arrays.asList(
            new PropertyInfo("Value", "Current value", "number"),
            new PropertyInfo("MinValue", "Minimum value", "number"),
            new PropertyInfo("MaxValue", "Maximum value", "number"),
            new PropertyInfo("Step", "Step increment", "number"),
            new PropertyInfo("Precision", "Decimal precision", "number"),
            new PropertyInfo("TrackColor", "Color of the track", "color"),
            new PropertyInfo("ThumbColor", "Color of the thumb", "color"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("FloatSlider", floatSliderProps);

        // DropdownBox component
        List<PropertyInfo> dropdownProps = new ArrayList<>(commonProps);
        dropdownProps.addAll(Arrays.asList(
            new PropertyInfo("Items", "List of items", "array"),
            new PropertyInfo("SelectedIndex", "Selected item index", "number"),
            new PropertyInfo("PlaceholderText", "Placeholder text", "string"),
            new PropertyInfo("TextColor", "Color of the text", "color"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("DropdownBackground", "Background of dropdown menu", "color"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("DropdownBox", dropdownProps);

        // NumberField component
        List<PropertyInfo> numberFieldProps = new ArrayList<>(commonProps);
        numberFieldProps.addAll(Arrays.asList(
            new PropertyInfo("Value", "Default number value", "number"),
            new PropertyInfo("PlaceholderText", "Placeholder text", "string"),
            new PropertyInfo("TextColor", "Color of the text", "color"),
            new PropertyInfo("PlaceholderColor", "Color of placeholder text", "color"),
            new PropertyInfo("MinValue", "Minimum value", "number"),
            new PropertyInfo("MaxValue", "Maximum value", "number"),
            new PropertyInfo("Step", "Step increment", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("NumberField", numberFieldProps);

        // DecoratedContainer component (container with layout support)
        List<PropertyInfo> decoratedContainerProps = new ArrayList<>(commonProps);
        decoratedContainerProps.addAll(Arrays.asList(
            new PropertyInfo("LayoutMode", "Layout mode for children", "layout value"),
            new PropertyInfo("Spacing", "Spacing between children", "number"),
            new PropertyInfo("BorderColor", "Border color", "color"),
            new PropertyInfo("BorderWidth", "Border width", "number"),
            new PropertyInfo("BorderRadius", "Border radius", "number"),
            new PropertyInfo("ScrollEnabled", "Enable scrolling", "boolean"),
            new PropertyInfo("ShadowColor", "Shadow color", "color"),
            new PropertyInfo("ShadowOffset", "Shadow offset", "number")
        ));
        UI_TYPES.put("DecoratedContainer", decoratedContainerProps);

        // PageOverlay component (full screen overlay with layout)
        List<PropertyInfo> pageOverlayProps = new ArrayList<>(commonProps);
        pageOverlayProps.addAll(Arrays.asList(
            new PropertyInfo("LayoutMode", "Layout mode for children", "layout value"),
            new PropertyInfo("Spacing", "Spacing between children", "number"),
            new PropertyInfo("CloseOnClickOutside", "Close when clicking outside", "boolean"),
            new PropertyInfo("OverlayColor", "Background overlay color", "color"),
            new PropertyInfo("OnClose", "Close event handler", "event handler")
        ));
        UI_TYPES.put("PageOverlay", pageOverlayProps);
    }

    /**
     * Get all available UI component types
     */
    public static Set<String> getUITypes() {
        return UI_TYPES.keySet();
    }

    /**
     * Get properties for a specific UI component type
     */
    public static List<PropertyInfo> getPropertiesForType(String type) {
        return UI_TYPES.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Get sub-properties for a specific property of a component type For example: getSubProperties("Label", "Style")
     * returns style options like FontSize, TextColor, etc.
     */
    public static List<PropertyInfo> getSubPropertiesForProperty(String componentType, String propertyName) {
        List<PropertyInfo> properties = getPropertiesForType(componentType);
        for (PropertyInfo prop : properties) {
            if (prop.name().equals(propertyName) && prop.hasSubProperties()) {
                return prop.subProperties();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Check if a type exists
     */
    public static boolean isValidType(String type) {
        return UI_TYPES.containsKey(type);
    }

    /**
     * Get all valid style properties (used for validation in Style blocks)
     */
    public static List<PropertyInfo> getStyleProperties() {
        return LABEL_STYLE_PROPERTIES;
    }
}

