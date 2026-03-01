import XCTest
@testable import FlexUI

/// Comprehensive tests for FlexUI parsing, theme resolution, and condition evaluation
final class FlexUITests: XCTestCase {
    
    // MARK: - Setup & Teardown
    
    override func setUp() {
        super.setUp()
        FlexUI.clearCache()
    }
    
    override func tearDown() {
        super.tearDown()
        FlexUI.clearCache()
    }
    
    // MARK: - Parsing Tests
    
    func testParseValidConfig() {
        let jsonString = """
        {
            "version": "1.0",
            "screenId": "test_screen",
            "root": {
                "type": "container",
                "style": {
                    "padding": 16,
                    "backgroundColor": "#FFFFFF"
                },
                "children": [
                    {
                        "type": "text",
                        "props": {
                            "content": "Hello, World!"
                        }
                    }
                ]
            }
        }
        """
        
        do {
            let config = try FlexParser.parseConfig(jsonString)
            XCTAssertEqual(config.version, "1.0")
            XCTAssertEqual(config.screenId, "test_screen")
            XCTAssertEqual(config.root.type, "container")
            XCTAssertEqual(config.root.children?.count, 1)
            XCTAssertEqual(config.root.children?.first?.type, "text")
        } catch {
            XCTFail("Failed to parse config: \\(error)")
        }
    }
    
    func testParseInvalidJSON() {
        let invalidJson = "{invalid json"
        
        do {
            _ = try FlexParser.parseConfig(invalidJson)
            XCTFail("Should have thrown parsing error")
        } catch let error as FlexError {
            switch error {
            case .parseError:
                // Expected
                break
            default:
                XCTFail("Wrong error type: \\(error)")
            }
        } catch {
            XCTFail("Unexpected error type: \\(error)")
        }
    }
    
    func testParseNodeWithAction() {
        let jsonString = """
        {
            "type": "button",
            "props": {
                "text": "Click me"
            },
            "action": {
                "type": "callback",
                "data": {
                    "event": "button_clicked"
                }
            }
        }
        """
        
        do {
            let node = try FlexParser.parseNode(jsonString)
            XCTAssertEqual(node.type, "button")
            XCTAssertNotNil(node.action)
            XCTAssertEqual(node.action?.type, "callback")
            XCTAssertEqual(node.action?.getString("event"), "button_clicked")
        } catch {
            XCTFail("Failed to parse node: \\(error)")
        }
    }
    
    func testParseNodeWithCondition() {
        let jsonString = """
        {
            "type": "container",
            "condition": {
                "if": "{{data.showContainer}}",
                "operator": "==",
                "value": true
            }
        }
        """
        
        do {
            let node = try FlexParser.parseNode(jsonString)
            XCTAssertNotNil(node.condition)
            XCTAssertEqual(node.condition?.condition, "{{data.showContainer}}")
            XCTAssertEqual(node.condition?.operator, "==")
        } catch {
            XCTFail("Failed to parse node with condition: \\(error)")
        }
    }
    
    // MARK: - Theme Resolution Tests
    
    func testThemeColorResolution() {
        let theme = FlexTheme(
            colors: [
                "primary": "#007AFF",
                "secondary": "#5856D6"
            ]
        )
        
        let resolver = ThemeResolver(theme: theme)
        
        // Test theme color lookup
        XCTAssertEqual(resolver.resolveColor("primary")?.hexString, "#007AFF")
        XCTAssertEqual(resolver.resolveColor("secondary")?.hexString, "#5856D6")
    }
    
    func testThemeDimensionResolution() {
        let theme = FlexTheme(
            dimensions: [
                "spacing_sm": 8,
                "spacing_md": 16,
                "spacing_lg": 24
            ]
        )
        
        let resolver = ThemeResolver(theme: theme)
        
        // Test dimension lookup
        XCTAssertEqual(resolver.resolveDimension("spacing_sm"), 8)
        XCTAssertEqual(resolver.resolveDimension("spacing_md"), 16)
        XCTAssertEqual(resolver.resolveDimension("spacing_lg"), 24)
    }
    
