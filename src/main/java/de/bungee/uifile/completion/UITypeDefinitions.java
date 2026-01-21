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
        UI_TYPES.put("Container", groupProps);
        UI_TYPES.put("DecoratedContainer", groupProps);
        UI_TYPES.put("SectionContainer", groupProps);
        UI_TYPES.put("Row", groupProps);
        UI_TYPES.put("RowHintContainer", groupProps);
        UI_TYPES.put("RowLabelContainer", groupProps);
        UI_TYPES.put("Content", groupProps);
        UI_TYPES.put("Wrapper", groupProps);

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
        UI_TYPES.put("TitleLabel", labelProps);
        UI_TYPES.put("CenteredTitleLabel", labelProps);
        UI_TYPES.put("Title", labelProps);
        UI_TYPES.put("PanelTitle", labelProps);
        UI_TYPES.put("RowLabel", labelProps);
        UI_TYPES.put("StatNameLabel", labelProps);
        UI_TYPES.put("StatNameValueLabel", labelProps);
        UI_TYPES.put("HotkeyLabel", labelProps);
        UI_TYPES.put("LabelAffix", labelProps);

        // Button component
        List<PropertyInfo> buttonProps = new ArrayList<>(commonProps);
        buttonProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Text on the button", "string"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("FlexWeight", "Flex weight for layout", "number"),
            new PropertyInfo("HoverBackground", "Background color on hover", "color"),
            new PropertyInfo("PressedBackground", "Background color when pressed", "color"),
            new PropertyInfo("DisabledBackground", "Background color when disabled", "color"),
            new PropertyInfo("Sounds", "Button sound effects", "sound config"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("Button", buttonProps);
        UI_TYPES.put("ActionButton", buttonProps);
        UI_TYPES.put("BackButton", buttonProps);
        UI_TYPES.put("ColumnButton", buttonProps);
        UI_TYPES.put("PrimaryButton", buttonProps);
        UI_TYPES.put("SecondaryButton", buttonProps);
        UI_TYPES.put("ToggleButton", buttonProps);
        UI_TYPES.put("ToolButton", buttonProps);
        UI_TYPES.put("TabButton", buttonProps);

        // TextButton component
        List<PropertyInfo> textButtonProps = new ArrayList<>(commonProps);
        textButtonProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Text on the button", "string"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("FlexWeight", "Flex weight for layout", "number"),
            new PropertyInfo("Sounds", "Button sound effects", "sound config"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("TextButton", textButtonProps);
        UI_TYPES.put("PrimaryTextButton", textButtonProps);
        UI_TYPES.put("SecondaryTextButton", textButtonProps);
        UI_TYPES.put("TertiaryTextButton", textButtonProps);
        UI_TYPES.put("DestructiveTextButton", textButtonProps);
        UI_TYPES.put("SmallSecondaryTextButton", textButtonProps);
        UI_TYPES.put("TagTextButton", textButtonProps);

        // TextField / TextInput component
        List<PropertyInfo> textFieldProps = new ArrayList<>(commonProps);
        textFieldProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Default text value", "string"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("PlaceholderText", "Placeholder text", "string"),
            new PropertyInfo("PlaceholderStyle", "Style for placeholder text", "style reference"),
            new PropertyInfo("MaxLength", "Maximum text length", "number"),
            new PropertyInfo("MaxVisibleLines", "Maximum visible lines", "number"),
            new PropertyInfo("ReadOnly", "Whether the field is read-only", "boolean"),
            new PropertyInfo("Password", "Password input mode", "boolean"),
            new PropertyInfo("PasswordChar", "Character to display for password", "string"),
            new PropertyInfo("AutoGrow", "Automatically grow height with content", "boolean"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("TextField", textFieldProps);
        UI_TYPES.put("CompactTextField", textFieldProps);
        UI_TYPES.put("MultilineTextField", textFieldProps);

        // Image component
        List<PropertyInfo> imageProps = new ArrayList<>(commonPropsNoStyle);
        imageProps.addAll(Arrays.asList(
            new PropertyInfo("TexturePath", "Image source path", "string"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("MaskTexturePath", "Mask texture path", "string"),
            new PropertyInfo("Color", "Image tint color", "color"),
            new PropertyInfo("Frame", "Frame index for sprite sheets", "number"),
            new PropertyInfo("FramesPerSecond", "Animation speed for sprites", "number")
        ));
        UI_TYPES.put("Image", imageProps);
        UI_TYPES.put("BackgroundImage", imageProps);
        UI_TYPES.put("AssetImage", imageProps);
        UI_TYPES.put("Icon", imageProps);
        UI_TYPES.put("Sprite", imageProps);

        // CheckBox component
        List<PropertyInfo> checkBoxProps = new ArrayList<>(commonProps);
        checkBoxProps.addAll(Arrays.asList(
            new PropertyInfo("Text", "Label text", "string"),
            new PropertyInfo("Checked", "Whether checked by default", "boolean"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("CheckedStyle", "Style when checked", "style reference"),
            new PropertyInfo("UncheckedStyle", "Style when unchecked", "style reference"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("CheckBox", checkBoxProps);
        UI_TYPES.put("LabeledCheckBox", checkBoxProps);

        // Slider component
        List<PropertyInfo> sliderProps = new ArrayList<>(commonProps);
        sliderProps.addAll(Arrays.asList(
            new PropertyInfo("Value", "Current value", "number"),
            new PropertyInfo("MinValue", "Minimum value", "number"),
            new PropertyInfo("MaxValue", "Maximum value", "number"),
            new PropertyInfo("Step", "Step increment", "number"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("SliderStyle", "Custom slider styling", "style reference"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("Slider", sliderProps);
        UI_TYPES.put("FloatSlider", sliderProps);

        // NumberField component
        List<PropertyInfo> numberFieldProps = new ArrayList<>(commonProps);
        numberFieldProps.addAll(Arrays.asList(
            new PropertyInfo("Value", "Default number value", "number"),
            new PropertyInfo("MinValue", "Minimum value", "number"),
            new PropertyInfo("MaxValue", "Maximum value", "number"),
            new PropertyInfo("Step", "Step increment", "number"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("Format", "Number format string", "string"),
            new PropertyInfo("MaxDecimalPlaces", "Maximum decimal places", "number"),
            new PropertyInfo("NumberFieldStyle", "Style for number field", "style reference"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("NumberField", numberFieldProps);
        UI_TYPES.put("SliderNumberField", numberFieldProps);
        UI_TYPES.put("FloatSliderNumberField", numberFieldProps);

        // Panel component
        List<PropertyInfo> panelProps = new ArrayList<>(commonProps);
        panelProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("LayoutMode", "Layout mode for children", "layout value"),
            new PropertyInfo("Spacing", "Spacing between children", "number"),
            new PropertyInfo("ScrollbarStyle", "Scrollbar styling", "style reference"),
            new PropertyInfo("ScrollEnabled", "Enable scrolling", "boolean")
        ));
        UI_TYPES.put("Panel", panelProps);

        // ItemIcon component
        List<PropertyInfo> itemIconProps = new ArrayList<>(commonPropsNoStyle);
        itemIconProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("IconWidth", "Width of the icon", "number"),
            new PropertyInfo("IconHeight", "Height of the icon", "number"),
            new PropertyInfo("IconTexturePath", "Path to icon texture", "string")
        ));
        UI_TYPES.put("ItemIcon", itemIconProps);

        // ItemSlot & ItemSlotButton component
        List<PropertyInfo> itemSlotProps = new ArrayList<>(commonPropsNoStyle);
        itemSlotProps.addAll(Arrays.asList(
            new PropertyInfo("SlotSize", "Size of the slot", "number"),
            new PropertyInfo("SlotIconSize", "Size of the icon within slot", "number"),
            new PropertyInfo("SlotBackground", "Background texture/color", "color"),
            new PropertyInfo("SlotHoverOverlay", "Overlay when hovering", "texture"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("DurabilityBar", "Show durability bar", "boolean"),
            new PropertyInfo("DurabilityBarAnchor", "Anchor for durability bar", "anchor value"),
            new PropertyInfo("OnClick", "Click event handler", "event handler")
        ));
        UI_TYPES.put("ItemSlot", itemSlotProps);
        UI_TYPES.put("ItemSlotButton", itemSlotProps);

        // ItemGrid component
        List<PropertyInfo> itemGridProps = new ArrayList<>(commonPropsNoStyle);
        itemGridProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("SlotsPerRow", "Number of slots per row", "number"),
            new PropertyInfo("Capacity", "Total capacity", "number"),
            new PropertyInfo("SlotSize", "Size of each slot", "number"),
            new PropertyInfo("SlotIconSize", "Size of icons in slots", "number"),
            new PropertyInfo("SlotSpacing", "Spacing between slots", "number"),
            new PropertyInfo("SlotBackground", "Background for slots", "color"),
            new PropertyInfo("ItemGridStyle", "Custom item grid styling", "style reference"),
            new PropertyInfo("RenderItemQualityBackground", "Render quality backgrounds", "boolean"),
            new PropertyInfo("ItemScale", "Scale factor for items", "number"),
            new PropertyInfo("AreItemsDraggable", "Allow item dragging", "boolean")
        ));
        UI_TYPES.put("ItemGrid", itemGridProps);

        // DropdownBox component
        List<PropertyInfo> dropdownProps = new ArrayList<>(commonProps);
        dropdownProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("AllowUnselection", "Allow deselecting items", "boolean"),
            new PropertyInfo("MaxSelection", "Maximum number of selections", "number"),
            new PropertyInfo("EntryHeight", "Height of each entry", "number"),
            new PropertyInfo("EntryLabelStyle", "Style for entry labels", "style reference"),
            new PropertyInfo("SelectedEntryLabelStyle", "Style for selected entries", "style reference"),
            new PropertyInfo("PopupStyle", "Style for popup menu", "style reference"),
            new PropertyInfo("OnChange", "Change event handler", "event handler")
        ));
        UI_TYPES.put("DropdownBox", dropdownProps);
        UI_TYPES.put("ColorPickerDropdownBoxStyle", dropdownProps);
        UI_TYPES.put("FileDropdownBoxStyle", dropdownProps);

        // ProgressBar component
        List<PropertyInfo> progressBarProps = new ArrayList<>(commonPropsNoStyle);
        progressBarProps.addAll(Arrays.asList(
            new PropertyInfo("Value", "Current progress value (0-1)", "number"),
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("Bar", "Bar configuration", "style config"),
            new PropertyInfo("BarTexturePath", "Texture for progress bar", "string"),
            new PropertyInfo("Background", "Background configuration", "color")
        ));
        UI_TYPES.put("ProgressBar", progressBarProps);
        UI_TYPES.put("CircularProgressBar", progressBarProps);

        // Separator components
        List<PropertyInfo> separatorProps = new ArrayList<>(commonPropsNoStyle);
        separatorProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("Color", "Separator color", "color")
        ));
        UI_TYPES.put("Separator", separatorProps);
        UI_TYPES.put("Sep", separatorProps);
        UI_TYPES.put("VerticalSeparator", separatorProps);
        UI_TYPES.put("Divider", separatorProps);
        UI_TYPES.put("ContentSeparator", separatorProps);
        UI_TYPES.put("PanelSeparatorFancy", separatorProps);
        UI_TYPES.put("ActionButtonSeparator", separatorProps);
        UI_TYPES.put("VerticalActionButtonSeparator", separatorProps);

        // Tab Navigation component
        List<PropertyInfo> tabNavProps = new ArrayList<>(commonPropsNoStyle);
        tabNavProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("SelectedTab", "Index of selected tab", "number"),
            new PropertyInfo("TabStyle", "Style for tabs", "style reference"),
            new PropertyInfo("SelectedTabStyle", "Style for selected tab", "style reference"),
            new PropertyInfo("TabNavigationStyle", "Overall navigation styling", "style reference"),
            new PropertyInfo("TabSounds", "Sound effects for tabs", "sound config")
        ));
        UI_TYPES.put("TabNavigation", tabNavProps);
        UI_TYPES.put("Tab", tabNavProps);

        // BlockSelector component
        List<PropertyInfo> blockSelectorProps = new ArrayList<>(commonPropsNoStyle);
        blockSelectorProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("BlockSelectorStyle", "Custom block selector styling", "style reference")
        ));
        UI_TYPES.put("BlockSelector", blockSelectorProps);

        // Overlay components
        List<PropertyInfo> overlayProps = new ArrayList<>(commonPropsNoStyle);
        overlayProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("LayoutMode", "Layout mode for children", "layout value"),
            new PropertyInfo("Spacing", "Spacing between children", "number"),
            new PropertyInfo("CloseOnClickOutside", "Close when clicking outside", "boolean"),
            new PropertyInfo("Background", "Overlay background color", "color"),
            new PropertyInfo("OnClose", "Close event handler", "event handler")
        ));
        UI_TYPES.put("Overlay", overlayProps);
        UI_TYPES.put("PageOverlay", overlayProps);
        UI_TYPES.put("OfflineOverlay", overlayProps);
        UI_TYPES.put("SceneBlur", overlayProps);

        // Preview components
        List<PropertyInfo> previewProps = new ArrayList<>(commonPropsNoStyle);
        previewProps.addAll(Arrays.asList(
            new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"),
            new PropertyInfo("Scale", "Scale factor", "number")
        ));
        UI_TYPES.put("CharacterPreviewComponent", previewProps);
        UI_TYPES.put("PlayerPreviewComponent", previewProps);
        UI_TYPES.put("ItemPreviewComponent", previewProps);

        // Specialized components with basic properties
        List<PropertyInfo> specializedProps = new ArrayList<>(commonPropsNoStyle);
        specializedProps.add(new PropertyInfo("Anchor", "Anchor point and dimensions", "anchor value"));

        UI_TYPES.put("ActionButtonContainer", specializedProps);
        UI_TYPES.put("HeaderSearch", specializedProps);
        UI_TYPES.put("HotkeyRow", specializedProps);
        UI_TYPES.put("DoubleArrowKeyHotkeyRow", specializedProps);
        UI_TYPES.put("EditionCard", specializedProps);
        UI_TYPES.put("ReorderableListGrip", specializedProps);
        UI_TYPES.put("DefaultSpinner", specializedProps);
        UI_TYPES.put("MenuItem", specializedProps);
        UI_TYPES.put("Legend", specializedProps);
        UI_TYPES.put("Pages", specializedProps);
        UI_TYPES.put("Page", specializedProps);
        UI_TYPES.put("TabSeparator", specializedProps);
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

