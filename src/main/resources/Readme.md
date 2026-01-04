# Resources

This directory contains configuration files, UI layouts, stylesheets, and other static resources used by the application.

---

## Contents

### UI Layouts

#### `layout.fxml`
Main JavaFX layout file defining the structure of the Railway Terminal Management System interface.

**Features**:
- Navigation tabs for different modules
- Modern card-based button layout
- Responsive design
- Dark theme styling

**Modules**:
- Cargo Handling
- Spatial and Indexed Queries
- Railway Network Optimization
- Traffic and Freight Manager
- Settings

### Stylesheets

#### `styles.css`
Modern CSS styling for JavaFX components with a professional dark theme.

**Features**:
- Indigo/purple color scheme
- Smooth transitions and hover effects
- Professional typography
- Responsive button cards
- Modern tab styling

**Key Styles**:
- `.root`: Dark background (#0D1117)
- `.title-main`: Large gradient titles
- `.button-card`: Modern card buttons with hover effects
- `.modern-button`: Styled action buttons
- `.tab-pane`: Professional tab navigation
- `.content-area`: Content container styling

**Color Palette**:
- Primary: #6366F1 (Indigo)
- Background: #0D1117 (Dark)
- Surface: #161B22 (Dark surface)
- Text: #F1F5F9 (Light text)
- Accent: #A78BFA (Purple)

---

## Usage

### Loading Resources in Java

```java
// Load FXML layout
FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout.fxml"));
Parent root = loader.load();

// Load CSS stylesheet
scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
```

### FXML Layout

The `layout.fxml` file defines the main application structure:
- Header with application title
- Navigation tabs
- Module-specific content areas
- Footer with version information

### CSS Styling

The `styles.css` file provides:
- Consistent theming across the application
- Modern UI components
- Responsive design elements
- Professional appearance

---

## Customization

### Modifying Styles

Edit `styles.css` to customize:
- Color scheme
- Font sizes and families
- Spacing and padding
- Animation effects
- Component appearances

### Modifying Layout

Edit `layout.fxml` to customize:
- Navigation structure
- Module organization
- Button placement
- Content areas

---

## Best Practices

1. **Resource Loading**: Always use `getClass().getResource()` for loading resources
2. **CSS Classes**: Use semantic class names that describe purpose
3. **FXML Structure**: Keep FXML files organized and maintainable
4. **Theme Consistency**: Maintain consistent styling across all components

---

## Related Documentation

- [Main README](../../README.md)
- [Java Source Code](../java/Readme.md)
- [Test Documentation](../../test/Readme.md)

---

## Notes

- Resources are automatically included in the JAR file during Maven build
- CSS files support JavaFX-specific properties (prefixed with `-fx-`)
- FXML files support JavaFX component binding and event handling