    func testVariableReplacement() {
        let theme = ThemeDefaults.defaultTheme
        let resolver = ThemeResolver(theme: theme)
        
        let input = "Color is {{colors.primary}}"
        let result = resolver.replaceVariables(input)
        
        XCTAssertNotEqual(result, input)
        XCTAssertTrue(result.contains("#"))
    }
    
    func testDataVariableReplacement() {
        let theme = ThemeDefaults.defaultTheme
        let resolver = ThemeResolver(theme: theme)
        
        let data: [String: Any] = [
            "user": [
                "name": "John Doe",
                "email": "john@example.com"
            ]
        ]
        
        let input = "Welcome, {{user.name}}!"
        let result = resolver.replaceVariables(input, with: data)
        
        XCTAssertEqual(result, "Welcome, John Doe!")
    }
    
    func testThemeFontResolution() {
        let fontDef = FontDefinition(family: "System", size: 16, weight: "bold")
        let theme = FlexTheme(
            fonts: [
                "heading": fontDef
            ]
        )
        
        let resolver = ThemeResolver(theme: theme)
        let font = resolver.resolveFont("heading", defaultSize: 14)
        
        XCTAssertNotNil(font)
        XCTAssertEqual(font?.pointSize, 16)
    }
    
    // MARK: - Condition Evaluation Tests
    
    func testSimpleBooleanCondition() {
        let condition = FlexCondition.boolean("{{data.isEnabled}}")
        let data: [String: Any] = ["isEnabled": true]
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertTrue(condition.evaluate(data: data, theme: theme))
    }
    
    func testEqualityCondition() {
        let condition = FlexCondition.equals("{{data.status}}", value: "active")
        
        let activeData: [String: Any] = ["status": "active"]
        let inactiveData: [String: Any] = ["status": "inactive"]
        
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertTrue(condition.evaluate(data: activeData, theme: theme))
        XCTAssertFalse(condition.evaluate(data: inactiveData, theme: theme))
    }
    
    func testGreaterThanCondition() {
        let condition = FlexCondition.greaterThan("{{data.count}}", value: 5)
        
        let lowData: [String: Any] = ["count": 3]
        let highData: [String: Any] = ["count": 10]
        
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertFalse(condition.evaluate(data: lowData, theme: theme))
        XCTAssertTrue(condition.evaluate(data: highData, theme: theme))
    }
    
    func testContainsCondition() {
        let condition = FlexCondition.contains("{{data.tags}}", value: "featured")
        
        let data: [String: Any] = ["tags": ["featured", "popular", "new"]]
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertTrue(condition.evaluate(data: data, theme: theme))
    }
    
    func testAndCondition() {
        let cond1 = FlexCondition.equals("{{data.status}}", value: "active")
        let cond2 = FlexCondition.greaterThan("{{data.level}}", value: 5)
        let andCondition = FlexCondition.and([cond1, cond2])
        
        let matchingData: [String: Any] = ["status": "active", "level": 10]
        let mismatchingData: [String: Any] = ["status": "inactive", "level": 10]
        
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertTrue(andCondition.evaluate(data: matchingData, theme: theme))
        XCTAssertFalse(andCondition.evaluate(data: mismatchingData, theme: theme))
    }
    
    func testOrCondition() {
        let cond1 = FlexCondition.equals("{{data.role}}", value: "admin")
        let cond2 = FlexCondition.equals("{{data.role}}", value: "moderator")
        let orCondition = FlexCondition.or([cond1, cond2])
        
        let adminData: [String: Any] = ["role": "admin"]
        let modData: [String: Any] = ["role": "moderator"]
        let userData: [String: Any] = ["role": "user"]
        
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertTrue(orCondition.evaluate(data: adminData, theme: theme))
        XCTAssertTrue(orCondition.evaluate(data: modData, theme: theme))
        XCTAssertFalse(orCondition.evaluate(data: userData, theme: theme))
    }
    
    // MARK: - Component Rendering Tests
    
    func testComponentRegistry() {
        let registry = ComponentRegistry.shared
        
        // Check built-in components are registered
        XCTAssertTrue(registry.isRegistered(type: "container"))
        XCTAssertTrue(registry.isRegistered(type: "text"))
        XCTAssertTrue(registry.isRegistered(type: "button"))
        XCTAssertTrue(registry.isRegistered(type: "row"))
        XCTAssertTrue(registry.isRegistered(type: "column"))
    }
    
