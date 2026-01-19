# Changelog

All notable changes to the UI File Plugin will be documented
in this file.

The format is based
on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.4] - 2026-01-19

### Fixed

- **Plugin Configuration**: Fixed AutoCompletion contributor registration formatting in plugin.xml.

## [1.2.3] - 2026-01-19

### Fixed

- **Preview Rendering Improvements**: Enhanced stability and
  visual quality of the UI preview panel.
    - Fixed zoom controls (+, -, Reset) functionality with
      proper event handling.
    - Improved keyboard shortcuts for zoom operations
      (Ctrl+Plus, Ctrl+Minus, Ctrl+0).
    - Enhanced toolbar button styling with better sizing and
      hover effects.
    - Fixed zoom label positioning and alignment in the
      toolbar.
    - Improved component bounds calculation and layout
      handling.
    - Better rendering of nested components and groups.

## [1.2.2] - 2026-01-18

### Added

- **Interactive Color Picker**: Click on color gutter icons
  to open the color picker dialog.
    - Change colors visually with IntelliJ's native color
      picker.
    - Full support for alpha channel (transparency) in
      colors.
    - Intention action "Choose color" available via
      Alt+Enter on color values.
    - Real-time code updates when selecting new colors.
    - Enhanced tooltips showing RGB/RGBA values on hover.

### Improved

- **Preview System Enhancements**: Simplified and optimized
  the UI model and renderer structure.
    - Enhanced component layout handling for better
      rendering accuracy.
    - Improved code clarity by removing unnecessary comments
      and refactoring component logic.
    - Better style handling for UI components in preview
      panel.

## [1.2.1] - 2026-01-19

### Added

- **Code Reformatting Support**: Comprehensive support for
  the "Reformat Code" action (Ctrl+Alt+L / Cmd+Option+L) in
  `.ui` files.
    - Smart spacing rules for components, properties, and
      values.
    - Configurable indentation (default 2 spaces).
    - Idempotent formatting (multiple applications produce
      consistent results).
- **Hierarchical AST Parsing**: Re-implemented the parser to
  create a proper hierarchical tree structure for components
  and nested blocks.
    - Improves code folding accuracy.
    - Enables correct relative indentation for nested
      elements.

### Fixed

- **Indentation Issues**: Resolved several bugs related to
  incorrect or excessive indentation.
    - Fixed "staircase" effect in nested components.
    - Fixed double-spacing issues during reformatting.

## [1.2.0] - 2026-01-18

### Added

- **Live Preview Tool Window**: Added visual rendering of
  .ui files
- **Real-time Preview Updates**: Preview updates while
  typing in the editor
- **Professional UI Component Rendering**: Support for
  Group, Label, Button, and TextField components
- **Zoom Controls**: Added zoom in, zoom out, and reset to
  100% controls
- **Accurate Rendering**: Color and size rendering from .ui
  syntax
- **Preview Panel**: Positioned on the right side of the IDE
- **New UI File Action**: Added context menu action to
  create new .ui files
    - Right-click in Project view to create new .ui files
    - Automatic .ui extension handling
    - Dialog prompt for file name input
- **Plugin Settings**: Added settings page for UI File
  plugin
    - Toggle "New File" action on/off
    - Accessible via Settings → Tools → Hytale UI File

### Fixed

- **New File Action**: Fixed directory determination logic
    - Now correctly uses selected directory or parent
      directory
    - Falls back to project base directory when no selection
    - Improved error handling for invalid directory
      scenarios
- **Resource Bundle Integration**: Fixed
  internationalization support for action texts and
  descriptions

## [1.1.0] - 2026-01-15

### Added

- **IntelliSense/Code Completion**: Comprehensive
  auto-completion support for UI files
    - Component type suggestions (Group, Label, Button,
      TextField, Image, CheckBox, Slider, Panel, ScrollView)
    - Context-aware property suggestions based on the
      current component type
    - Property descriptions and type information in
      completion popup
    - Smart value templates for different property types (
      colors, strings, numbers, etc.)
    - Automatic cursor positioning after insertion
- **Enhanced Property Support**: Extended property
  definitions for all UI component types
    - Common properties available for all components
    - Specialized properties for each component type
    - Detailed documentation for each property

### Documentation

- Added comprehensive IntelliSense documentation (
  INTELLISENSE.md)
- Updated README with IntelliSense features and supported
  components
- Updated plugin description to highlight new completion
  features

## [1.0.1] - 2026-01-15

### Changed

- **Extended IDE Compatibility**: Now supports IntelliJ IDEA
  builds 251 through 253.*
- **Enhanced JDK Compatibility**: Plugin now builds
  correctly with JDK 17, 21, and 25
- **Build Configuration**: Added explicit Java toolchain
  configuration focusing on Java 25

### Fixed

- Fixed compatibility issue with IntelliJ IDEA 2025.3 (build
  253.*)
- Fixed build errors when using JDK 25

## [1.0.0] - 2026-01-15

### Added

- Initial release
- Syntax highlighting for `.ui` files
- Support for UI components (Group, Label, Button,
  TextInput)
- Property recognition (Anchor, Background, Text, Style,
  etc.)
- Color preview for hex color values
- Bracket matching for `{}` and `()`
- Comment support with `Ctrl+/` toggle
- File type registration for `.ui` extension

### Features

- **Components**: Group, Label, Button, TextInput
- **Properties**: Anchor, Background, Text, Style,
  LayoutMode, Padding, Placeholder, TextColor
- **Color Highlighting**: Automatic preview for hex colors (
  #RRGGBB format)
- **IDE Integration**: Works with IntelliJ IDEA 2025.1+

[1.2.1]: https://github.com/BungeeDEV/hytale-ui-plugin/releases/tag/v1.2.1

[1.2.0]: https://github.com/BungeeDEV/hytale-ui-plugin/releases/tag/v1.2.0

[1.0.1]: https://github.com/BungeeDEV/hytale-ui-plugin/releases/tag/v1.0.1

[1.0.0]: https://github.com/BungeeDEV/hytale-ui-plugin/releases/tag/v1.0.0