    func testCustomComponentRegistration() {
        let registry = ComponentRegistry.shared
        
        registry.register(type: "customView") { props, theme in
            return UIView()
        }
        
        XCTAssertTrue(registry.isRegistered(type: "customView"))
    }
    
    // MARK: - Action Tests
    
    func testFlexActionCreation() {
        let action = FlexAction.navigate(to: "home_screen", data: ["animated": true])
        
        XCTAssertEqual(action.type, "navigate")
        XCTAssertEqual(action.getString("screen"), "home_screen")
        XCTAssertEqual(action.getBool("animated"), true)
    }
    
    func testFlexActionValidation() {
        let validAction = FlexAction.openUrl("https://example.com")
        XCTAssertNoThrow(try validAction.validate())
        
        let invalidAction = FlexAction.openUrl("not a valid url")
        XCTAssertThrowsError(try invalidAction.validate())
    }
    
    // MARK: - FlexProps Tests
    
    func testFlexPropsGetters() {
        let props = FlexProps(properties: [
            "title": "Hello",
            "count": 42,
            "enabled": true,
            "rating": 4.5
        ])
        
        XCTAssertEqual(props.getString("title"), "Hello")
        XCTAssertEqual(props.getInt("count"), 42)
        XCTAssertEqual(props.getBool("enabled"), true)
        XCTAssertEqual(props.getFloat("rating"), 4.5)
    }
    
    func testFlexPropsTypeConversion() {
        let props = FlexProps(properties: [
            "stringNumber": "42",
            "intAsFloat": 10
        ])
        
        // String to int conversion
        XCTAssertEqual(props.getInt("stringNumber"), 42)
        
        // Int to float conversion
        XCTAssertEqual(props.getFloat("intAsFloat"), 10.0)
    }
    
    // MARK: - Theme Defaults Tests
    
    func testDefaultThemeColors() {
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertNotNil(theme.getColor("primary"))
        XCTAssertNotNil(theme.getColor("secondary"))
        XCTAssertNotNil(theme.getColor("error"))
        XCTAssertNotNil(theme.getColor("success"))
    }
    
    func testDefaultThemeDimensions() {
        let theme = ThemeDefaults.defaultTheme
        
        XCTAssertNotNil(theme.getDimension("spacing_sm"))
        XCTAssertNotNil(theme.getDimension("spacing_md"))
        XCTAssertNotNil(theme.getDimension("spacing_lg"))
    }
    
    func testThemeMerging() {
        let customTheme = FlexTheme(
            colors: ["primary": "#FF0000"]
        )
        
        let merged = ThemeDefaults.defaultTheme.merged(with: customTheme)
        
        XCTAssertEqual(merged.getColor("primary"), "#FF0000")
        // Default secondary should still be there
        XCTAssertNotNil(merged.getColor("secondary"))
    }
    
    // MARK: - Performance Tests
    
    func testLargeConfigParsing() {
        // Create a large config with many nested components
        var childrenJson = ""
        for i in 0..<100 {
            childrenJson += """
            {
                "type": "text",
                "props": {
                    "content": "Item \\(i)"
                }
            },
            """
        }
        childrenJson.removeLast() // Remove trailing comma
        
        let largeConfigJson = """
        {
            "version": "1.0",
            "screenId": "large_screen",
            "root": {
                "type": "column",
                "children": [\\(childrenJson)]
            }
        }
        """
        
        let startTime = CFAbsoluteTimeGetCurrent()
        
        do {
            _ = try FlexParser.parseConfig(largeConfigJson)
            let timeElapsed = CFAbsoluteTimeGetCurrent() - startTime
            
            // Should parse in less than 1 second
            XCTAssertLessThan(timeElapsed, 1.0)
        } catch {
            XCTFail("Failed to parse large config: \\(error)")
        }
    }
}

// MARK: - Test Helpers

extension XCTestCase {
    func XCTAssertNoThrow<T>(_ expression: @autoclosure () throws -> T, _ message: @autoclosure () -> String = "", file: StaticString = #filePath, line: UInt = #line) {
        do {
            _ = try expression()
        } catch {
            XCTFail("Expected no error, but got: \\(error)", file: file, line: line)
        }
    }
}